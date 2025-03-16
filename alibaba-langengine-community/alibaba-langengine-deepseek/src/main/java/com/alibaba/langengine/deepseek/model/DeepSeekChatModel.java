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
package com.alibaba.langengine.deepseek.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import com.alibaba.langengine.core.model.fastchat.service.FastChatService;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.deepseek.DeepSeekConfiguration.*;

/**
 * OpenAI ChatModel大模型（支持gpt3.5以上），用到chatMessage方式
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class DeepSeekChatModel extends BaseChatModel<ChatCompletionRequest> {

    private FastChatService service;

    private static final String DEFAULT_BASE_URL = "https://api.deepseek.com/";

    public DeepSeekChatModel() {
        this(null);
    }

    public DeepSeekChatModel(String baseUrl) {
        this(baseUrl, DEEPSEEK_API_KEY);
    }

    public DeepSeekChatModel(String baseUrl, String apiKey) {
        setModel(DeepSeekModelConstants.DEEPSEEK_CHAT);
        String serverUrl;
        if(!StringUtils.isEmpty(baseUrl)) {
            serverUrl = baseUrl;
        } else {
            serverUrl = !StringUtils.isEmpty(DEEPSEEK_SERVER_URL) ? DEEPSEEK_SERVER_URL : DEFAULT_BASE_URL;
        }
        service = new FastChatService(serverUrl, Duration.ofSeconds(Long.parseLong(DEEPSEEK_AI_TIMEOUT)), true, apiKey);
    }

    /**
     * user
     */
    private String user;

    /**
     * logitBias
     */
    private Map<String, Integer> logitBias;

    /**
     * 模型是否返回json格式结果
     */
    private boolean jsonMode = false;

    /**
     * 是否流式增量
     */
    private boolean sseInc = true;

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder();
        builder.messages(chatMessages);
        if(user != null) {
            builder.user(user);
        }
        if(logitBias != null) {
            builder.logitBias(logitBias);
        }
        if(jsonMode) {
            builder.responseFormat(new HashMap<String, String>() {{
                put("type", "json_object");
            }});
        }
        if(!CollectionUtils.isEmpty(functions)) {
            List<ToolDefinition> toolDefinitions = functions.stream().map(e -> {
                ToolDefinition toolDefinition = new ToolDefinition();
                ToolFunction toolFunction = new ToolFunction();
                toolFunction.setName(e.getName());
                toolFunction.setDescription(e.getDescription());

                FunctionParameter toolParameter = new FunctionParameter();
                if(e.getParameters() != null) {
                    toolParameter.setProperties(e.getParameters().getProperties());
                    toolParameter.setType(e.getParameters().getType());
                    toolParameter.setRequired(e.getParameters().getRequired());
                }
                toolFunction.setParameters(toolParameter);
                toolDefinition.setFunction(toolFunction);
                return toolDefinition;
            }).collect(Collectors.toList());
            builder.tools(toolDefinitions);
        }
        return builder.build();
    }

    @Override
    public BaseMessage runRequest(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        service.createChatCompletion(request).getChoices().forEach(e -> {
            ChatMessage chatMessage = e.getMessage();
            if(chatMessage != null) {
                BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                if (message != null) {
                    message.setOrignalContent(JSON.toJSONString(e));
                    baseMessage.set(message);
                }
            }
        });
        BaseMessage finalMessage = baseMessage.get();
        log.info("finalMessage response is {}", JSON.toJSONString(finalMessage));
        return finalMessage;
    }

    @Override
    public BaseMessage runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<Object> functionCallContent = new AtomicReference<>();
        AtomicReference<ResponseCollector> functionCallNameContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<ResponseCollector> argumentContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<String> role = new AtomicReference<>();
        service.streamChatCompletion(request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    log.info("chunk result is {}", JSON.toJSONString(e));
                    if(CollectionUtils.isEmpty(e.getChoices())) {
                        log.error("chunk result choices is empty");
                        return;
                    }

                    ChatCompletionChoice choice = e.getChoices().get(0);
                    if("stop".equals(choice.getFinishReason())
                            || "function_call".equals(choice.getFinishReason())) {
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
