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

import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.openmanus.OpenManusConfiguration;
import com.alibaba.langengine.openmanus.tool.*;

import java.util.Iterator;

public class ManusAgent extends ToolCallAgent {

    private static final String SYSTEM_PROMPT = "You are OpenManus, an all-capable AI assistant, aimed at solving any task presented by the user. You have various tools at your disposal that you can call upon to efficiently complete complex requests. Whether it's programming, information retrieval, file processing, or web browsing, you can handle it all.";
    private static final String NEXT_STEP_PROMPT = "You can interact with the computer using PythonExecute, save important content and information files through FileSaver, open browsers with BrowserUseTool, and retrieve information using GoogleSearch.\n" +
            "\n" +
            "PythonExecute: Execute Python code to interact with the computer system, data processing, automation tasks, etc.\n" +
            "\n" +
            "FileSaver: Save files locally, such as txt, py, html, etc.\n" +
            "\n" +
            "BrowserUseTool: Open, browse, and use web browsers.If you open a local HTML file, you must provide the absolute path to the file.\n" +
            "\n" +
            "GoogleSearch: Perform web information retrieval\n" +
            "\n" +
            "Based on user needs, proactively select the most appropriate tool or combination of tools. For complex tasks, you can break down the problem and use different tools step by step to solve it. After using each tool, clearly explain the execution results and suggest the next steps.";

    private String name = "Manus";
    private String description = "A versatile agent that can solve various tasks using multiple tools";

    public ManusAgent() {
        setLlm(OpenManusConfiguration.getManusChatModel());

        setMemory(new ConversationBufferMemory());

        setSystemPrompt(SYSTEM_PROMPT);
        setNextStepPrompt(NEXT_STEP_PROMPT);

        setAvailableTools(new ToolCollection(
                new PythonExecute(),
                new GoogleSearch(),
                new BrowserUseTool(),
                new FileSaver(),
                new Terminate()
        ));
    }

    public void addTool(BaseTool tool) {
        getAvailableTools().addTool(tool);
    }

    public void removeTool(String name) {
        Iterator<BaseTool> iterator =  getAvailableTools().iterator();
        while (iterator.hasNext()) {
            BaseTool tool = iterator.next();
            if(name.equals(tool.getName())) {
                iterator.remove();
            }
        }

        setAvailableTools(getAvailableTools());
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }
}
