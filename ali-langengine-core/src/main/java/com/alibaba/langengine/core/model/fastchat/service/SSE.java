/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.core.model.fastchat.service;

/**
 * Server-Sent Events规范
 *
 * @author xiaoxuan.lp
 */
public class SSE {
    private static final String DONE_DATA = "[DONE]";

    private final String data;

    public SSE(String data){
        this.data = data;
    }

    public String getData(){
        return this.data;
    }

    public byte[] toBytes(){
        return String.format("data: %s\n\n", this.data).getBytes();
    }

    public boolean isDone(){
        return DONE_DATA.equalsIgnoreCase(this.data);
    }
}
