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
import com.alibaba.langengine.tool.google.service.SerpapiService;
import lombok.extern.slf4j.Slf4j;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.alibaba.langengine.tool.ToolConfiguration.SERPAPI_KEY;
import static com.alibaba.langengine.tool.ToolConfiguration.SERPAPI_SERVER_URL;

@Slf4j
public class GoogleSearch extends BaseTool {

    private SerpapiService service;

    private String serpapiKey = SERPAPI_KEY;

    private String PARAMETERS = "{\n" +
            "\t\"type\": \"object\",\n" +
            "\t\"properties\": {\n" +
            "\t\t\"query\": {\n" +
            "\t\t\t\"type\": \"string\",\n" +
            "\t\t\t\"description\": \"(required) The search query to submit to Google.\"\n" +
            "\t\t},\n" +
            "\t\t\"num_results\": {\n" +
            "\t\t\t\"type\": \"integer\",\n" +
            "\t\t\t\"description\": \"(optional) The number of search results to return. Default is 10.\",\n" +
            "\t\t\t\"default\": 10\n" +
            "\t\t}\n" +
            "\t},\n" +
            "\t\"required\": [\"query\"]\n" +
            "}";

    public GoogleSearch() {
        setName("google_search");
        setDescription("Perform a Google search and return a list of relevant links.\n" +
                "Use this tool when you need to find information on the web, get up-to-date data, or research specific topics.\n" +
                "The tool returns a list of URLs that match the search query.");

        String serverUrl = SERPAPI_SERVER_URL;
        service = new SerpapiService(serverUrl, Duration.ofSeconds(100L), false, null, null);

        setParameters(PARAMETERS);

    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("GoogleSearch toolInput:" + toolInput);

        Map<String, Object> toolInputMap = JSON.parseObject(toolInput, new TypeReference<Map<String, Object>>() {});
        String query = (String) toolInputMap.get("query");

        Integer numResults = 2;
        if(toolInputMap.get("num_results") != null) {
            numResults = (Integer) toolInputMap.get("num_results");
        }
        Map<String, Object> response = service.search(query, 0, numResults, serpapiKey);

        if (response.containsKey("error")) {
            throw new RuntimeException("Got error from SerpAPI: " + response.get("error"));
        }
        if (response.containsKey("answer_box") && response.get("answer_box") instanceof List) {
            response.put("answer_box", ((List) response.get("answer_box")).get(0));
        }

        String toret = "";
        if (response.containsKey("answer_box") && ((Map<String, Object>) response.get("answer_box")).containsKey("answer")) {
            toret = ((Map<String, Object>) response.get("answer_box")).get("answer").toString();
        } else if (response.containsKey("answer_box") && ((Map<String, Object>) response.get("answer_box")).containsKey("snippet")) {
            toret = ((Map<String, Object>) response.get("answer_box")).get("snippet").toString();
        } else if (response.containsKey("answer_box")
                && ((Map<String, Object>) response.get("answer_box")).containsKey("snippet_highlighted_words")
        ) {
            toret = ((List<String>) ((Map<String, Object>) response.get("answer_box")).get("snippet_highlighted_words")).get(0);
        } else if (response.containsKey("sports_results") && ((Map<String, Object>) response.get("sports_results")).containsKey("game_spotlight")) {
            toret = ((Map<String, Object>) response.get("sports_results")).get("game_spotlight").toString();
        } else if (response.containsKey("shopping_results") && ((List<Map<String, Object>>) response.get("shopping_results")).get(0).containsKey("title")) {
            List<Map<String, Object>> shoppingResults = (List<Map<String, Object>>) response.get("shopping_results");
            List<Map<String, Object>> subList = shoppingResults.subList(0, 3);
            toret = subList.toString();
        } else if (response.containsKey("knowledge_graph") && ((Map<String, Object>) response.get("knowledge_graph")).containsKey("description")) {
            toret = ((Map<String, Object>) response.get("knowledge_graph")).get("description").toString();
        } else if ((((List<Map<String, Object>>) response.get("organic_results")).get(0)).containsKey("snippet")) {
            toret = (((List<Map<String, Object>>) response.get("organic_results")).get(0)).get("snippet").toString();
        } else if ((((List<Map<String, Object>>) response.get("organic_results")).get(0)).containsKey("link")) {
            toret = (((List<Map<String, Object>>) response.get("organic_results")).get(0)).get("link").toString();
        } else if (response.containsKey("images_results") && ((Map<String, Object>) ((List<Map<String, Object>>) response.get("images_results")).get(0)).containsKey("thumbnail")) {
            List<String> thumbnails = new ArrayList<>();
            List<Map<String, Object>> imageResults = (List<Map<String, Object>>) response.get("images_results");
            for (Map<String, Object> item : imageResults.subList(0, 10)) {
                thumbnails.add(item.get("thumbnail").toString());
            }
            toret = thumbnails.toString();
        } else {
            toret = "No good search result found";
        }
        log.warn("SerpapiTool result:" + toret);
        return new ToolExecuteResult(toret);
    }

    public static void main(String[] args) {
        GoogleSearch googleSearch = new GoogleSearch();
        ToolExecuteResult toolExecuteResult = googleSearch.run("{\"query\":\"中国的首都在哪里\"}", null);
        System.out.println(JSON.toJSON(toolExecuteResult));
    }
}
