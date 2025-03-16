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
package com.alibaba.langengine.sqlite.memory;

import com.alibaba.langengine.core.messages.*;
import com.alibaba.langengine.core.prompt.MessageInfoDO;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.sqlite.memory.cache.SqliteCache;
import lombok.Data;

import java.util.List;
import java.util.stream.Collectors;

/**
 * sqlite消息历史记录
 *
 * @author xiaoxuan.lp
 */
@Data
public class ChatMessageSqliteHistory extends BaseChatMessageHistory {

    private SqliteCache sqliteCache;

    @Override
    public List<BaseMessage> getRawMessages(String sessionId) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        List<MessageInfoDO> messageInfoDOs = sqliteCache.getMessageInfo(sessionId);
        return messageInfoDOs.stream().map(messageInfoDO -> {
            if(messageInfoDO.getRole().equals("Human")) {
                HumanMessage humanMessage = new HumanMessage();
                humanMessage.setContent(messageInfoDO.getContent());
                return humanMessage;
            } else if(messageInfoDO.getRole().equals("AI")) {
                AIMessage aiMessage = new AIMessage();
                aiMessage.setContent(messageInfoDO.getContent());
                return aiMessage;
            } else if(messageInfoDO.getRole().equals("System")) {
                SystemMessage systemMessage = new SystemMessage();
                systemMessage.setContent(messageInfoDO.getContent());
                return systemMessage;
            } else if(messageInfoDO.getRole().equals("Tool")) {
                ToolMessage toolMessage = new ToolMessage();
                toolMessage.setContent(messageInfoDO.getContent());
                return toolMessage;
            } else {
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setRole(messageInfoDO.getRole());
                chatMessage.setContent(messageInfoDO.getContent());
            }
            return null;
        }).filter(e -> e != null).collect(Collectors.toList());
    }

    @Override
    public void addSystemMessage(String sessionId, String message) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        sqliteCache.updateMessageInfo(sessionId, "System", message);
    }

    @Override
    public void addUserMessage(String sessionId, String message) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        sqliteCache.updateMessageInfo(sessionId, "Human", message);
    }

    @Override
    public void addAIMessage(String sessionId, String message) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        sqliteCache.updateMessageInfo(sessionId, "AI", message);
    }

    @Override
    public void addToolMessage(String sessionId, String message) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        sqliteCache.updateMessageInfo(sessionId, "Tool", message);
    }

    @Override
    public void clear(String sessionId) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        sqliteCache.remove(sessionId);
    }
}
