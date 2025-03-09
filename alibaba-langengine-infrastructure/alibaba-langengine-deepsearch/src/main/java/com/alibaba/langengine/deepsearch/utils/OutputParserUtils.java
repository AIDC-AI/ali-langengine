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
package com.alibaba.langengine.deepsearch.utils;

import com.alibaba.fastjson.JSONObject;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.ArrayList;

public class OutputParserUtils {

    public static List<String> literalEval(String responseContent) {
        responseContent = responseContent.trim();

//        if (responseContent.contains("") && responseContent.contains("")) {
//            int endOfThink = responseContent.indexOf("</think>") + "</think>".length();
//            responseContent = responseContent.substring(endOfThink).trim();
//        }

        try {
            if (responseContent.startsWith("```") && responseContent.endsWith("```")) {
                if (responseContent.startsWith("```python")) {
                    responseContent = responseContent.substring(9, responseContent.length() - 3).trim();
                } else if (responseContent.startsWith("```json")) {
                    responseContent = responseContent.substring(7, responseContent.length() - 3).trim();
                } else if (responseContent.startsWith("```str")) {
                    responseContent = responseContent.substring(6, responseContent.length() - 3).trim();
                } else if (responseContent.startsWith("```\n")) {
                    responseContent = responseContent.substring(4, responseContent.length() - 3).trim();
                } else {
                    throw new IllegalArgumentException("Invalid code block format");
                }
            }
            return parseJsonOrList(responseContent);
        } catch (Exception e) {
            Pattern pattern = Pattern.compile("(\\[.*?\\]|\\{.*?\\})", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(responseContent);

            List<String> matches = new ArrayList<>();
            while (matcher.find()) {
                matches.add(matcher.group(1));
            }

            if (matches.size() != 1) {
                throw new IllegalArgumentException("Invalid JSON/List format for response content:\n" + responseContent);
            }

            String jsonPart = matches.get(0);
            return parseJsonOrList(jsonPart);
        }
    }

    private static List<String> parseJsonOrList(String content) {
        content = content.trim();
        return JSONObject.parseArray(content, String.class);
    }
}
