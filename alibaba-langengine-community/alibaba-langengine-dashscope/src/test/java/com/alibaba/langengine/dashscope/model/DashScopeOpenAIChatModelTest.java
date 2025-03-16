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
import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.prompt.ChatPromptValue;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import org.junit.jupiter.api.Test;

import java.util.*;

public class DashScopeOpenAIChatModelTest {

    @Test
    public void test_predict() {
        // success
        DashScopeOpenAIChatModel llm = new DashScopeOpenAIChatModel();
        llm.setModel(DashScopeModelName.DEEPSEEK_R1);
        System.out.println("response:" + llm.predict("你是谁？"));
    }

    @Test
    public void test_predict_function_call() {
        // success
        DashScopeOpenAIChatModel llm = new DashScopeOpenAIChatModel();
        llm.setToolChoice("required");

        List<PromptValue> promptValueList = new ArrayList<>();
        ChatPromptValue promptValue = new ChatPromptValue();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("我有一个请求,requestId是 16lxqklu2vlaj,请问具体的调用详情能告诉我么？");
        promptValue.getMessages().add(humanMessage);
        promptValueList.add(promptValue);

        List<FunctionDefinition> functionDefinitions = new ArrayList<>();
        FunctionDefinition functionDefinition = new FunctionDefinition();
        functionDefinition.setName("ApiLogTool");
        functionDefinition.setDescription("API日志查询");
        FunctionParameter parameter = new FunctionParameter();
        parameter.setType("object");
        parameter.setRequired(Arrays.asList(new String[]{"requestId"}));

        Map<String, FunctionProperty> properties = new HashMap<>();
        FunctionProperty property = new FunctionProperty();
        property.setType("string");
        property.setDescription("调用请求id");
        properties.put("requestId", property);
        parameter.setProperties(properties);
        functionDefinition.setParameters(parameter);
        functionDefinitions.add(functionDefinition);

        LLMResult llmResult = llm.generatePrompt(promptValueList, functionDefinitions, null);
        System.out.println("response:" + JSON.toJSONString(llmResult));
    }

    @Test
    public void test_chat_stream_callback() {
        // successx
        DashScopeOpenAIChatModel llm = new DashScopeOpenAIChatModel();
        llm.setStream(true);

        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        messages.add(humanMessage);
        humanMessage.setContent("你是谁？");

        {
            BaseMessage result = llm.run(messages, null, null, (m) -> {
                System.out.println("callback:" + JSON.toJSONString(m));
            }, null);
            System.out.println("result=[" + JSON.toJSONString(result) + "]");
        }
    }
}
