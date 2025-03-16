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
package com.alibaba.langengine.openmanus.tool;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionDefinition;
import com.alibaba.langengine.core.model.fastchat.completion.chat.FunctionParameter;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;

import java.util.*;

public class ToolCollection {

    private List<BaseTool> tools;
    private Map<String, BaseTool> toolMap;

    public ToolCollection(BaseTool... tools) {
        this.tools = new ArrayList<>(Arrays.asList(tools));
        this.toolMap = new HashMap<>();
        for (BaseTool tool : tools) {
            toolMap.put(tool.getName(), tool);
        }
    }

    public Iterator<BaseTool> iterator() {
        return tools.iterator();
    }

    public List<FunctionDefinition> toParams() {
        List<FunctionDefinition> functions = new ArrayList<>();
        for (BaseTool tool : tools) {
            FunctionDefinition functionDefinition = new FunctionDefinition();
            functionDefinition.setName(tool.getName());
            functionDefinition.setDescription(tool.getDescription());
            FunctionParameter functionParameter = JSON.parseObject(tool.getParameters(), FunctionParameter.class);

            functionDefinition.setParameters(functionParameter);
            functions.add(functionDefinition);
        }
        return functions;
    }

    public ToolExecuteResult execute(String name, String toolInput) {
        BaseTool tool = toolMap.get(name);
        if (tool == null) {
            throw new RuntimeException("tool is null");
        }
        try {
            return tool.run(toolInput);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    public List<ToolExecuteResult> executeAll() {
        List<ToolExecuteResult> list = new ArrayList<>();
        for (BaseTool tool : tools) {
            try {
                list.add(tool.run(JSON.toJSONString(Collections.emptyMap())));
            } catch (Throwable e) {
                throw new RuntimeException(e);
            }
        }
        return list;
    }

    public BaseTool getTool(String name) {
        return toolMap.get(name);
    }

    public boolean hasTool(String name) {
        return toolMap.containsKey(name);
    }

    public ToolCollection addTool(BaseTool tool) {
        tools.add(tool);
        toolMap.put(tool.getName(), tool);
        return this;
    }

    public ToolCollection addTools(BaseTool... tools) {
        for (BaseTool tool : tools) {
            addTool(tool);
        }
        return this;
    }
}