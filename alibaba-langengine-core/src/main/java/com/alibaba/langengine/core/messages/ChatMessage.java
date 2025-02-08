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
package com.alibaba.langengine.core.messages;

import lombok.Data;

@Data
public class ChatMessage extends BaseMessage {

    /**
     * 当前聊天消息发送者的名称，在多智能体系统中这个消息需要进行标记
     */
    private String name;

    private String role;

    private Boolean prefix;

    @Override
    public String getType() {
        return "chat";
    }
}
