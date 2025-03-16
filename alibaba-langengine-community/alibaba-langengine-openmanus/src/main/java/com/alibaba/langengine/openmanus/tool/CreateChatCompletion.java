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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
public class CreateChatCompletion extends BaseTool {

    private Map<Class<?>, String> typeMapping = new HashMap<>();
    private Class<?> responseType = String.class;
    private List<String> required = new ArrayList<>();

    public CreateChatCompletion() {
        setName("create_chat_completion");
        setDescription("Creates a structured completion with specified output formatting.");
        typeMapping.put(String.class, "string");
        typeMapping.put(Integer.class, "integer");
        typeMapping.put(Float.class, "number");
        typeMapping.put(Boolean.class, "boolean");
        typeMapping.put(Map.class, "object");
        typeMapping.put(List.class, "array");
        this.required.add("response");

        setParameters(buildParameters());
    }

    private String buildParameters() {
//        if (responseType.equals(String.class)) {
            Map<String, Object> params = new HashMap<String, Object>() {{
                put("type", "object");
                put("properties", new HashMap<String, Object>() {{
                    put("response", new HashMap<String, Object>() {{
                        put("type", "string");
                        put("description", "The response text that should be delivered to the user");
                    }});
                }});
                put("required", required);
            }};
            return JSON.toJSONString(params);
//        }
//        return createTypeSchema(responseType);
    }

    private Map<String, Object> createTypeSchema(Class<?> typeHint) {
        String type = typeMapping.getOrDefault(typeHint, "string");
        return new HashMap<String, Object>() {{
            put("type", "object");
            put("properties", new HashMap<String, Object>() {{
                put("response", new HashMap<String, Object>() {{
                    put("type", type);
                    put("description", "Response of type " + typeHint.getSimpleName());
                }});
            }});
            put("required", required);
        }};
    }

    @Override
    public ToolExecuteResult run(String toolInput, ExecutionContext executionContext) {
        log.info("CreateChatCompletion toolInput:" + toolInput);
        return new ToolExecuteResult(toolInput);
//        List<String> required = requiredFields != null ? requiredFields : this.required;
//        Object result;
//
//        if (required.size() == 1) {
//            String requiredField = required.get(0);
//            result = args.getOrDefault(requiredField, "");
//        } else {
//            Map<String, Object> results = new HashMap<>();
//            for (String field : required) {
//                results.put(field, args.getOrDefault(field, ""));
//            }
//            return results;
//        }
//
//        if (responseType == String.class) {
//            return result;
//        }
//
//        if (BaseModel.class.isAssignableFrom(responseType)) {
//            return createInstance(responseType, args);
//        }
//
//        if (List.class.isAssignableFrom(responseType) || Map.class.isAssignableFrom(responseType)) {
//            return result;
//        }
//
//        try {
//            return responseType.getConstructor(String.class).newInstance(result.toString());
//        } catch (Exception e) {
//            return result;
//        }
    }
}
