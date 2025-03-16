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
import com.alibaba.langengine.core.chain.llmbash.BashProcess;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class Bash extends BaseTool {

    /**
     * bash执行工作目录
     */
    private String workingDirectoryPath;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"command\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"The bash command to execute. Can be empty to view additional logs when previous exit code is `-1`. Can be `ctrl+c` to interrupt the currently running process.\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"command\"]\n" +
            "}";

    public Bash(String workingDirectoryPath) {
        this.workingDirectoryPath = workingDirectoryPath;
        setName("bash");
        setDescription("Execute a bash command in the terminal.\n" +
                "* Long running commands: For commands that may run indefinitely, it should be run in the background and the output should be redirected to a file, e.g. command = `python3 app.py > server.log 2>&1 &`.\n" +
                "* Interactive: If a bash command returns exit code `-1`, this means the process is not yet finished. The assistant must then send a second call to terminal with an empty `command` (which will retrieve any additional logs), or it can send additional text (set `command` to the text) to STDIN of the running process, or it can send command=`ctrl+c` to interrupt the process.\n" +
                "* Timeout: If a command execution result says \"Command timed out. Sending SIGINT to the process\", the assistant should retry running the command in the background.");

        setParameters(PARAMETERS);
    }


    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("Bash toolInput:" + toolInput);
        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
        String command = (String) toolInputMap.get("command");
        List<String> commandList = new ArrayList<>();
        commandList.add(command);
        List<String> result = BashProcess.executeCommand(commandList, workingDirectoryPath);
        return new ToolExecuteResult(JSON.toJSONString(result));
    }

    public String getWorkingDirectoryPath() {
        return workingDirectoryPath;
    }

    public void setWorkingDirectoryPath(String workingDirectoryPath) {
        this.workingDirectoryPath = workingDirectoryPath;
    }


    public static void main(String[] args) {
        Bash bash = new Bash("/Users/xiaoxuan.lp/works/sources");
//        String toolInput = "echo 'hello world'";
        String toolInput = "{\"command\":\"ls -ls\"}";
        ToolExecuteResult toolExecuteResult = bash.run(toolInput, null);
        System.out.println(JSON.toJSON(toolExecuteResult));
    }
}
