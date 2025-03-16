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
package com.alibaba.langengine.claude.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.claude.model.completion.*;
import com.alibaba.langengine.claude.model.service.ClaudeService;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageRole;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.alibaba.langengine.claude.ClaudeConfiguration.*;

/**
 * Claude大模型
 * https://docs.anthropic.com/en/api/messages
 * https://docs.anthropic.com/en/api/getting-started
 * https://console.anthropic.com/settings/keys
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class ClaudeChatModel extends BaseChatModel<ChatCompletionRequest> {

    /**
     *  Service
     */
    private ClaudeService service;

    /**
     * System prompt.
     * A system prompt is a way of providing context and instructions to Claude, such as specifying a particular goal or role.
     */
    private String system;

    /**
     * How the model should use the provided tools.
     * The model can use a specific tool, any available tool, or decide by itself.
     */
    private ToolChoice toolChoiceDomain;

    /**
     * 是否流式增量
     */
    private boolean sseInc = false;

    public ClaudeChatModel() {
        this(ANTHROPIC_API_KEY);
    }

    public ClaudeChatModel(String token) {
        setModel(ClaudeModelContants.CLAUDE_35_SONNET_20241022);
        service = new ClaudeService(ANTHROPIC_SERVER_URL, Duration.ofSeconds(Long.parseLong(ANTHROPIC_API_TIMEOUT)), true, token);
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages,
                                              List<FunctionDefinition> functions,
                                              List<String> stops,
                                              Consumer<BaseMessage> consumer,
                                              Map<String, Object> extraAttributes) {
        ChatCompletionRequest.ChatCompletionRequestBuilder builder = ChatCompletionRequest.builder();
        builder.messages(chatMessages);
        builder.system(getSystem());
        if(!CollectionUtils.isEmpty(functions)) {
            List<ToolDefinition> toolDefinitions = functions.stream().map(e -> {
                ToolDefinition toolDefinition = new ToolDefinition();
                toolDefinition.setName(e.getName());
                toolDefinition.setDescription(e.getDescription());
                toolDefinition.setParameters(e.getParameters());
                return toolDefinition;
            }).collect(Collectors.toList());
            builder.tools(toolDefinitions);
        }
        return builder.build();
    }

    @Override
    public BaseMessage runRequest(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();

        ChatCompletionResult completionResult = service.createCompletion(request);
        String completionResultString = JSON.toJSONString(completionResult);
        log.info("{} completionResult is {}", getModel(), completionResultString);
        if(completionResult == null) {
            throw new RuntimeException("completionResult is null");
        }
        if (completionResult.getType() != null && "error".equals(completionResult.getType())) {
            throw new RuntimeException(getModel() + " completionResult error  is " + JSON.toJSONString(completionResult.getError()));
        }
        if(CollectionUtils.isEmpty(completionResult.getContent())) {
            throw new RuntimeException("completionResult content is null");
        }
        // 判断是否存在工具召回
        if(completionResult.getContent().stream().anyMatch(e -> "tool_use".equals(e.getType()))) {
            AIMessage message = new AIMessage();
            message.setToolUse(true);
            message.setOrignalContent(completionResultString);
            message.setContent(JSON.toJSONString(completionResult.getContent()));
            baseMessage.set(message);
        } else {
            String responseContent = completionResult.getContent().get(0).getText();
            AIMessage message = new AIMessage();
            message.setOrignalContent(completionResultString);
            message.setContent(responseContent);
            baseMessage.set(message);
        }
        return baseMessage.get();
    }

    @Override
    public BaseMessage runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();
        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(true));

        service.createCompletionStream(request)
                .doOnError(Throwable::printStackTrace)
                .blockingForEach(e -> {
                    log.info("chunk is {}", JSON.toJSONString(e));
                    if("message_stop".equals(e.getType())) {
                        return;
                    }
                    if("content_block_delta".equals(e.getType())) {
                        if(e.getDelta() != null && "text_delta".equals(e.getDelta().getType())) {
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setRole(ChatMessageRole.ASSISTANT.value());
                            chatMessage.setContent(e.getDelta().getText());
                            BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                            if(message != null) {
                                answerContent.get().collect(message.getContent());
                                String response = answerContent.get().joining();
                                message.setContent(response);
                                baseMessage.set(message);
                                if(consumer != null) {
                                    consumer.accept(message);
                                }
                            }
                        }
                    }
                });

        return baseMessage.get();
    }
}
