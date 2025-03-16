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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCall;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCallFunction;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.openmanus.domain.AgentState;
import com.alibaba.langengine.openmanus.tool.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class ToolCallAgent extends ReActAgent {

    private static final String SYSTEM_PROMPT = "You are an agent that can execute tool calls, 并且你需要及时反思和复盘";
    private static final String NEXT_STEP_PROMPT = "If you want to stop interaction, use `terminate` tool/function call.";
    private static final Integer REPLY_MAX = 3;

    private static final String TOOL_CALL_REQUIRED = "Tool calls required but none provided";

    private ToolCollection availableTools = new ToolCollection(
            new CreateChatCompletion(),
            new Terminate()
    );

    private String toolChoices = "auto";
    private List<String> specialToolNames = new ArrayList<>();
    private List<ToolCall> toolCalls = new ArrayList<>();
    private int maxSteps = 30;
    private boolean inited = false;

    public ToolCallAgent() {
        this.specialToolNames.add(new Terminate().getName());

        setSystemPrompt(SYSTEM_PROMPT);
        setNextStepPrompt(NEXT_STEP_PROMPT);
    }

    @Override
    protected boolean think() {
        int retry = 0;
        return _think(retry);
    }

    private boolean _think(int retry) {
        try {
            toolCalls = new ArrayList<>();

            if (!inited && !StringUtils.isEmpty(getNextStepPrompt())) {
                HumanMessage humanMessage = new HumanMessage();
                humanMessage.setContent(getNextStepPrompt());
                getMessages().add(humanMessage);
                inited = true;
            }

            if(!(getMessages().get(0) instanceof SystemMessage)) {
                SystemMessage systemMessage = new SystemMessage();
                systemMessage.setContent(getSystemPrompt());
                getMessages().add(0, systemMessage);
            }

            List<FunctionDefinition> functions = availableTools.toParams();

            BaseMessage response = getLlm().run(getMessages(), functions, null, null, null);

            boolean hasToolCalls = (response.getAdditionalKwargs() != null
                    && (response.getAdditionalKwargs().get("tool_calls") != null
                    || response.getAdditionalKwargs().get("function_call") != null));

            if(hasToolCalls) {
                if(response.getAdditionalKwargs().get("tool_calls") != null) {
                    List<Map<String, Object>> mapList = (List<Map<String, Object>>) response.getAdditionalKwargs().get("tool_calls");
                    toolCalls = JSON.parseArray(JSON.toJSONString(mapList), ToolCall.class);
                } else {
                    Map<String, Object> map = (Map<String, Object>) response.getAdditionalKwargs().get("function_call");
                    ToolCall toolCall = new ToolCall();
                    toolCall.setType("function");
                    toolCall.setFunction(JSON.parseObject(JSON.toJSONString(map), ToolCallFunction.class));
                    toolCalls.add(toolCall);
                }
            }

            log.info(String.format("✨ %s's thoughts: %s", getName(), response.getContent()));
            log.info(String.format("🛠️ %s selected %d tools to use", getName(), toolCalls.size()));

            if (!toolCalls.isEmpty()) {
                log.info(String.format("🧰 Tools being prepared: %s", toolCalls.stream().map(call -> call.getFunction().getName())
                        .collect(Collectors.toList())));
            }

            if ("none".equals(toolChoices)) {
                if (!toolCalls.isEmpty()) {
                    log.warn(String.format("🤔 Hmm, %s tried to use tools when they weren't available!", getName()));
                }
                if (!response.getContent().isEmpty()) {
                    getMemory().getChatMemory().addAIMessage(response.getContent());
                    return true;
                }
                return false;
            }

            getMemory().getChatMemory().getMessages().add(response);

            if ("required".equals(toolChoices) && toolCalls.isEmpty()) {
                return true;
            }

            if ("auto".equals(toolChoices) && toolCalls.isEmpty()) {
                return !response.getContent().isEmpty();
            }

            return !toolCalls.isEmpty();
        } catch (Exception e) {
            log.error(String.format("🚨 Oops! The %s's thinking process hit a snag: %s", getName(), e.getMessage()));
            AIMessage aiMessage = new AIMessage();
            aiMessage.setContent(String.format("Error encountered while processing: %d - %s", retry, e.getMessage()));
            // 异常重试
            if(retry < REPLY_MAX) {
                return _think(retry + 1);
            } else {
                getMemory().getChatMemory().getMessages().add(aiMessage);
            }
            return false;
        }
    }

    @Override
    protected String act() {
        try {
            if (toolCalls.isEmpty()) {
                if ("required".equals(toolChoices)) {
                    throw new IllegalArgumentException(TOOL_CALL_REQUIRED);
                }
                return getMessages().get(getMessages().size() - 1).getContent();
            }

            List<String> results = new ArrayList<>();
            for (ToolCall command : toolCalls) {
                String result = executeTool(command);
                log.info(String.format("🎯 Tool '%s' completed its mission! Result: %s", command.getFunction().getName(), result));

                ToolMessage toolMessage = new ToolMessage();
                toolMessage.setTool_call_id(command.getId());
                toolMessage.setContent(result);
                toolMessage.setName(command.getFunction().getName());
                getMemory().getChatMemory().getMessages().add(toolMessage);

                results.add(result);
            }

            return String.join("\n\n", results);
        } catch (Exception e) {
            log.error(e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String executeTool(ToolCall command) {
        try {
            if (command == null || command.getFunction() == null || command.getFunction().getName().isEmpty()) {
                return "Error: Invalid command format";
            }

            String name = command.getFunction().getName();
            if (!availableTools.hasTool(name)) {
                return String.format("Error: Unknown tool '%s'", name);
            }

            String args = command.getFunction().getArguments() != null ? command.getFunction().getArguments() : "{}";
            log.info(String.format("🔧 Activating tool: '%s'...", name));
            ToolExecuteResult result = availableTools.execute(name, args);

            String observation = result != null
                    ? String.format("Observed output of cmd `%s` executed:\n%s", name, result)
                    : String.format("Cmd `%s` completed with no output", name);

            handleSpecialTool(name, result);

            return observation;
        } catch (Exception e) {
            log.error(String.format("⚠️ Tool '%s' encountered a problem: %s", command.getFunction().getName(), e.getMessage()));
            return "Error: " + e.getMessage();
        }
    }

    private void handleSpecialTool(String name, ToolExecuteResult result) {
        if (!isSpecialTool(name)) {
            return;
        }
        if (shouldFinishExecution(name, result)) {
            log.info(String.format("🏁 Special tool '%s' has completed the task!", name));
            setState(AgentState.FINISHED);
        }
    }

    private boolean shouldFinishExecution(String name, ToolExecuteResult result) {
        return true;
    }

    private boolean isSpecialTool(String name) {
        return specialToolNames.stream().anyMatch(n -> n.equalsIgnoreCase(name));
    }

    public ToolCollection getAvailableTools() {
        return availableTools;
    }

    public void setAvailableTools(ToolCollection availableTools) {
        this.availableTools = availableTools;
    }

    public String getToolChoices() {
        return toolChoices;
    }

    public void setToolChoices(String toolChoices) {
        this.toolChoices = toolChoices;
    }
}
