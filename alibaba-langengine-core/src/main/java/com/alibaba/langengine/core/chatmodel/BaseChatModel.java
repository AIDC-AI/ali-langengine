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
package com.alibaba.langengine.core.chatmodel;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.languagemodel.BaseLanguageModel;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.outputs.ChatResult;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.prompt.*;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * ChatModel模型基类
 *
 * @author xiaoxuan.lp
 */
@Data
@Slf4j
public abstract class BaseChatModel<T extends ChatCompletionRequest> extends BaseLanguageModel<BaseMessage> {

    @Override
    public LLMResult generatePrompt(List<PromptValue> prompts, List<FunctionDefinition> functions, List<String> stops, ExecutionContext executionContext, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        List<List<BaseMessage>> promptMessages = new ArrayList<>();
        for (PromptValue prompt : prompts) {
            promptMessages.add(prompt.toMessages());
        }
        return generate(promptMessages, functions, stops, executionContext, consumer, extraAttributes);
    }

    public LLMResult generate(List<List<BaseMessage>> messages, List<FunctionDefinition> functions, List<String> stops, ExecutionContext executionContext, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        if(executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setMessages(messages);
            executionContext.setFunctions(functions);
        }

        try {
            onLlmStart(this, executionContext, consumer);

            LLMResult llmResult;
            if(executionContext != null && executionContext.getLlmResult() != null) {
                llmResult = executionContext.getLlmResult();
            } else {
                List<ChatResult> results = new ArrayList<>();
                for (List<BaseMessage> message : messages) {
                    ChatResult result = generateWithCache(message, functions, stops, executionContext, consumer, extraAttributes);
                    results.add(result);

                }
                llmResult = new LLMResult();
                llmResult.setGenerations(results.stream().map(result -> result.getGenerations()).collect(Collectors.toList()));
            }
            if(getCallbackManager() != null) {
                executionContext.setMessages(messages);
                executionContext.setFunctions(functions);
                executionContext.setLlmResult(llmResult);
            }
            onLlmEnd(this, executionContext, consumer);

            return llmResult;
        } catch (Throwable e) {
            if(getCallbackManager() != null) {
                executionContext.setMessages(messages);
                executionContext.setFunctions(functions);
            }
            onLlmError(this, e, executionContext, consumer);
            throw e;
        }
    }

    public ChatResult generateWithCache(List<BaseMessage> messages, List<FunctionDefinition> functions, List<String> stops, ExecutionContext executionContext, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        String prompt = null;
        String llmString = null;
        if(LangEngineConfiguration.CurrentCache != null) {
            prompt = JSON.toJSONString(messages);
            if (!CollectionUtils.isEmpty(stops)) {
                llmString = JSON.toJSONString(stops);
            }
            List<Generation> cacheVal = LangEngineConfiguration.CurrentCache.get(prompt, llmString);
            if(cacheVal != null) {
                ChatResult chatResult = new ChatResult();
                chatResult.setGenerations(cacheVal);
                return chatResult;
            }
        }

        BaseMessage resultMessage = run(messages, functions, stops, consumer, extraAttributes);
        if(resultMessage == null) {
            throw new RuntimeException("resultMessage is null");
        }

        Generation generation = new Generation();
        generation.setMessage(resultMessage);
        generation.setText(resultMessage.getContent());
        generation.setGenerationInfo(new HashMap<>());

        ChatResult chatResult = new ChatResult();
        chatResult.setGenerations(new ArrayList<>());
        chatResult.getGenerations().add(generation);

        if (LangEngineConfiguration.CurrentCache != null) {
            LangEngineConfiguration.CurrentCache.update(prompt, llmString, chatResult.getGenerations());
        }

        return chatResult;
    }

    @Override
    public String predict(String text, List<String> stops, ExecutionContext executionContext, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        List<List<BaseMessage>> messages = new ArrayList<>();
        List<BaseMessage> message = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent(text);
        message.add(humanMessage);
        messages.add(message);
        LLMResult llmResult = generate(messages, null, stops, executionContext, consumer, extraAttributes);
        if(llmResult != null && llmResult.getGenerations() != null
                && llmResult.getGenerations().size() > 0
                && llmResult.getGenerations().get(0).size() > 0) {
            return llmResult.getGenerations().get(0).get(0).getText();
        }
        return null;
    }

    public BaseMessage run(List<BaseMessage> messages) {
        return run(messages, null, null, null, null);
    }

    /**
     * run
     *
     * @param messages message.content Available options: user, assistant
     * @param functions
     * @param stops
     * @param consumer
     * @param extraAttributes
     * @return
     */
    public BaseMessage run(List<BaseMessage> messages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        log.info("model is {}", getLlmModelName());
        log.info("messages is {}", JSON.toJSONString(messages));
        List<ChatMessage> chatMessages = MessageConverter.convertMessageToChatMessage(messages);
        log.info("chatMessages is {}", (chatMessages != null ? JSON.toJSONString(chatMessages) : null));
        log.info("functions is {}", (functions != null ? JSON.toJSONString(functions) : null));

        T request = buildRequest(chatMessages, functions, stops, consumer, extraAttributes);
        request.setModel(request.getModel() != null ? request.getModel() : getModel());
        request.setTemperature(request.getTemperature() != null ? request.getTemperature() : getTemperature());
        request.setMaxTokens(request.getMaxTokens() != null ? request.getMaxTokens() : getMaxTokens());
        request.setTopK(request.getTopK() != null ? request.getTopK() : getTopK());
        request.setTopP(request.getTopP() != null ? request.getTopP() : getTopP());
        request.setFrequencyPenalty(request.getFrequencyPenalty() != null ? request.getFrequencyPenalty() : getFrequencyPenalty());
        request.setPresencePenalty(request.getPresencePenalty() != null ? request.getPresencePenalty() : getPresencePenalty());
        request.setStop(!CollectionUtils.isEmpty(request.getStop()) ? request.getStop() : stops);
        request.setStream(currentStream(consumer));

        log.info("{} request is {}", getClass().getName(), JSON.toJSONString(request));
        if(!currentStream(consumer)) {
            return runRequest(request, stops, consumer, extraAttributes);
        } else {
            return runRequestStream(request, stops, consumer, extraAttributes);
        }
    }

    /**
     * 构建请求对象
     *
     * @param chatMessages,
     * @param functions
     * @param stops
     * @param consumer
     * @param extraAttributes
     * @return
     */
    public abstract T buildRequest(List<ChatMessage> chatMessages,
                                   List<FunctionDefinition> functions,
                                   List<String> stops,
                                   Consumer<BaseMessage> consumer,
                                   Map<String, Object> extraAttributes);

    /**
     * 非流式请求
     *
     * @param request
     * @param stops
     * @param consumer
     * @param extraAttributes
     * @return
     */
    public abstract BaseMessage runRequest(T request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes);

    /**
     * 流式请求
     *
     * @param request
     * @param stops
     * @param consumer
     * @param extraAttributes
     * @return
     */
    public abstract BaseMessage runRequestStream(T request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes);

    @Override
    public String getStructuredChatAgentPrefixPrompt(BaseMemory memory, boolean isCH) {
        return null;
    }

    @Override
    public String getStructuredChatAgentSuffixPrompt(BaseMemory memory, boolean isCH) {
        return null;
    }

    @Override
    public String getStructuredChatAgentInstructionsPrompt(BaseMemory memory, boolean isCH) {
        return null;
    }

    @Override
    public String getToolDescriptionPrompt(BaseMemory memory, boolean isCH) {
        return null;
    }

    @Override
    public AgentOutputParser getStructuredChatOutputParser() {
        return null;
    }

    @Override
    public AgentOutputParser getAPIChainUrlOutputParser() {
        return null;
    }
}
