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
package com.alibaba.langengine.openmanus.agent;

import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.memory.BaseChatMemory;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.openmanus.domain.AgentState;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public abstract class BaseAgent {
    private final ReentrantLock lock = new ReentrantLock();

    private String name = "Unique name of the agent";
    private String description = "Optional agent description";

    private String systemPrompt = "System-level instruction prompt";
    private String nextStepPrompt = "Prompt for determining next action";

    private BaseChatModel llm;
    private BaseChatMemory memory;
    private AgentState state = AgentState.IDLE;

    private int maxSteps = 3;
    private int currentStep = 0;

    private int duplicateThreshold = 2;

    public void updateMemory(String role, String content, Map<String, Object> kwargs) {
        switch (role) {
            case "user":
                memory.getChatMemory().addUserMessage(content);
                break;
            case "system":
                memory.getChatMemory().addSystemMessage(content);
                break;
            case "assistant":
                memory.getChatMemory().addAIMessage(content);
                break;
            case "tool":
                memory.getChatMemory().addToolMessage(content);
                break;
            default:
                throw new IllegalArgumentException("Unsupported message role: " + role);
        }
    }

    public String run(String request) {
        currentStep = 0;
        if (state != AgentState.IDLE) {
            throw new IllegalStateException("Cannot run agent from state: " + state);
        }

        updateMemory("user", request, new HashMap<>());

        List<String> results = new ArrayList<>();
        lock.lock();
        try {
            state = AgentState.RUNNING;
            while (currentStep < maxSteps && !state.equals(AgentState.FINISHED)) {
                currentStep++;
                log.info("Executing step " + currentStep + "/" + maxSteps);
                String stepResult = step();
                if (isStuck()) {
                    handleStuckState();
                }
                results.add("Step " + currentStep + ": " + stepResult);
            }
            if (currentStep >= maxSteps) {
                results.add("Terminated: Reached max steps (" + maxSteps + ")");
            }
        } finally {
            lock.unlock();
            state = AgentState.IDLE;  // Reset state after execution
        }
        return String.join("\n", results);
    }

    protected abstract String step();

    private void handleStuckState() {
        String stuckPrompt = "Observed duplicate responses. Consider new strategies and avoid repeating ineffective paths already attempted.";
        nextStepPrompt = stuckPrompt + "\n" + nextStepPrompt;
        log.warn("Agent detected stuck state. Added prompt: " + stuckPrompt);
    }

    private boolean isStuck() {
        List<BaseMessage> messages = memory.getChatMemory().getMessages();
        if (messages.size() < 2) {
            return false;
        }

        BaseMessage lastMessage = messages.get(messages.size() - 1);
        if (lastMessage.getContent() == null || lastMessage.getContent().isEmpty()) {
            return false;
        }
        int duplicateCount = 0;
        for (int i = messages.size() - 2; i >= 0; i--) {
            BaseMessage msg = messages.get(i);
            if ("ai".equals(msg.getType()) && lastMessage.getContent() != null && lastMessage.getContent().equals(msg.getContent())) {
                duplicateCount++;
            }
        }

        return duplicateCount >= duplicateThreshold;
    }

    public List<BaseMessage> getMessages() {
        return memory.getChatMemory().getMessages();
    }

    public void setMessages(List<BaseMessage> messages) {
        memory.getChatMemory().setMessages(messages);
    }

    public int getMaxSteps() {
        return maxSteps;
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
    }

    public int getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(int currentStep) {
        this.currentStep = currentStep;
    }

    public String getNextStepPrompt() {
        return nextStepPrompt;
    }

    public void setNextStepPrompt(String nextStepPrompt) {
        this.nextStepPrompt = nextStepPrompt;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BaseChatModel getLlm() {
        return llm;
    }

    public void setLlm(BaseChatModel llm) {
        this.llm = llm;
    }

    public BaseChatMemory getMemory() {
        return memory;
    }

    public void setMemory(BaseChatMemory memory) {
        this.memory = memory;
    }

    public AgentState getState() {
        return state;
    }

    public void setState(AgentState state) {
        this.state = state;
    }

    public int getDuplicateThreshold() {
        return duplicateThreshold;
    }

    public void setDuplicateThreshold(int duplicateThreshold) {
        this.duplicateThreshold = duplicateThreshold;
    }
}
