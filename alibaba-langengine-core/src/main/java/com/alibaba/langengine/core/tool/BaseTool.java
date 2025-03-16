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
package com.alibaba.langengine.core.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.callback.BaseCallbackManager;
import com.alibaba.langengine.core.callback.CallbackManager;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionParameter;
import com.alibaba.langengine.core.runnables.RunnableStreamCallback;
import com.alibaba.langengine.core.config.LangEngineConfiguration;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.runnables.Runnable;
import com.alibaba.langengine.core.runnables.RunnableConfig;
import com.alibaba.langengine.core.runnables.RunnableHashMap;
import com.alibaba.langengine.core.runnables.RunnableOutput;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.Data;

import java.util.Map;
import java.util.TreeMap;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * LangEngine工具必须实现的接口
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property=JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseTool extends Runnable<Object, RunnableOutput> {

    /**
     * callback manager
     */
    private BaseCallbackManager callbackManager;

    public BaseCallbackManager getCallbackManager() {
        if(callbackManager == null) {
            callbackManager = LangEngineConfiguration.CALLBACK_MANAGER;
        }
        return callbackManager;
    }

    /**
     * 明确传达其用途的工具的唯一名称，英文key
     */
    private String name;

    /**
     * 工具中文名称
     */
    private String humanName;

    /**
     * 用于告诉模型如何/何时/为何使用该工具
     */
    private String description;

    /**
     * 工具方法名称（Semantic kernel适用）
     */
    private String functionName;

    /**
     * 方法工具
     */
    private Function<Map<String, Object>, Map<String, Object>> func;

    /**
     * 简易工具
     */
    private Function<String, String> basicFunc;

    /**
     * 参数json
     */
    private String parameters;

    /**
     * 工具参数集合
     */
    private Map<String, Object> args = new TreeMap<>();

    /**
     * 是否直接返回工具的输出，将其设置为 true
     */
    private boolean returnDirect;

    private boolean verbose;

    public FunctionDefinition toParams() {
        FunctionDefinition function = new FunctionDefinition();
        function.setName(getName());
        function.setDescription(getDescription());
        function.setParameters(JSON.parseObject(getParameters(), FunctionParameter.class));
        return function;
    }

    public void setCallbackManager(BaseCallbackManager callbackManager) {
        if (callbackManager != null && callbackManager.getRunManager() != null) {
            callbackManager.getRunManager().setRunType("tool");
            callbackManager.getRunManager().setName(this.name);
        }
        this.callbackManager = callbackManager;
    }

    public ToolExecuteResult run(String toolInput) {
        return run(toolInput, null);
    }

    public abstract ToolExecuteResult run(String toolInput, ExecutionContext executionContext);

    public ToolExecuteResult invoke(Object input, RunnableConfig config, Consumer<Object> consumer) {
//        if(callbackManager == null) {
//            setCallbackManager(new CallbackManager());
//            callbackManager.addHandler(new RunnableStreamCallback());
//        }

        ExecutionContext executionContext = null;
        if(config != null && config.isStreamLog()) {
            executionContext = new ExecutionContext();
            executionContext.setChunkConsumer(consumer);
        }
        if(input instanceof BaseMessage) {
            return run(((BaseMessage) input).getContent(), executionContext);
        } else if(input instanceof String) {
            return run(input.toString(), executionContext);
        }
        else if(input instanceof RunnableHashMap) {
            return run(JSON.toJSONString(input));
        }
        return null;
    }

    @Override
    public ToolExecuteResult invoke(Object input, RunnableConfig config) {
        return invoke(input, config, null);
    }

    @Override
    public RunnableOutput stream(Object input, RunnableConfig config, Consumer<Object> chunkConsumer) {
        RunnableOutput toolExecuteResult = invoke(input);
        chunkConsumer.accept(toolExecuteResult);
        return toolExecuteResult;
    }

    public String serialize() {
        try {
            return JacksonUtils.MAPPER.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void onToolStart(BaseTool baseTool, String toolInput, ExecutionContext executionContext) {
        if(executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if (getCallbackManager() != null) {
            executionContext.setTool(baseTool);
            executionContext.setToolInput(toolInput);
            executionContext.setToolExecuteResult(null);
            getCallbackManager().onToolStart(executionContext);
        }
    }

    public void onToolEnd(BaseTool baseTool, String toolInput, ToolExecuteResult toolExecuteResult, ExecutionContext executionContext) {
        if(executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if (getCallbackManager() != null) {
            executionContext.setTool(baseTool);
            executionContext.setToolInput(toolInput);
            if(toolExecuteResult != null) {
                executionContext.setToolExecuteResult(toolExecuteResult);
            } else {
                executionContext.setToolExecuteResult(null);
            }
            getCallbackManager().onToolEnd(executionContext);
        }
    }

    public void onToolError(BaseTool baseTool, Throwable throwable, ExecutionContext executionContext) {
        if(executionContext == null) {
            executionContext = new ExecutionContext();
        }
        if (getCallbackManager() != null) {
            executionContext.setTool(baseTool);
            executionContext.setThrowable(throwable);
            executionContext.setToolExecuteResult(null);
            getCallbackManager().onToolError(executionContext);
        }
    }
}
