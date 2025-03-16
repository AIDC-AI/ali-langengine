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
package com.alibaba.langengine.core.memory;

import com.alibaba.langengine.core.messages.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

/**
 * ChatMessageHistory
 *
 * @author xiaoxuan.lp
 */
@Data
public class ChatMessageHistory extends BaseChatMessageHistory {

    private List<BaseMessage> rawMessages = new ArrayList<>();

    @Override
    protected List<BaseMessage> getRawMessages(String sessionId) {
        return rawMessages;
    }

    @Override
    public void addSystemMessage(String sessionId, String message) {
        SystemMessage systemMessage = new SystemMessage();
        systemMessage.setContent(message);
        getMessages().add(systemMessage);
    }

    @Override
    public void addUserMessage(String sessionId, String message) {
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent(message);
        getMessages().add(humanMessage);
    }

    @Override
    public void addAIMessage(String sessionId, String message) {
        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent(message);
        getMessages().add(aiMessage);
    }

    @Override
    public void addToolMessage(String sessionId, String message) {
        ToolMessage toolMessage = new ToolMessage();
        toolMessage.setContent(message);
        getMessages().add(toolMessage);
    }

    @Override
    public void clear(String sessionId) {
        getMessages().clear();
    }
}
