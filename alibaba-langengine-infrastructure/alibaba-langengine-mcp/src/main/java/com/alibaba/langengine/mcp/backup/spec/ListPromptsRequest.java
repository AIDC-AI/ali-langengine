///**
// * Copyright (C) 2024 AIDC-AI
// * <p>
// * Licensed under the Apache License, Version 2.0 (the "License");
// * you may not use this file except in compliance with the License.
// * You may obtain a copy of the License at
// * <p>
// * http://www.apache.org/licenses/LICENSE-2.0
// * <p>
// * Unless required by applicable law or agreed to in writing, software
// * distributed under the License is distributed on an "AS IS" BASIS,
// * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// * See the License for the specific language governing permissions and
// * limitations under the License.
// */
//package com.alibaba.langengine.mcp.spec.schema;
//
//import com.alibaba.fastjson.JSONObject;
//import com.alibaba.langengine.mcp.spec.Method;
//import com.alibaba.langengine.mcp.spec.MethodDefined;
//
//public class ListPromptsRequest implements ClientRequest, PaginatedRequest {
//
//    private String cursor;
//
//    private JSONObject meta;
//
//    private final Method method = MethodDefined.PromptsList;
//
//    public ListPromptsRequest(String cursor) {
//        this.cursor = cursor;
//    }
//
//    public String getCursor() {
//        return cursor;
//    }
//
//    public void setCursor(String cursor) {
//        this.cursor = cursor;
//    }
//
//    @Override
//    public JSONObject getMeta() {
//        return meta;
//    }
//
//    public void setMeta(JSONObject meta) {
//        this.meta = meta;
//    }
//
//    @Override
//    public Method getMethod() {
//        return method;
//    }
//}
