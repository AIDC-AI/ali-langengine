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
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.callback.ExecutionContext;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.openmanus.tool.support.CodeExecutionResult;
import com.alibaba.langengine.openmanus.tool.support.CodeUtils;
import com.alibaba.langengine.openmanus.tool.support.LogIdGenerator;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

import static com.alibaba.langengine.tool.PythonCodeConstants.LLMMATH_PYTHON_CODE;

@Slf4j
public class PythonExecute extends BaseTool {

    private Boolean arm64 = false;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"code\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"The Python code to execute.\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"code\"]\n" +
            "}";

    public PythonExecute() {
        setName("python_execute");
        setDescription("Executes Python code string. Note: Only print outputs are visible, function return values are not captured. Use print statements to see results.");

        setParameters(PARAMETERS);
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("PythonExecute toolInput:" + toolInput);
        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
        String code = (String) toolInputMap.get("code");
//        String result = PythonUtils.invokePythonCodeWithArch(code, arm64);
        CodeExecutionResult codeExecutionResult = CodeUtils.executeCode(code, "python", "tmp_" + LogIdGenerator.generateUniqueId() + ".py", arm64, new HashMap<>());
        String result = codeExecutionResult.getLogs();
        return new ToolExecuteResult(result);
    }

    public static void main(String[] args) {
        PythonExecute pythonExecute = new PythonExecute();
//        String toolInput = "print('hello')";
        String toolInput = String.format(LLMMATH_PYTHON_CODE, "2 + 3 * 5");
        ToolExecuteResult toolExecuteResult = pythonExecute.run(String.format("{\"code\":\"%s\"}", toolInput), null);
        System.out.println(JSON.toJSON(toolExecuteResult));
    }

    public Boolean getArm64() {
        return arm64;
    }

    public void setArm64(Boolean arm64) {
        this.arm64 = arm64;
    }
}
