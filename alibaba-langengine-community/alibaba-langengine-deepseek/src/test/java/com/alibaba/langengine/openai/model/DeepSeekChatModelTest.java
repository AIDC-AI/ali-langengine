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
package com.alibaba.langengine.openai.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionParameter;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionProperty;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.outputparser.JsonOutputParser;
import com.alibaba.langengine.core.outputparser.MarkdownListOutputParser;
import com.alibaba.langengine.core.outputs.LLMResult;
import com.alibaba.langengine.core.prompt.ChatPromptValue;
import com.alibaba.langengine.core.prompt.PromptValue;
import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import com.alibaba.langengine.deepseek.model.DeepSeekChatModel;
import com.alibaba.langengine.deepseek.model.DeepSeekModelConstants;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.Lists;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.*;

public class DeepSeekChatModelTest {

    @Test
    public void test_run() {
        DeepSeekChatModel llm = new DeepSeekChatModel();
        System.out.println("response:" + llm.predict("9.11 and 9.8, which is greater?"));
    }

    @Test
    public void test_r1() {
        // success
        DeepSeekChatModel llm = new DeepSeekChatModel();
        llm.setModel(DeepSeekModelConstants.DEEPSEEK_REASONER);

        List<BaseMessage> messages = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("You are a helpful assistant.");
        messages.add(systemMessage);

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("9.11 and 9.8, which is greater?");
        messages.add(humanMessage);

        BaseMessage response = llm.run(messages);
        System.out.println("response:" + JSON.toJSONString(response));
    }

    @Test
    public void test_beta() {
        // success
        DeepSeekChatModel llm = new DeepSeekChatModel("https://api.deepseek.com/beta/");

        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("Please write quick sort code");
        messages.add(humanMessage);

        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent("```python\n");
        aiMessage.setPrefix(true);
        messages.add(aiMessage);

        BaseMessage response = llm.run(messages);
        System.out.println("response:" + JSON.toJSONString(response));
    }

    @Test
    public void test_predict_function_call() {
        DeepSeekChatModel llm = new DeepSeekChatModel();

        List<BaseMessage> messages = new ArrayList<>();

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("今天杭州天气怎么样？");
        messages.add(humanMessage);

        List<FunctionDefinition> functions = new ArrayList<>();
        FunctionDefinition functionDefinition = new FunctionDefinition();
        functionDefinition.setName("get_current_weather");
        functionDefinition.setDescription("Get the current weather in a given location.");
        FunctionParameter functionParameter = new FunctionParameter();
        functionParameter.setRequired(Arrays.asList(new String[] { "location" }));

        Map<String, FunctionProperty> propertyMap = new HashMap<>();

        FunctionProperty functionProperty = new FunctionProperty();
        functionProperty.setType("string");
        functionProperty.setDescription("The city and state, e.g. San Francisco, CA");
        propertyMap.put("location", functionProperty);

        functionProperty = new FunctionProperty();
        functionProperty.setType("string");
        List<String> enums = new ArrayList<>();
        enums.add("celsius");
        enums.add("fahrenheit");
        functionProperty.setEnums(enums);
        functionProperty.setDescription("The temperature unit.");
        propertyMap.put("unit", functionProperty);

        functionParameter.setProperties(propertyMap);
        functionDefinition.setParameters(functionParameter);
        functions.add(functionDefinition);

        BaseMessage baseMessage = llm.run(messages, functions, null, null, null);
        System.out.println("response:" + JSON.toJSONString(baseMessage));
    }

    @Test
    public void test_jsonMode() {
        // success
        DeepSeekChatModel llm = new DeepSeekChatModel();
        llm.setJsonMode(true);

        List<BaseMessage> messages = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("The user will provide some exam text. Please parse the \"question\" and \"answer\" and output them in JSON format. \n" +
                "\n" +
                "EXAMPLE INPUT: \n" +
                "Which is the highest mountain in the world? Mount Everest.\n" +
                "\n" +
                "EXAMPLE JSON OUTPUT:\n" +
                "{\n" +
                "    \"question\": \"Which is the highest mountain in the world?\",\n" +
                "    \"answer\": \"Mount Everest\"\n" +
                "}");
        messages.add(systemMessage);

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("Which is the longest river in the world? The Nile River.");
        messages.add(humanMessage);

        BaseMessage response = llm.run(messages);
        System.out.println("response:" + JSON.toJSONString(response));
    }

    @Test
    public void test_predict_streamTrue() {
        // success
        DeepSeekChatModel llm = new DeepSeekChatModel();
        llm.predict("你是谁？", null, e -> {
            System.out.println(e);
        });
    }
}
