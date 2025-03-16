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
import com.alibaba.langengine.deepsearch.DeepSearcher;
import com.alibaba.langengine.deepsearch.vectorstore.RetrievalResultData;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

@Slf4j
public class DeepSearchTool extends BaseTool {

    private DeepSearcher deepSearcher;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"query\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"In-depth search, usually including papers, academic research, current affairs surveys, etc.\"\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"query\"]\n" +
            "}";

    public DeepSearchTool() {
        setName("deep_search");
        setDescription("This tool is suitable for handling general and simple queries, such as given a topic and then writing a report, survey, or article.");

        setParameters(PARAMETERS);
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("DeepSearchTool toolInput:" + toolInput);

        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
        String query = (String) toolInputMap.get("query");

        RetrievalResultData retrievalResultData = deepSearcher.query(query);
        log.warn("DeepSearchTool result:" + JSON.toJSONString(retrievalResultData));
        return new ToolExecuteResult(retrievalResultData.getAnswer());
    }

    public DeepSearcher getDeepSearcher() {
        return deepSearcher;
    }

    public void setDeepSearcher(DeepSearcher deepSearcher) {
        this.deepSearcher = deepSearcher;
    }
}
