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
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.tool.BaseTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import com.alibaba.langengine.docloader.easyexcel.EasyExcelDocLoader;
import com.alibaba.langengine.docloader.pdf.PDFDocLoader;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
public class DocLoaderTool extends BaseTool {

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"file_type\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(required) File type, such as pdf, text, docx, xlsx, csv, etc..\"\n" +
            "\t\t},\n" +
            "\t\t\"file_path\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(required) Get the absolute path of the file from the user request.\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"file_type\",\"file_path\"]\n" +
            "}";

    public DocLoaderTool() {
        setName("doc_loader");
        setDescription("Get the content information of a local file at a specified path. \n" +
                "Use this tool when you want to get some related information asked by the user. \n" +
                "This tool accepts the file path and gets the related information content.");

        setParameters(PARAMETERS);
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("DocLoaderTool toolInput:" + toolInput);
        try {
            Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
            String fileType = (String) toolInputMap.get("file_type");
            String filePath = (String) toolInputMap.get("file_path");
            List<Document> documentList = null;
            if("pdf".equals(fileType)) {
                PDFDocLoader loader = new PDFDocLoader();
                loader.setFilePath(filePath);
                documentList = loader.load();
            } else if("csv".equals(fileType) || "xlsx".equals(fileType) || "xls".equals(fileType)) {
                String path = "data/apilist.xlsx";
                filePath = getClass().getClassLoader().getResource(path).getPath();
                EasyExcelDocLoader loader = new EasyExcelDocLoader(filePath, null);
                loader.setReadHeader(true);
                loader.setFilePath(filePath);
                documentList = loader.load();
            }

            List<String> documentContents = documentList.stream().map(document -> document.getPageContent()).collect(Collectors.toList());

            String documentContentStr = String.join("\n", documentContents);
            if(StringUtils.isEmpty(documentContentStr)) {
                return new ToolExecuteResult("No Related information");
            } else {
                return new ToolExecuteResult("Related information: " + documentContentStr);
            }
        } catch (Throwable e) {
            return new ToolExecuteResult("Error get Related information: " + e.getMessage());
        }
    }
}
