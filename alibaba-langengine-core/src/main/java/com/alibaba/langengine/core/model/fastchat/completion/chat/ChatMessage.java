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
package com.alibaba.langengine.core.model.fastchat.completion.chat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * ChatMessage
 *
 * @author xiaoxuan.lp
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {

    /**
     * Must be either 'system', 'user', or 'assistant'.<br>
     * You may use {@link ChatMessageRole} enum.
     */
    String role;

    /**
     * message content
     * 使用Object来允许多种类型，例如String和List
     */
    Object content;

    Boolean prefix;

    @JsonProperty("reasoning_content")
    String reasoningContent;

    /**
     * 函数调用
     */
    @JsonProperty("function_call")
    Map<String, Object> functionCall;

    /**
     * 名称
     * 包括：user、system、assistant
     */
    String name;

    @JsonProperty("tool_calls")
    List<Map<String, Object>> toolCalls;

    /**
     * 工具Id
     * 只有ToolMessage有
     */
    @JsonProperty("tool_call_id")
    String toolCallId;

    public void setContent(String content) {
        this.content = content;
    }

    public void setContentWithPojo(List<ChatMessageContent> chatMessageContents) {
        this.content = chatMessageContents;
    }

    public Object getContent() {
        return content;
    }

    /**
     * 获取content，根据需要的类型进行转换
     *
     * @param clazz
     * @return
     * @param <T>
     */
    public <T> T getContent(Class<T> clazz) {
        return clazz.cast(content);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Map<String, Object> getFunctionCall() {
        return functionCall;
    }

    public void setFunctionCall(Map<String, Object> functionCall) {
        this.functionCall = functionCall;
    }

    public List<Map<String, Object>> getToolCalls() {
        return toolCalls;
    }

    public void setToolCalls(List<Map<String, Object>> toolCalls) {
        this.toolCalls = toolCalls;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
