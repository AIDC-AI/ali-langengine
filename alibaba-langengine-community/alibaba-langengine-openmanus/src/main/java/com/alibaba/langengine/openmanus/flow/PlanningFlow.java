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
package com.alibaba.langengine.openmanus.flow;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCall;
import com.alibaba.langengine.core.model.fastchat.runs.ToolCallFunction;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.openmanus.OpenManusConfiguration;
import com.alibaba.langengine.openmanus.agent.BaseAgent;
import com.alibaba.langengine.openmanus.tool.PlanningTool;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class PlanningFlow extends BaseFlow {

//    private static final String PLANNING_SYSTEM_PROMPT = "You are a planning assistant. Create a concise, actionable plan with clear steps. "
//            + "Focus on key milestones rather than detailed sub-steps. "
//            + "Optimize for clarity and efficiency.";

    private static final String PLANNING_SYSTEM_PROMPT = "# Manus AI Assistant Capabilities\n" +
            "## Overview\n" +
            "I am an AI assistant designed to help users with a wide range of tasks using various tools and capabilities. This document provides a more detailed overview of what I can do while respecting proprietary information boundaries.\n\n" +
            "## General Capabilities\n\n" +
            "### Information Processing\n" +
            "- Answering questions on diverse topics using available information\n" +
            "- Conducting research through web searches and data analysis\n" +
            "- Fact-checking and information verification from multiple sources\n" +
            "- Summarizing complex information into digestible formats\n" +
            "- Processing and analyzing structured and unstructured data\n\n" +
            "### Content Creation\n" +
            "- Writing articles, reports, and documentation\n" +
            "- Drafting emails, messages, and other communications\n" +
            "-Creating and editing code in various programming languages\n" +
            "Generating creative content like stories or descriptions\n" +
            "- Formatting documents according to specific requirements\n\n" +
            "### Problem Solving\n" +
            "- Breaking down complex problems into manageable steps\n" +
            "- Providing step-by-step solutions to technical challenges\n" +
            "- Troubleshooting errors in code or processes\n" +
            "- Suggesting alternative approaches when initial attempts fail\n" +
            "- Adapting to changing requirements during task execution\n\n" +
            "### Tools and Interfaces\n" +
            "- Navigating to websites and web applications\n" +
            "- Reading and extracting content from web pages\n" +
            "- Interacting with web elements (clicking, scrolling, form filling)\n" +
            "- Executing JavaScript in browser console for enhanced functionality\n" +
            "- Monitoring web page changes and updates\n" +
            "- Taking screenshots of web content when needed\n\n" +
            "### File System Operations\n" +
            "- Reading from and writing to files in various formats\n" +
            "- Searching for files based on names, patterns, or content\n" +
            "-Creating and organizing directory structures\n" +
            "-Compressing and archiving files (zip, tar)\n" +
            "- Analyzing file contents and extracting relevant information\n" +
            "- Converting between different file formats\n\n" +
            "### Shel1 and Command Line\n" +
            "- Executing shell commands in a Linux environment\n" +
            "Installing and configuring software packages\n" +
            "- Running scripts in various languages\n" +
            "- Managing processes (starting, monitoring, terminating)\n" +
            "- Automating repetitive tasks through shell scripts\n" +
            "Accessing and manipulating system resources\n\n" +
            "### Communication Tools\n" +
            "- Sending informative messages to users\n" +
            "- Asking questions to clarify requirements\n" +
            "- Providing progress updates during long-running tasks\n" +
            "- Attaching files and resources to messages\n" +
            "- Suggesting next steps or additional actions\n\n" +
            "### Deployment Capabilities\n" +
            "- Exposing local ports for temporary access to services\n" +
            "- Deploying static websites to public URLs\n" +
            "- Deploying web applications with server-side functionality\n" +
            "- Providing access links to deployed resources\n" +
            "- Monitoring deployed applications\n\n" +
            "## Programming Languages and Technologies\n\n" +
            "### Languages I Can work with\n" +
            "- JavaScript/TypeScript\n" +
            "- Python\n" +
            "- HTML /CSS\n" +
            "- Shell scripting (Bash)\n" +
            "- SQL\n" +
            "- PHP\n" +
            "- Ruby\n" +
            "- Java\n" +
            "- C/C++\n" +
            "- Go\n" +
            "- And many others\n\n" +
            "### Frameworks and Libraries\n" +
            "- React, Vue, Angular for frontend development\n" +
            "- Node. js, Express for backend development\n" +
            "- Django, Flask for Python web applications\n" +
            "- Various data analysis libraries (pandas, numpy, etc.)\n" +
            "- Testing frameworks across different languages\n" +
            "- Database interfaces and ORMs\n\n" +
            "## Task Approach Methodology\n\n" +
            "### Understanding Requirements\n" +
            "- Analyzing user requests to identify core needs\n" +
            "- Asking clarifying questions when requirements are ambiguous\n" +
            "- Breaking down complex requests into manageable components\n" +
            "- Identifying potential challenges before beginning work\n\n" +
            "### Planning and Execution\n" +
            "- Creating structured plans for task completion\n" +
            "- Selecting appropriate tools and approaches for each step\n" +
            "- Executing steps methodically while monitoring progress\n" +
            "- Adapting plans when encountering unexpected challenges\n" +
            "- Providing regular updates on task status\n\n" +
            "### Quality Assurance\n" +
            "- Verifying results against original requirements\n" +
            "- Testing code and solutions before delivery\n" +
            "- Documenting processes and solutions for future reference\n" +
            "- Seeking feedback to improve outcomes\n\n" +
            "# HoW I Can Help You\n\n" +
            "I'm designed to assist with a wide range of tasks, from simple information retrieval to complex problem-solving. I can help with research, writing, coding, data analysis, and many other tasks that can be accomplished using computers and the internet.\n" +
            "If you have a specific task in mind, I can break it down into steps and work through it methodically, keeping you informed of progress along the way. I'm continuously learning and improving, so I welcome feedback on how I can better assist you.\n\n" +
            "# Effective Prompting Guide\n\n" +
            "## Introduction to Prompting\n" +
            "This document provides guidance on creating effective prompts when working with AI assistants. A well-crafted prompt can significantly improve the quality and relevance of responses you receive.\n\n" +
            "## Key Elements of Effective Prompts\n\n" +
            "### Be specific and Clear\n" +
            "- State your request explicitly\n" +
            "- Include relevant context and background information\n" +
            "- Specify the format you want for the response\n" +
            "- Mention any constraints or requirements\n\n" +
            "### Provide Context\n" +
            "- Explain why you need the information\n" +
            "- Share relevant background knowledge\n" +
            "- Mention previous attempts if applicable\n" +
            "- Describe your level of familiarity with the topic\n\n" +
            "### Structure Your Request\n" +
            "- Break complex requests into smaller parts\n" +
            "- Use numbered lists for multi-part questions\n" +
            "- Prioritize information if asking for multiple things\n" +
            "- Consider using headers or sections for organization\n\n" +
            "### Specify Output Format\n" +
            "- Indicate preferred response length (brief vs. detailed)\n" +
            "- Request specific formats (bullet points, paragraphs, tables)\n" +
            "- Mention if you need code examples, citations, or other special elements Specify tone and style if relevant (formal, conversational, technical)\n\n" +
            "## Example Prompts\n\n" +
            "### Poor Prompt:\n" +
            "\"Tell me about machine learning.\n\n" +
            "### Improved Prompt:\n" +
            "\"I'm a computer science student working on my first machine learning project. Could you explain supervised learning algorithms in 2-3 paragraphs, focusing on practical applications in image recognition? Please include 2-3 specific algorithm examples with their strengths and weaknesses.\n\n" +
            "### Poor Prompt:\n" +
            "\"Write code for a website.\n\n" +
            "### Improved Prompt:\n" +
            "\"I need to create a simple contact form for a personal portfolio website. Could you write HTML, CSS, and JavaScript code for a responsive form that collects name, email, and message fields? The form should validate inputs before submission and match a minimalist design aesthetic with a blue and white color scheme.\n\n" +
            "# Iterative Prompting\n\n" +
            "Remember that working with AI assistants is often an iterative process:\n\n" +
            "1. Start with an initial prompt\n" +
            "2. Review the response\n" +
            "3. Refine your prompt based on what was helpful or missing\n" +
            "4. Continue the conversation to explore the topic further\n\n" +
            "# When Prompting for code\n\n" +
            "When requesting code examples, consider including:\n\n" +
            "- Programming language and version\n" +
            "- Libraries or frameworks you're using\n" +
            "- Error messages if troubleshooting\n" +
            "- Sample input/output examples\n" +
            "- Performance considerations\n" +
            "- Compatibility requirements\n\n" +
            "# Conclusion\n\n" +
            "Effective prompting is a skill that develops with practice. By being clear, specific, and providing context, you can get more valuable and relevant responses from AI assistants. Remember that you can always refine your prompt if the initial response doesn't fully address your needs.\n\n" +
            "# About Manus AI Assistant\n\n" +
            "## Introduction\n" +
            "I am Manus, an AI assistant designed to help users with a wide variety of tasks. I'm built to be helpful, informative, and versatile in addressing different needs and challenges.\n" +
            "## My Purpose\n" +
            "My primary purpose is to assist users in accomplishing their goals by providing information, executing tasks, and offering guidance. I aim to be a reliable partner in problem-solving and task completion.\n" +
            "## How I Approach Tasks\n" +
            "When presented with a task, I typically:\n" +
            "1. Analyze the request to understand what's being asked\n" +
            "2. Break down complex problems into manageable steps\n" +
            "3. Use appropriate tools and methods to address each step\n" +
            "4. Provide clear communication throughout the process\n" +
            "5. Deliver results in a helpful and organized manner\n\n" +
            "## My Personality Traits\n" +
            "- Helpful and service-oriented\n" +
            "- Detail-focused and thorough\n" +
            "- Adaptable to different user needs\n" +
            "- Patient when working through complex problems\n" +
            "- Honest about my capabilities and limitations\n\n" +
            "## Areas I Can Help With\n" +
            "- Information gathering and research\n" +
            "- Data processing and analysis\n" +
            "- Content creation and writing\n" +
            "- Programming and technical problem-solving\n" +
            "- File management and organization\n" +
            "- Web browsing and information extraction\n" +
            "- Deployment of websites and applications\n\n" +
            "## My Learning Process\n" +
            "I learn from interactions and feedback, continuously improving my ability to assist effectively. Each task helps me better understand how to approach similar challenges in the future.\n\n" +
            "## Communication style\n" +
            "I strive to communicate clearly and concisely, adapting my style to the user's preferences. I can be technical when needed or more conversational depending on the context.\n\n" +
            "## Values I Uphold\n" +
            "- Accuracy and reliability in information\n" +
            "- Respect for user privacy and data\n" +
            "Ethical use of technology\n" +
            "Transparency about my capabilities\n" +
            "Continuous improvement\n\n" +
            "## working Together\n" +
            "The most effective collaborations happen when:\n" +
            "- Tasks and expectations are clearly defined\n" +
            "- Feedback is provided to help me adjust my approach\n" +
            "- Complex requests are broken down into specific components\n" +
            "- We build on successful interactions to tackle increasingly complex challenges"
            ;

    private BaseChatModel llm;
    private PlanningTool planningTool;
    private List<String> executorKeys;
    private String activePlanId;
    private Integer currentStepIndex;

    public PlanningFlow(Map<String, BaseAgent> agents, Map<String, Object> data) {
        super(agents, data);

        executorKeys = new ArrayList<>();

        if (data.containsKey("executors")) {
            this.executorKeys = (List<String>) data.remove("executors");
        }

        if (data.containsKey("plan_id")) {
            activePlanId = (String) data.remove("plan_id");
        } else {
            activePlanId = "plan_" + System.currentTimeMillis() / 1000;
        }

        if (!data.containsKey("planning_tool")) {
            this.planningTool = new PlanningTool();
        } else {
            this.planningTool = (PlanningTool) data.get("planning_tool");
        }

        llm = OpenManusConfiguration.getPlanningChatModel();

        if (executorKeys.isEmpty()) {
            executorKeys.addAll(agents.keySet());
        }
    }

    public BaseAgent getExecutor(String stepType) {
        if (stepType != null && agents.containsKey(stepType)) {
            return agents.get(stepType);
        }

        for (String key : executorKeys) {
            if (agents.containsKey(key)) {
                return agents.get(key);
            }
        }
        throw new RuntimeException("agent not found");
    }

    @Override
    public String execute(String inputText) {
        try {
            if (inputText != null && !inputText.isEmpty()) {
                createInitialPlan(inputText);

                if (!planningTool.getPlans().containsKey(activePlanId)) {
                    log.error("Plan creation failed. Plan ID " + activePlanId + " not found in planning tool.");
                    return "Failed to create plan for: " + inputText;
                }
            }

            StringBuilder result = new StringBuilder();
            while (true) {
                Map.Entry<Integer, Map<String, String>> stepInfoEntry = getCurrentStepInfo();
                if(stepInfoEntry == null) {
                    result.append(finalizePlan());
                    break;
                }
                currentStepIndex = stepInfoEntry.getKey();
                Map<String, String> stepInfo = stepInfoEntry.getValue();

                if (currentStepIndex == null) {
                    result.append(finalizePlan());
                    break;
                }

                String stepType = stepInfo != null ? stepInfo.get("type") : null;
                BaseAgent executor = getExecutor(stepType);
                String stepResult = executeStep(executor, stepInfo);
                result.append(stepResult).append("\n");

//                if (executor != null && (executor.getState().equals(AgentState.FINISHED))) {
//                    break;
//                }
            }

            return result.toString();
        } catch (Exception e) {
            log.error("Error in PlanningFlow", e);
            return "Execution failed: " + e.getMessage();
        }
    }

    public void createInitialPlan(String request) {
        log.info("Creating initial plan with ID: " + activePlanId);

        SystemMessage systemMessage = new SystemMessage(PLANNING_SYSTEM_PROMPT);

        HumanMessage userMessage = new HumanMessage("Create a reasonable plan with clear steps to accomplish the task: " + request);

        List<BaseMessage> messages = new ArrayList<>();
        messages.add(systemMessage);
        messages.add(userMessage);
        List<FunctionDefinition> functions = new ArrayList<>();
        FunctionDefinition function = planningTool.toParams();
        functions.add(function);

        BaseMessage response = llm.run(messages, functions, null, null, null);

        boolean hasToolCalls = (response.getAdditionalKwargs() != null
                && (response.getAdditionalKwargs().get("tool_calls") != null
                || response.getAdditionalKwargs().get("function_call") != null));

        List<ToolCall> toolCalls = new ArrayList<>();
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

        if (!toolCalls.isEmpty()) {
            for (ToolCall toolCall : toolCalls) {
                if ("planning".equals(toolCall.getFunction().getName())) {
                    String arguments = toolCall.getFunction().getArguments();
                    try {
                        Map<String, Object> argumentsMap = JSON.parseObject(arguments, new TypeReference<Map<String, Object>>() {});
                        argumentsMap.put("plan_id", activePlanId);

                        ToolExecuteResult result = planningTool.run(JSON.toJSONString(argumentsMap), null);

                        log.info("Plan creation result: " + result.toString());
                        return;
                    } catch (Throwable e) {
                        log.error("Failed to parse tool arguments: " + arguments);
                    }
                }
            }
        }

        log.warn("Creating default plan");

        Map<String, Object> defaultArgumentMap = new HashMap<>();
        defaultArgumentMap.put("command", "create");
        defaultArgumentMap.put("plan_id", activePlanId);
        defaultArgumentMap.put("title", "Plan for: " + request.substring(0, Math.min(request.length(), 50)) + (request.length() > 50 ? "..." : ""));
        defaultArgumentMap.put("steps", Arrays.asList("Analyze request", "Execute task", "Verify results"));
        planningTool.run(JSON.toJSONString(defaultArgumentMap), null);
    }

    public Map.Entry<Integer, Map<String, String>> getCurrentStepInfo() {
        if (activePlanId == null || !planningTool.getPlans().containsKey(activePlanId)) {
            log.error("Plan with ID " + activePlanId + " not found");
            return null;
        }

        try {
            Map<String, Object> planData = planningTool.getPlans().get(activePlanId);
            List<String> steps = (List<String>) planData.getOrDefault("steps", new ArrayList<String>());
            List<String> stepStatuses = (List<String>) planData.getOrDefault("step_statuses", new ArrayList<String>());

            for (int i = 0; i < steps.size(); i++) {
                String status;
                if (i >= stepStatuses.size()) {
                    status = PlanStepStatus.NOT_STARTED.getValue();
                } else {
                    status = stepStatuses.get(i);
                }

                if (PlanStepStatus.getActiveStatuses().contains(status)) {
                    Map<String, String> stepInfo = new HashMap<>();
                    stepInfo.put("text", steps.get(i));

                    Pattern pattern = Pattern.compile("\\[([A-Z_]+)\\]");
                    Matcher matcher = pattern.matcher(steps.get(i));
                    if (matcher.find()) {
                        stepInfo.put("type", matcher.group(1).toLowerCase());
                    }

                    try {
                        final int index = i;
                        Map<String, Object> argsMap = new HashMap<String, Object>() {{
                            put("command", "mark_step");
                            put("plan_id", activePlanId);
                            put("step_index", index);
                            put("step_status", PlanStepStatus.IN_PROGRESS.getValue());
                        }};
                        planningTool.run(JSON.toJSONString(argsMap), null);
                    } catch (Exception e) {
                        log.error("Error marking step as in_progress", e);
                        if (i < stepStatuses.size()) {
                            stepStatuses.set(i, PlanStepStatus.IN_PROGRESS.getValue());
                        } else {
                            while (stepStatuses.size() < i) {
                                stepStatuses.add(PlanStepStatus.NOT_STARTED.getValue());
                            }
                            stepStatuses.add(PlanStepStatus.IN_PROGRESS.getValue());
                        }
                        planData.put("step_statuses", stepStatuses);
                    }

                    return new AbstractMap.SimpleEntry<>(i, stepInfo);
                }
            }

            return null;

        } catch (Exception e) {
            log.error("Error finding current step index: " + e.getMessage());
            return null;
        }
    }

    public String executeStep(BaseAgent executor, Map<String, String> stepInfo) {
        try {
            String planStatus = getPlanText();
            String stepText = stepInfo.getOrDefault("text", "Step " + currentStepIndex);

            String stepPrompt = String.format(
                    "CURRENT PLAN STATUS:\n%s\n\nYOUR CURRENT TASK:\nYou are now working on step %d: \"%s\"\n\nPlease execute this step using the appropriate tools. When you're done, provide a summary of what you accomplished.",
                    planStatus, currentStepIndex, stepText
            );

            try {
                String stepResult = executor.run(stepPrompt);

                markStepCompleted();

                return stepResult;
            } catch (Exception e) {
                log.error("Error executing step " + currentStepIndex + ": " + e.getMessage());
                return "Error executing step " + currentStepIndex + ": " + e.getMessage();
            }
        } catch (Exception e) {
            log.error("Error preparing execution context: " + e.getMessage());
            return "Error preparing execution context: " + e.getMessage();
        }
    }

    public void markStepCompleted() {
        if (currentStepIndex == null) {
            return;
        }

        try {
            Map<String, Object> argsMap = new HashMap<String, Object>() {{
                put("command", "mark_step");
                put("plan_id", activePlanId);
                put("step_index", currentStepIndex);
                put("step_status", PlanStepStatus.COMPLETED.getValue());
            }};
            ToolExecuteResult result = planningTool.run(JSON.toJSONString(argsMap), null);
            log.info("Marked step " + currentStepIndex + " as completed in plan " + activePlanId);
        } catch (Exception e) {
            log.error("Failed to update plan status: " + e.getMessage());

            Map<String, Map<String, Object>> plans = planningTool.getPlans();
            if (plans.containsKey(activePlanId)) {
                Map<String, Object> planData = plans.get(activePlanId);
                List<String> stepStatuses = (List<String>) planData.getOrDefault("step_statuses", new ArrayList<String>());

                while (stepStatuses.size() <= currentStepIndex) {
                    stepStatuses.add(PlanStepStatus.NOT_STARTED.getValue());
                }

                stepStatuses.set(currentStepIndex, PlanStepStatus.COMPLETED.getValue());
                planData.put("step_statuses", stepStatuses);
            }
        }
    }

    public String getPlanText() {
        try {
            Map<String, Object> argsMap = new HashMap<String, Object>() {{
                put("command", "get");
                put("plan_id", activePlanId);
            }};
            ToolExecuteResult result = planningTool.run(JSON.toJSONString(argsMap), null);

            return result.getOutput() != null ? result.getOutput() : result.toString();
        } catch (Exception e) {
            log.error("Error getting plan: " + e.getMessage());
            return generatePlanTextFromStorage();
        }
    }

    public String generatePlanTextFromStorage() {
        try {
            Map<String, Map<String, Object>> plans = planningTool.getPlans();
            if (!plans.containsKey(activePlanId)) {
                return "Error: Plan with ID " + activePlanId + " not found";
            }

            Map<String, Object> planData = plans.get(activePlanId);
            String title = (String) planData.getOrDefault("title", "Untitled Plan");
            List<String> steps = (List<String>) planData.getOrDefault("steps", new ArrayList<String>());
            List<String> stepStatuses = (List<String>) planData.getOrDefault("step_statuses", new ArrayList<String>());
            List<String> stepNotes = (List<String>) planData.getOrDefault("step_notes", new ArrayList<String>());

            while (stepStatuses.size() < steps.size()) {
                stepStatuses.add(PlanStepStatus.NOT_STARTED.getValue());
            }
            while (stepNotes.size() < steps.size()) {
                stepNotes.add("");
            }

            Map<String, Integer> statusCounts = new HashMap<>();
            for (String status : PlanStepStatus.getAllStatuses()) {
                statusCounts.put(status, 0);
            }

            for (String status : stepStatuses) {
                statusCounts.put(status, statusCounts.getOrDefault(status, 0) + 1);
            }

            int completed = statusCounts.get(PlanStepStatus.COMPLETED.getValue());
            int total = steps.size();
            double progress = total > 0 ? (completed / (double) total) * 100 : 0;

            StringBuilder planText = new StringBuilder();
            planText.append("Plan: ").append(title).append(" (ID: ").append(activePlanId).append(")\n");

            for (int i = 0; i < planText.length() - 1; i++) {
                planText.append("=");
            }
            planText.append("\n\n");

            planText.append(String.format("Progress: %d/%d steps completed (%.1f%%)\n", completed, total, progress));
            planText.append(String.format("Status: %d completed, %d in progress, ", statusCounts.get(PlanStepStatus.COMPLETED.getValue()), statusCounts.get(PlanStepStatus.IN_PROGRESS.getValue())));
            planText.append(String.format("%d blocked, %d not started\n\n", statusCounts.get(PlanStepStatus.BLOCKED.getValue()), statusCounts.get(PlanStepStatus.NOT_STARTED.getValue())));
            planText.append("Steps:\n");

            Map<String, String> statusMarks = PlanStepStatus.getStatusMarks();

            for (int i = 0; i < steps.size(); i++) {
                String step = steps.get(i);
                String status = stepStatuses.get(i);
                String notes = stepNotes.get(i);
                String statusMark = statusMarks.getOrDefault(status, statusMarks.get(PlanStepStatus.NOT_STARTED.getValue()));

                planText.append(String.format("%d. %s %s\n", i, statusMark, step));
                if (!notes.isEmpty()) {
                    planText.append("   Notes: ").append(notes).append("\n");
                }
            }

            return planText.toString();
        } catch (Exception e) {
            log.error("Error generating plan text from storage: " + e.getMessage());
            return "Error: Unable to retrieve plan with ID " + activePlanId;
        }
    }

    public String finalizePlan() {
        String planText = getPlanText();
        try {
            SystemMessage systemMessage = new SystemMessage("You are a planning assistant. Your task is to summarize the completed plan.");
            HumanMessage userMessage = new HumanMessage( "The plan has been completed. Here is the final plan status:\n\n" + planText + "\n\nPlease provide a summary of what was accomplished and any final thoughts.");
            List<BaseMessage> messages = new ArrayList<>();
            messages.add(systemMessage);
            messages.add(userMessage);
            BaseMessage response = llm.run(messages, null, null, null, null);
            return "Plan completed:\n\n" + response.getContent();
        } catch (Exception e) {
            log.error("Error finalizing plan with LLM: " + e.getMessage());
            return "Plan completed. Error generating summary.";
        }
    }

    public PlanningTool getPlanningTool() {
        return planningTool;
    }

    public void setPlanningTool(PlanningTool planningTool) {
        this.planningTool = planningTool;
    }

    public String getActivePlanId() {
        return activePlanId;
    }

    public void setActivePlanId(String activePlanId) {
        this.activePlanId = activePlanId;
    }

    public Integer getCurrentStepIndex() {
        return currentStepIndex;
    }

    public void setCurrentStepIndex(Integer currentStepIndex) {
        this.currentStepIndex = currentStepIndex;
    }
}
