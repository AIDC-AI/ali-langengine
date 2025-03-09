/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.dashscope.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionChoice;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.service.DefaultLLMService;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.model.agent.DashScopeAPIChainUrlOutputParser;
import com.alibaba.langengine.dashscope.model.agent.DashScopePromptConstants;
import com.alibaba.langengine.dashscope.model.agent.DashScopeStructuredChatOutputParser;
import com.alibaba.langengine.dashscope.model.completion.*;
import com.alibaba.langengine.dashscope.model.service.DashScopeService;
import com.alibaba.langengine.dashscope.model.service.openai.DashScopeOpenAIApi;
import com.google.common.collect.Maps;
import io.reactivex.Flowable;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import static com.alibaba.langengine.dashscope.DashScopeConfiguration.*;

/**
 * DashScope大模型，兼容OpenAI格式
 * https://help.aliyun.com/zh/dashscope/developer-reference/qwen-api
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class DashScopeOpenAIChatModel extends BaseChatModel<ChatCompletionRequest> {

    /**
     * service
     */
    private DefaultLLMService<DashScopeOpenAIApi> service;

    /**
     * 是否流式增量
     */
    private boolean sseInc = true;

    private static final String DEFAULT_BASE_URL = "https://dashscope.aliyuncs.com/";

    public DashScopeOpenAIChatModel() {
        this(DASHSCOPE_API_KEY);
    }

    public DashScopeOpenAIChatModel(String token) {
        setModel(DashScopeModelName.QWEN_TURBO);
//        setMaxTokens(256);
//        setTopP(0.8d);
        String serverUrl = !StringUtils.isEmpty(DASHSCOPE_OPENAI_COMPATIBLE_SERVER_URL) ? DASHSCOPE_OPENAI_COMPATIBLE_SERVER_URL : DEFAULT_BASE_URL;
        service = new DefaultLLMService(serverUrl, Duration.ofSeconds(Long.parseLong(DASHSCOPE_API_TIMEOUT)), true, token, DashScopeOpenAIApi.class);
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder();
        builder.messages(chatMessages);
        builder.functions(functions);
        return builder.build();
    }

    @Override
    public BaseMessage runRequest(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();

        ChatCompletionResult completionResult = service.execute(service.getApi().createChatCompletion(request));
        log.info("DashScopeOpenAIChatModel completionResult is {}", JSON.toJSONString(completionResult));
        if(completionResult == null) {
            throw new RuntimeException("create chat completion result failed");
        }

        Long totalTokens = 0L;
        if(completionResult.getUsage() != null) {
            totalTokens = completionResult.getUsage().getTotalTokens();
        }
        if(completionResult.getChoices() == null) {
            throw new RuntimeException("create chat completion choices failed");
        }
        Long finalTotalTokens = totalTokens;
        completionResult.getChoices().forEach(e -> {
            ChatMessage chatMessage = e.getMessage();
            if(chatMessage != null) {
                BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                message.setTotalTokens(finalTotalTokens);
                message.setOrignalContent(JSON.toJSONString(e));
                String role = chatMessage.getRole();
                String answer;
                if(chatMessage.getFunctionCall() != null && chatMessage.getFunctionCall().size() > 0) {
                    if(message instanceof AIMessage) {
                        AIMessage aiMessage = (AIMessage) message;
                        aiMessage.setToolUse(true);
                    }
                    Map<String, Object> functionCallMap = new HashMap<>();
                    functionCallMap.put("function_call", chatMessage.getFunctionCall());
                    answer = JSON.toJSONString(functionCallMap);
                } else {
                    answer = chatMessage.getContent().toString();
                }
                log.warn(getModel() + " chat answer is {}", answer);
                if (message != null) {
                    baseMessage.set(message);
                }
            }
        });

        return baseMessage.get();
    }

    @Override
    public BaseMessage runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<Object> functionCallContent = new AtomicReference<>();
        AtomicReference<ResponseCollector> functionCallNameContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<ResponseCollector> argumentContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<String> role = new AtomicReference<>();
        service.stream(service.getApi().createChatCompletionStream(request), ChatCompletionChunk.class)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    log.info("chunk result is {}", JSON.toJSONString(e));
                    if(CollectionUtils.isEmpty(e.getChoices())) {
                        log.error("chunk result choices is empty");
                        return;
                    }

                    ChatCompletionChoice choice = e.getChoices().get(0);
                    if("stop".equals(choice.getFinishReason()) || "function_call".equals(choice.getFinishReason())) {
                        return;
                    }
                    ChatMessage chatMessage = choice.getMessage();
                    if(chatMessage != null) {
                        if(!StringUtils.isEmpty(chatMessage.getRole())) {
                            role.set(chatMessage.getRole());
                        }
                        chatMessage.setRole(role.get());
                        BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                        if(message != null) {
                            if(chatMessage.getContent() != null) {
                                String answer = chatMessage.getContent().toString();
                                log.warn(getModel() + " chat stream answer is {}", answer);
                                if (message != null) {
                                    answerContent.get().collect(message.getContent());
                                    String response = answerContent.get().joining();
                                    message.setContent(response);
                                    baseMessage.set(message);
                                    if (consumer != null) {
                                        consumer.accept(message);
                                    }
                                }
                            } else if (chatMessage.getFunctionCall() != null && chatMessage.getFunctionCall().size() > 0) {
                                Map<String, Object> functionCallMap = Maps.newHashMap();
                                if (chatMessage.getFunctionCall().get("name") != null) {
                                    functionCallNameContent.get().collect(chatMessage.getFunctionCall().get("name").toString());
                                }
                                if (chatMessage.getFunctionCall().get("arguments") != null) {
                                    argumentContent.get().collect(chatMessage.getFunctionCall().get("arguments").toString());
                                }
                                functionCallMap.put("function_call", new HashMap<String, String>() {{
                                    put("name", functionCallNameContent.get().joining());
                                    put("arguments", argumentContent.get().joining());
                                }});
                                functionCallContent.set(functionCallMap);

                                if (functionCallContent.get() != null) {
                                    if (consumer != null) {
                                        String functionCallContentString = JSON.toJSONString(functionCallContent.get());
                                        log.warn(getModel() + " functionCall stream answer is {}", functionCallContentString);
                                        AIMessage aiMessage = new AIMessage();
                                        aiMessage.setAdditionalKwargs((Map<String, Object>) functionCallContent.get());
//                                        aiMessage.setContent(JSON.toJSONString(aiMessage.getAdditionalKwargs()));
                                        consumer.accept(aiMessage);
                                    }
                                }
                            }
                        }
                    }
                });

        if(functionCallContent.get() != null) {
            AIMessage aiMessage = new AIMessage();
            aiMessage.setAdditionalKwargs((Map<String, Object>)functionCallContent.get());
            baseMessage.set(aiMessage);
            log.info("functionCallContent get is {}", JSON.toJSONString(aiMessage));
            return baseMessage.get();
        }

        String responseContent = answerContent.get().joining();

        log.warn(getModel() + " final answer:" + responseContent);

        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent(responseContent);
        baseMessage.set(aiMessage);

        return baseMessage.get();
    }

}
