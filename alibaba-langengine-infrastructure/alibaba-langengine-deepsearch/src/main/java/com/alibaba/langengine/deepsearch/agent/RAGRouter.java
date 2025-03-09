/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.deepsearch.agent;

import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.deepsearch.vectorstore.RAGAgentData;
import com.alibaba.langengine.deepsearch.vectorstore.RetrievalResultData;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class RAGRouter extends RAGAgent {
    private BaseChatModel llm;
    private List<RAGAgent> ragAgents;
    private List<String> agentDescriptions;

    private static final String RAG_ROUTER_PROMPT =
            "Given a list of agent indexes and corresponding descriptions, each agent has a specific function. \n" +
                    "Given a query, select only one agent that best matches the agent handling the query, and return the index without any other information.\n\n" +
                    "## Question\n%s\n\n" +
                    "## Agent Indexes and Descriptions\n%s\n\n" +
                    "Only return one agent index number that best matches the agent handling the query:\n";

    public RAGRouter(BaseChatModel llm, List<RAGAgent> ragAgents) {
        this.llm = llm;
        this.ragAgents = ragAgents;
        this.agentDescriptions = ragAgents.stream()
                        .map(agent -> agent.getDescription())
                        .collect(Collectors.toList());
    }

    private RAGAgentData route(String query) {
        StringBuilder descriptionBuilder = new StringBuilder();
        for (int i = 0; i < agentDescriptions.size(); i++) {
            descriptionBuilder.append(String.format("[%d]: %s\n", i + 1, agentDescriptions.get(i)));
        }

        String prompt = String.format(RAG_ROUTER_PROMPT, query, descriptionBuilder);
        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent(prompt);
        messages.add(humanMessage);
        BaseMessage chatResponse = llm.run(messages);
        int selectedAgentIndex;
        try {
            selectedAgentIndex = Integer.parseInt(chatResponse.getContent().trim()) - 1;
        } catch (NumberFormatException e) {
            log.warn("Parse int failed in RAGRouter, but will try to find the last digit as fallback.");
            selectedAgentIndex = Integer.parseInt(findLastDigit(chatResponse.getContent())) - 1;
        }

        RAGAgent selectedAgent = ragAgents.get(selectedAgentIndex);
        log.info(String.format(" Select agent [%s] to answer the query [%s] \n",
                selectedAgent.getClass().getName(), query));

        RAGAgentData ragAgentData = new RAGAgentData(selectedAgent, chatResponse.getTotalTokens());
        return ragAgentData;
    }

    @Override
    public String getDescription() {
        return "";
    }

    public RetrievalResultData retrieve(String query, Map<String, Object> kwargs) {
        RAGAgentData ragAgentData = route(query);
        RetrievalResultData retrievalResultData = ragAgentData.getAgent().retrieve(query, kwargs);
        return new RetrievalResultData(retrievalResultData.getDocuments(),
                retrievalResultData.getConsumeTokens() + ragAgentData.getTotalToken());
    }

    public RetrievalResultData query(String query, Map<String, Object> kwargs) {
        RAGAgentData ragAgentData = route(query);
        RetrievalResultData queryResult = ragAgentData.getAgent().query(query, kwargs);
        return new RetrievalResultData(queryResult.getAnswer(),
                queryResult.getDocuments(),
                queryResult.getConsumeTokens() + ragAgentData.getTotalToken());
    }

    private String findLastDigit(String string) {
        for (int i = string.length() - 1; i >= 0; i--) {
            if (Character.isDigit(string.charAt(i))) {
                return String.valueOf(string.charAt(i));
            }
        }
        throw new IllegalArgumentException("No digit found in the string");
    }
}
