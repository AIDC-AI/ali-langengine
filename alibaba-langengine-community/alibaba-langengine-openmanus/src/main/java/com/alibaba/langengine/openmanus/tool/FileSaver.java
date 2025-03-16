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
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

@Slf4j
public class FileSaver extends BaseTool {

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"content\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(required) The content to save to the file.\"\n" +
            "\t\t},\n" +
            "\t\t\"file_path\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(required) The path where the file should be saved, including filename and extension.\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"content\", \"file_path\"]\n" +
            "}";

    public FileSaver() {
        setName("file_saver");
        setDescription("Save content to a local file at a specified path.\n" +
                "Use this tool when you need to save text, code, or generated content to a file on the local filesystem.\n" +
                "The tool accepts content and a file path, and saves the content to that location.");

        setParameters(PARAMETERS);
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("FileSaver toolInput:" + toolInput);
        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String content = (String) toolInputMap.get("content");
            String filePath = (String) toolInputMap.get("file_path");
            File file = new File(filePath);
            File directory = file.getParentFile();
            if (directory != null && !directory.exists()) {
                directory.mkdirs();
            }

            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
                writer.write(content);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            return new ToolExecuteResult("Content successfully saved to " + filePath);
        } catch (Throwable e) {
            return new ToolExecuteResult("Error saving file: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        FileSaver fileSaver = new FileSaver();
        ToolExecuteResult toolExecuteResult = fileSaver.run("{\"content\":\"hello\",\"file_path\":\"/Users/xiaoxuan.lp/works/demo.txt\"}", null);
        System.out.println(JSON.toJSON(toolExecuteResult));
    }
}
