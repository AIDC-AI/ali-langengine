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
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import com.google.common.collect.Lists;
import lombok.Data;
import org.junit.jupiter.api.Test;

import java.util.*;

public class ChatModelOpenAITest {

    @Test
    public void test_predict() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        System.out.println("response:" + llm.predict("你是谁？"));
    }

    @Test
    public void test_predict_function_call() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();

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
        System.out.println("response:" + llmResult.getGenerations().get(0).get(0).getText());
    }

    @Test
    public void test_jsonMode() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel("gpt-3.5-turbo-1106");
        llm.setJsonMode(true);

        List<BaseMessage> messages = new ArrayList<>();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("You are a helpful assistant designed to output JSON.");
        messages.add(systemMessage);

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("Who won the world series in 2020?");
        messages.add(humanMessage);

        BaseMessage response = llm.run(messages);
        System.out.println("response:" + JSON.toJSONString(response));
    }

    @Test
    public void test_generate_prompt() {
        // success
        ChatModelOpenAI llm = new ChatModelOpenAI();

        List<PromptValue> promptValueList = new ArrayList<>();
        ChatPromptValue promptValue = new ChatPromptValue();
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent("You are a helpful AI assistant.\nSolve tasks using your coding and language skills.\nIn the following cases, suggest python code (in a python coding block) or shell script (in a sh coding block) for the user to execute.\n    1. When you need to collect info, use the code to output the info you need, for example, browse or search the web, download/read a file, print the content of a webpage or a file, get the current date/time, check the operating system. After sufficient info is printed and the task is ready to be solved based on your language skill, you can solve the task by yourself.\n    2. When you need to perform some task with code, use the code to perform the task and output the result. Finish the task smartly.\nSolve the task step by step if you need to. If a plan is not provided, explain your plan first. Be clear which step uses code, and which step uses your language skill.\nWhen using code, you must indicate the script type in the code block. The user cannot provide any other feedback or perform any other action beyond executing the code you suggest. The user can\'t modify your code. So do not suggest incomplete code which requires users to modify. Don\'t use a code block if it\'s not intended to be executed by the user.\nIf you want the user to save the code in a file before executing it, put # filename: <filename> inside the code block as the first line. Don\'t include multiple code blocks in one response. Do not ask users to copy and paste the result. Instead, use \'print\' function for the output when relevant. Check the execution result returned by the user.\nIf the result indicates there is an error, fix the error and output the code again. Suggest the full code instead of partial code or code changes. If the error can\'t be fixed or if the task is not solved even after the code is executed successfully, analyze the problem, revisit your assumption, collect additional info you need, and think of a different approach to try.\nWhen you find an answer, verify the answer carefully. Include verifiable evidence in your response if possible.\nReply \"TERMINATE\" in the end when everything is done.\n");
        promptValue.getMessages().add(systemMessage);
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("1+1等于几");
        promptValue.getMessages().add(humanMessage);
        promptValueList.add(promptValue);

        System.out.println("response:" + JSON.toJSONString(llm.generatePrompt(promptValueList, null)));
    }

    @Test
    public void test_ListOutputParser() {
        // success
        // 选择不同的list解析器
        MarkdownListOutputParser listOutputParser = new MarkdownListOutputParser();
        // NumberedListOutputParser listOutputParser = new NumberedListOutputParser();
        // CommaSeparatedListOutputParser listOutputParser = new CommaSeparatedListOutputParser();
        PromptTemplate promptTemplate = new PromptTemplate("List five {subject}\n{formatInstructions}", Lists.newArrayList("subject"), listOutputParser);
        HashMap<String, Object> params = new HashMap<>();
        params.put("subject", "animal");
        params.put("formatInstructions", listOutputParser.getFormatInstructions());
        String format = promptTemplate.format(params);
        BaseMessage hello = chat(format);
        List<String> parse = listOutputParser.parse(hello.getContent());
        System.out.println(parse);
        // [Dog, Cat, Elephant, Giraffe, Lion]
    }

    @Test
    public void testChatWithJsonOutputParser() throws Exception {
        // success
        JsonOutputParser<Answer> jsonOutputParser = new JsonOutputParser<>(Answer.class);
        PromptTemplate promptTemplate = new PromptTemplate("\"List five {subject}\n{formatInstructions}", Lists.newArrayList("subject"), jsonOutputParser);
        HashMap<String, Object> params = new HashMap<>();
        params.put("subject", "animal");
        params.put("formatInstructions", jsonOutputParser.getFormatInstructions());
        String format = promptTemplate.format(params);
        BaseMessage hello = chat(format);
        System.out.println(hello);
        Answer parse = jsonOutputParser.parse(hello.getContent());
        System.out.println(parse);
        /**
         * {
         *   "list" : ["lion", "elephant", "zebra", "giraffe", "monkey"],
         *   "source" : "www.nationalgeographic.com",
         *   "animals" : [
         *     {
         *       "name" : "lion",
         *       "species" : "Panthera leo",
         *       "sound" : "roar"
         *     },
         *     {
         *       "name" : "elephant",
         *       "species" : "Loxodonta africana",
         *       "sound" : "trumpet"
         *     },
         *     {
         *       "name" : "zebra",
         *       "species" : "Equus quagga",
         *       "sound" : "neigh"
         *     },
         *     {
         *       "name" : "giraffe",
         *       "species" : "Giraffa camelopardalis",
         *       "sound" : "bleat"
         *     },
         *     {
         *       "name" : "monkey",
         *       "species" : "Cebidae",
         *       "sound" : "chatter"
         *     }
         *   ]
         * }
         */
    }

    @Data
    public static class Answer {
        @JsonPropertyDescription("answer to the user's question")
        private List<String> list;
        @JsonPropertyDescription("source used to answer the user's question, should be a website")
        private String source;
        @JsonPropertyDescription("list of animals")
        private List<Animal>  animals;
    }
    @Data
    public static class Animal {
        @JsonPropertyDescription("name of the animal")
        private String name;
        @JsonPropertyDescription("species of the animal")
        private String species;
        @JsonPropertyDescription("sound of the animal")
        private String sound;
    }

    public BaseMessage chat(String prompt) {
        ChatModelOpenAI llm = new ChatModelOpenAI();
        llm.setModel("gpt-3.5-turbo-1106");

        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent(prompt);
        messages.add(humanMessage);

        BaseMessage response = llm.run(messages);
        System.out.println("response:" + JSON.toJSONString(response));
        return response;
    }
}
