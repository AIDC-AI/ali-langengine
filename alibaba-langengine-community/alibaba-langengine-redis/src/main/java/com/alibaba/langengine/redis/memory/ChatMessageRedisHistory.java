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
package com.alibaba.langengine.redis.memory;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.alibaba.langengine.core.prompt.MessageInfoDO;
import com.alibaba.langengine.core.memory.BaseChatMessageHistory;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.ChatMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.SystemMessage;
import com.alibaba.langengine.core.messages.ToolMessage;

import com.alibaba.langengine.redis.memory.cache.RedisCache;
import lombok.Data;

/**
 * redis消息历史记录
 *
 * @author liuchunhe.lch on 2023/9/2 09:52
 */
@Data
public class ChatMessageRedisHistory extends BaseChatMessageHistory {

    private RedisCache redisCache;

    @Override
    public void setMessages(String sessionId, List<BaseMessage> messages) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        List<MessageInfoDO> messageInfoDOs = messages.stream().map(message -> {
            MessageInfoDO messageInfoDO = new MessageInfoDO();
            if (message instanceof HumanMessage) {
                messageInfoDO.setRole("Human");
            } else if (message instanceof AIMessage) {
                messageInfoDO.setRole("AI");
            } else if (message instanceof SystemMessage) {
                messageInfoDO.setRole("System");
            } else if (message instanceof ToolMessage) {
                messageInfoDO.setRole("Tool");
            } else if (message instanceof ChatMessage) {
                messageInfoDO.setRole(((ChatMessage)message).getRole());
            } else {
                return null;
            }
            messageInfoDO.setContent(message.getContent());
            return messageInfoDO;
        }).filter(Objects::nonNull).collect(Collectors.toList());
        // Update messages in Redis
        redisCache.updateMessageInfo(sessionId, messageInfoDOs);
    }

    @Override
    public List<BaseMessage> getRawMessages(String sessionId) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        List<MessageInfoDO> messageInfoDOs = redisCache.getMessageInfo(sessionId);
        if (messageInfoDOs == null) {
            return new ArrayList<>();
        }
        return messageInfoDOs.stream().map(messageInfoDO -> {
            if (messageInfoDO.getRole().equals("Human")) {
                HumanMessage humanMessage = new HumanMessage();
                humanMessage.setContent(messageInfoDO.getContent());
                return humanMessage;
            } else if (messageInfoDO.getRole().equals("AI")) {
                AIMessage aiMessage = new AIMessage();
                aiMessage.setContent(messageInfoDO.getContent());
                return aiMessage;
            } else if (messageInfoDO.getRole().equals("System")) {
                SystemMessage systemMessage = new SystemMessage();
                systemMessage.setContent(messageInfoDO.getContent());
                return systemMessage;
            } else if (messageInfoDO.getRole().equals("Tool")) {
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
        List<MessageInfoDO> allMessageInfoDOs = new ArrayList<>();
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        List<MessageInfoDO> messageInfoDOs = redisCache.getMessageInfo(sessionId);
        if (messageInfoDOs != null) {
            allMessageInfoDOs.addAll(messageInfoDOs);
        }
        MessageInfoDO messageInfoDO = new MessageInfoDO();
        messageInfoDO.setSessionId(sessionId);
        messageInfoDO.setRole("System");
        messageInfoDO.setContent(message);
        allMessageInfoDOs.add(messageInfoDO);
        redisCache.updateMessageInfo(sessionId, allMessageInfoDOs);
    }

    @Override
    public void addUserMessage(String sessionId, String message) {
        List<MessageInfoDO> allMessageInfoDOs = new ArrayList<>();
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        List<MessageInfoDO> messageInfoDOs = redisCache.getMessageInfo(sessionId);
        if (messageInfoDOs != null) {
            allMessageInfoDOs.addAll(messageInfoDOs);
        }
        MessageInfoDO messageInfoDO = new MessageInfoDO();
        messageInfoDO.setSessionId(sessionId);
        messageInfoDO.setRole("Human");
        messageInfoDO.setContent(message);
        allMessageInfoDOs.add(messageInfoDO);
        redisCache.updateMessageInfo(sessionId, allMessageInfoDOs);
    }

    @Override
    public void addAIMessage(String sessionId, String message) {
        List<MessageInfoDO> allMessageInfoDOs = new ArrayList<>();
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        List<MessageInfoDO> messageInfoDOs = redisCache.getMessageInfo(sessionId);
        if (messageInfoDOs != null) {
            allMessageInfoDOs.addAll(messageInfoDOs);
        }
        MessageInfoDO messageInfoDO = new MessageInfoDO();
        messageInfoDO.setSessionId(sessionId);
        messageInfoDO.setRole("AI");
        messageInfoDO.setContent(message);
        allMessageInfoDOs.add(messageInfoDO);
        redisCache.updateMessageInfo(sessionId, allMessageInfoDOs);
    }

    @Override
    public void addToolMessage(String sessionId, String message) {
        List<MessageInfoDO> allMessageInfoDOs = new ArrayList<>();
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        List<MessageInfoDO> messageInfoDOs = redisCache.getMessageInfo(sessionId);
        if (messageInfoDOs != null) {
            allMessageInfoDOs.addAll(messageInfoDOs);
        }
        MessageInfoDO messageInfoDO = new MessageInfoDO();
        messageInfoDO.setSessionId(sessionId);
        messageInfoDO.setRole("Tool");
        messageInfoDO.setContent(message);
        allMessageInfoDOs.add(messageInfoDO);
        redisCache.updateMessageInfo(sessionId, allMessageInfoDOs);
    }

    @Override
    public void clear(String sessionId) {
        if(sessionId == null) {
            sessionId = this.getSessionId();
        }
        redisCache.remove(sessionId);
    }
}
