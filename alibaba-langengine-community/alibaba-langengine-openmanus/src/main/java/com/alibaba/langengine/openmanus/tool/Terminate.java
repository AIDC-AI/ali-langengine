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
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Terminate extends BaseTool {

    private String PARAMETERS = "{\n" +
            "        \"type\": \"object\",\n" +
            "        \"properties\": {\n" +
            "            \"status\": {\n" +
            "                \"type\": \"string\",\n" +
            "                \"description\": \"The finish status of the interaction.\",\n" +
            "                \"enum\": [\"success\", \"failure\"],\n" +
            "            }\n" +
            "        },\n" +
            "        \"required\": [\"status\"],\n" +
            "    }";

    public Terminate() {
        setName("terminate");
        setDescription("Terminate the interaction when the request is met OR if the assistant cannot proceed further with the task.");

        setParameters(PARAMETERS);
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.warn("Terminate toolInput:" + toolInput);
        String result = String.format("The interaction has been completed with status: %s", toolInput);
        return new ToolExecuteResult(result);
    }

    public static void main(String[] args) {
        Terminate terminate = new Terminate();
        ToolExecuteResult toolExecuteResult = terminate.run("finished", null);
        System.out.println(JSON.toJSON(toolExecuteResult));
    }
}
