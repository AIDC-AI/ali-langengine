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
package com.alibaba.langengine.core.languagemodel;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import com.alibaba.langengine.core.agent.AgentOutputParser;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.runnables.RunnableStreamCallback;
import com.alibaba.langengine.core.memory.BaseMemory;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.outputs.Generation;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.prompt.ChatPromptValue;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.alibaba.langengine.core.prompt.StringPromptValue;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableConfig;
import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableModelInput;
import com.alibaba.langengine.core.runnables.RunnableOutput;
import com.alibaba.langengine.core.runnables.RunnableStringVar;
import com.alibaba.langengine.core.tokenizers.GPT2Tokenizer;
import com.alibaba.langengine.core.util.JacksonUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;
import org.apache.commons.collections.CollectionUtils;

/**
 * Base language model
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseLanguageModel<T> extends Runnable<RunnableInput, RunnableOutput> {

    /**
     * token(api key)
     */
    private String token;

    /**
     * The model that will complete your prompt.
     */
    private String model;

    /**
     * The maximum number of tokens to generate before stopping.
     * Note that our models may stop before reaching this maximum.
     * This parameter only specifies the absolute maximum number of tokens to generate.
     */
    private Integer maxTokens;

    /**
     * Whether to incrementally stream the response using server-sent events.
     */
    private boolean stream = false;

    /**
     * Only sample from the top K options for each subsequent token.
     */
    private Integer topK;

    /**
     * Use nucleus sampling.
     * In nucleus sampling, we compute the cumulative distribution over all the options for each subsequent token in decreasing probability order and cut it off once it reaches a particular probability specified by top_p.
     * You should either alter temperature or top_p, but not both.
     */
    private Double topP;

    /**
     * Amount of randomness injected into the response.
     * Defaults to 1.0. Ranges from 0.0 to 1.0. Use temperature closer to 0.0 for analytical / multiple choice, and closer to 1.0 for creative and generative tasks.
     * Note that even with temperature of 0.0, the results will not be fully deterministic.
     */
    private Double temperature;

    /**
     * Custom text sequences that will cause the model to stop generating.
     */
    private Double frequencyPenalty;

    /**
     * Number between -2.0 and 2.0.
     * Positive values penalize new tokens based on whether they appear in the text so far, increasing the model's likelihood to talk about new topics.
     */
    private Double presencePenalty;

    /**
     * An object describing metadata about the request.
     */
    private Map<String, Object> metadata;

    /**
     * 工具选择策略，默认为auto
     * "auto": 表示由大模型进行工具策略的选择。
     * "required": 如果您希望无论输入什么问题，Function Calling 都可以进行工具调用，可以设定tool_choice参数为"required"；
     * "none": 如果您希望无论输入什么问题，Function Calling 都不会进行工具调用，可以设定tool_choice参数为"none"；
     * {"type": "function", "function": {"name": "the_function_to_call"}}
     * 如果您希望对于某一类问题，Function Calling 能够强制调用某个工具，可以设定tool_choice参数为{"type": "function", "function": {"name": "the_function_to_call"}}，其中the_function_to_call是您指定的工具函数名称。
     */
    private String toolChoice = "auto";


    /**
     * 当前是否是流式模式
     *
     * @param consumer
     * @return
     */
    public boolean currentStream(Consumer<T> consumer) {
        return isStream() || (consumer != null);
    }

    //****** generatePrompt START ******//

    /**
     * Pass a sequence of prompts to the model and return model generations.
     *
     * @param prompts
     * @param functions
     * @return
     */
    public LLMResult generatePrompt(List<PromptValue> prompts, List<FunctionDefinition> functions) {
        return generatePrompt(prompts, functions, null, null);
    }
    public LLMResult generatePrompt(List<PromptValue> prompts, List<FunctionDefinition> functions, List<String> stops) {
        return generatePrompt(prompts, functions, stops, null, null, null);
    }
    public LLMResult generatePrompt(List<PromptValue> prompts, List<String> stops, ExecutionContext executionContext, Consumer<T> consumer) {
        return generatePrompt(prompts, null, stops, executionContext, consumer, null);
    }
    public LLMResult generatePrompt(List<PromptValue> prompts, List<String> stops, ExecutionContext executionContext, Consumer<T> consumer, Map<String, Object> extraAttributes) {
        return generatePrompt(prompts, null, stops, executionContext, consumer, extraAttributes);
    }
    public LLMResult generatePrompt(List<PromptValue> prompts, List<FunctionDefinition> functions, List<String> stops, Map<String, Object> extraAttributes) {
        return generatePrompt(prompts, functions, stops, null, null, extraAttributes);
    }
    public LLMResult generatePrompt(List<PromptValue> prompts, List<FunctionDefinition> functions, List<String> stops, ExecutionContext executionContext, Consumer<T> consumer) {
        return generatePrompt(prompts, functions, stops, executionContext, consumer, null);
    }
    public abstract LLMResult generatePrompt(List<PromptValue> prompts, List<FunctionDefinition> functions, List<String> stops, ExecutionContext executionContext, Consumer<T> consumer, Map<String, Object> extraAttributes);

    //****** generatePrompt END ******//


    //****** generatePromptAsync START ******//

    /**
     * 异步化获取提示值列表并返回LLMResult
     *
     * @param prompts
     * @param stops
     * @return
     */
    public CompletableFuture<LLMResult> generatePromptAsync(List<PromptValue> prompts, List<String> stops) {
        return generatePromptAsync(prompts, null, stops, null, null, null);
    }
    public CompletableFuture<LLMResult> generatePromptAsync(List<PromptValue> prompts, List<FunctionDefinition> functions, List<String> stops, ExecutionContext executionContext, Consumer<T> consumer, Map<String, Object> extraAttributes) {
        return CompletableFuture.supplyAsync(() -> generatePrompt(prompts, functions, stops, executionContext, consumer, extraAttributes));
    }

    //****** generatePromptAsync END ******//


    //****** predict兼容 START ******//

    /**
     * 从文本预测文本
     *
     * @param text 文本
     * @return
     */
    public String predict(String text) {
        return predict(text, null, null);
    }
    public String predict(String text, List<String> stops) {
        return predict(text, stops, null, null);
    }
    public String predict(String text, Consumer<T> consumer) {
        return predict(text, null, null, consumer);
    }
    public String predict(String text, List<String> stops, Consumer<T> consumer) {
        return predict(text, stops, null, consumer);
    }
    public String predict(String text, List<String> stops, ExecutionContext executionContext, Consumer<T> consumer) {
        return predict(text, stops, executionContext, consumer, null);
    }
    public abstract String predict(String text, List<String> stops, ExecutionContext executionContext, Consumer<T> consumer, Map<String, Object> extraAttributes);

    //****** predict兼容 END ******//


    //****** 异步化predict兼容 START ******//

    /**
     * 从文本预测文本
     * 异步化方法
     * @param text 文本
     * @return
     */
    public CompletableFuture<String> predictAsync(String text) {
        return predictAsync(text, null, null);
    }
    public CompletableFuture<String> predictAsync(String text, ExecutionContext executionContext) {
        return predictAsync(text, null, executionContext);
    }
    public CompletableFuture<String> predictAsync(String text, List<String> stops) {
        return predictAsync(text, stops, null);
    }
    public CompletableFuture<String> predictAsync(String text, List<String> stops, ExecutionContext executionContext) {
        return predictAsync(text, stops, executionContext, null);
    }
    public CompletableFuture<String> predictAsync(String text, List<String> stops, ExecutionContext executionContext, Map<String, Object> extraAttributes) {
        return CompletableFuture.supplyAsync(() -> predict(text, stops, executionContext, null, extraAttributes));
    }

    //****** 异步化predict兼容 END ******//


    /**
     * Return the ordered ids of the tokens in a text.
     *
     * @param text
     * @return
     */
    public List<Integer> getTokenIds(String text) {
        GPT2Tokenizer tokenizer = new GPT2Tokenizer();
        return tokenizer.encode(text);
    }

    /**
     * Get the number of tokens present in the text.
     *
     * @param text
     * @return
     */
    public int getNumTokens(String text) {
        GPT2Tokenizer tokenizer = new GPT2Tokenizer();
        return tokenizer.getTokenCount(text);
    }

    /**
     * Get the number of tokens in the messages.
     *
     * @param messages
     * @return
     */
    public int getNumTokensFromMessages(List<BaseMessage> messages) {
        return getNumTokens(MessageConverter.getBufferString(messages));
    }

    /**
     * 前缀描述
     *
     * @param memory
     * @param isCH
     * @return
     */
    public abstract String getStructuredChatAgentPrefixPrompt(BaseMemory memory, boolean isCH);

    /**
     * 后缀描述
     *
     * @param memory
     * @param isCH
     * @return
     */
    public abstract String getStructuredChatAgentSuffixPrompt(BaseMemory memory, boolean isCH);

    /**
     * 介绍性的描述
     * @param memory
     * @param isCH
     * @return
     */
    public abstract String getStructuredChatAgentInstructionsPrompt(BaseMemory memory, boolean isCH);

    /**
     * 工具性的描述
     * @param memory
     * @param isCH
     * @return
     */
    public abstract String getToolDescriptionPrompt(BaseMemory memory, boolean isCH);

    /**
     * 输出格式化器
     *
     * @return
     */
    public abstract AgentOutputParser getStructuredChatOutputParser();

    public abstract AgentOutputParser getAPIChainUrlOutputParser();

    /**
     * 用于回调trace，方便了解到当前模型内部的一些关键信息
     */
    public String getTraceInfo(){
        return getClass().getName();
    }

    /**
     * 拿到LLM当前家族簇的具体名称；
     * 比如千问系列多个模型、MiniMax系列有多个模型
     * 这里返回家族中的具体某个模型，用于进行统计计费
     */
    public String getLlmFamilyName() {
        return getClass().getSimpleName();
    }

    /**
     * 拿具体模型的名称
     *
     * @return
     */
    public String getLlmModelName() {
        return model;
    }

    public <T> void onLlmStart(BaseLanguageModel llm, ExecutionContext executionContext) {
        onLlmStart(llm, executionContext, null);
    }

    public <T> void onLlmStart(BaseLanguageModel llm, ExecutionContext executionContext, Consumer<T> chunkConsumer) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setLlm(llm);
            executionContext.setPrompts(null);
            executionContext.setLlmResult(null);
            executionContext.setThrowable(null);
            executionContext.setChunkConsumer(chunkConsumer);
            getCallbackManager().onLlmStart(executionContext);
        }
    }

    public <T> void onLlmStart(BaseLanguageModel llm, List<String> prompts, ExecutionContext executionContext) {
        onLlmStart(llm, prompts, executionContext, null);
    }

    public <T> void onLlmStart(BaseLanguageModel llm, List<String> prompts, ExecutionContext executionContext, Consumer<T> chunkConsumer) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setLlm(llm);
            executionContext.setPrompts(prompts);
            executionContext.setLlmResult(null);
            executionContext.setThrowable(null);
            executionContext.setChunkConsumer(chunkConsumer);
            getCallbackManager().onLlmStart(executionContext);
        }
    }

    public <T> void onLlmEnd(BaseLanguageModel llm, ExecutionContext executionContext) {
        onLlmEnd(llm, executionContext, null);
    }

    public <T> void onLlmEnd(BaseLanguageModel llm, ExecutionContext executionContext, Consumer<T> chunkConsumer) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setLlm(llm);
            executionContext.setPrompts(null);
            executionContext.setThrowable(null);
            executionContext.setChunkConsumer(chunkConsumer);
            getCallbackManager().onLlmEnd(executionContext);
        }
    }

    public <T> void onLlmEnd(BaseLanguageModel llm, List<String> prompts, LLMResult llmResult, ExecutionContext executionContext) {
        onLlmEnd(llm, prompts, llmResult, executionContext, null);
    }

    public <T> void onLlmEnd(BaseLanguageModel llm, List<String> prompts, LLMResult llmResult, ExecutionContext executionContext, Consumer<T> chunkConsumer) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setLlm(llm);
            executionContext.setPrompts(prompts);
            executionContext.setLlmResult(llmResult);
            executionContext.setThrowable(null);
            executionContext.setChunkConsumer(chunkConsumer);
            getCallbackManager().onLlmEnd(executionContext);
        }
    }

    public <T> void onLlmError(BaseLanguageModel llm, Throwable throwable, ExecutionContext executionContext) {
        onLlmError(llm, throwable, executionContext, null);
    }

    public <T> void onLlmError(BaseLanguageModel llm, Throwable throwable, ExecutionContext executionContext, Consumer<T> chunkConsumer) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setLlm(llm);
            executionContext.setPrompts(null);
            executionContext.setThrowable(throwable);
            executionContext.setLlmResult(null);
            executionContext.setChunkConsumer(chunkConsumer);
            getCallbackManager().onLlmError(executionContext);
        }
    }

    public <T> void onLlmError(BaseLanguageModel llm, List<String> prompts, Throwable throwable, ExecutionContext executionContext, Consumer<T> chunkConsumer) {
        if (executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if(getCallbackManager() != null) {
            executionContext.setLlm(llm);
            executionContext.setPrompts(prompts);
            executionContext.setThrowable(throwable);
            executionContext.setLlmResult(null);
            executionContext.setChunkConsumer(chunkConsumer);
            getCallbackManager().onLlmError(executionContext);
        }
    }

    /**
     * callback manager
     */
    private BaseCallbackManager callbackManager;

    public void setCallbackManager(BaseCallbackManager callbackManager) {
        if (callbackManager != null && callbackManager.getRunManager() != null) {
            callbackManager.getRunManager().setRunType("llm");
            callbackManager.getRunManager().setName(this.getClass().getSimpleName());
        }
        this.callbackManager = callbackManager;
    }

    public RunnableOutput invoke(RunnableInput input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    @Override
    public RunnableOutput stream(RunnableInput input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        return invoke(input, config, chunkConsumer);
    }

    @Override
    public RunnableOutput streamLog(RunnableInput input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        if((config != null && config.isStreamLog()) || callbackManager == null) {
            setCallbackManager(new CallbackManager());
            callbackManager.addHandler(new RunnableStreamCallback());
        }
        return invoke(input, config, chunkConsumer);
    }

    private RunnableOutput invoke(RunnableInput input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        PromptValue promptValue = convertInput(input);
        List<PromptValue> promptValueList = new ArrayList<>();
        promptValueList.add(promptValue);

        List<String> stops = null;
        List<FunctionDefinition> functions = null;
        if(config != null && config.getMetadata() != null &&  config.getMetadata().size() > 0) {
            if(config.getMetadata().get("stop") != null) {
                stops = (List<String>) config.getMetadata().get("stop");
            }
            if(config.getMetadata().get("functions") != null) {
                functions = (List<FunctionDefinition>) config.getMetadata().get("functions");
            }
        }

        Consumer<T> consumer = null;
        if(chunkConsumer != null) {
            consumer = t -> chunkConsumer.accept(t);
        }
        LLMResult llmResult = generatePrompt(promptValueList, functions, stops, (config != null ? config.getExecutionContext() : null), consumer, config != null ? config.getExtraAttributes() : null);
        if(llmResult == null
                || CollectionUtils.isEmpty(llmResult.getGenerations())
                || CollectionUtils.isEmpty(llmResult.getGenerations().get(0))) {
            return null;
        }
        Generation generation = llmResult.getGenerations().get(0).get(0);
        if(generation.getMessage() != null) {
            return generation.getMessage();
        } else if(generation.getText() != null) {
            RunnableStringVar stringVar = new RunnableStringVar();
            stringVar.setValue(generation.getText());
            return stringVar;
        }
        return null;
    }

    private PromptValue convertInput(RunnableInput input) {
        if(input instanceof PromptValue) {
            return (PromptValue)input;
        } else if(input instanceof RunnableStringVar) {
            RunnableStringVar stringVar = (RunnableStringVar) input;
            StringPromptValue promptValue = new StringPromptValue();
            promptValue.setText(stringVar.getValue());
            return promptValue;
        } else if(input instanceof RunnableModelInput) {
            RunnableModelInput runnableModelInput = (RunnableModelInput) input;
            ChatPromptValue promptValue = new ChatPromptValue();
            promptValue.setMessages(runnableModelInput.getMessages());
            return promptValue;
        } else {
            throw new RuntimeException("BaseLanguageModel invalid input type, must be a PromptValue, str, or list of BaseMessages.");
        }
    }
}
