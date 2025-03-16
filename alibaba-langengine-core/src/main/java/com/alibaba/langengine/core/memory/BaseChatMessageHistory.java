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

import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.util.JacksonUtils;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 聊天消息历史的基类
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property = JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseChatMessageHistory {
    public interface MessageHistoryWrapper {
        void modifyMessage(BaseMessage message);
    }

    private String sessionId;

    private MessageHistoryWrapper messageHistoryWrapper;

    public void setMessages(List<BaseMessage> messages) {
        setMessages(null, messages);
    }

    public void setMessages(String sessionId, List<BaseMessage> messages) {

    }

    protected List<BaseMessage> getRawMessages() {
        return getRawMessages(null);
    }

    protected abstract List<BaseMessage> getRawMessages(String sessionId);

    public List<BaseMessage> getMessages() {
        return getMessages(null);
    }

    public List<BaseMessage> getMessages(String sessionId) {
        if (messageHistoryWrapper == null) {
            return getRawMessages(sessionId);
        } else {
            List<BaseMessage> result = getRawMessages(sessionId);
            result.forEach(
                    message -> {
                        messageHistoryWrapper.modifyMessage(message);
                    });
            return result;
        }
    }

    /**
     * 移除最近一轮的对话；
     * 移除AI和用户的消息。
     * 移除AI，移除用户消息；
     * 移除AI消息、移除AI消息、移除AI消息，移除用户消息；
     */
    public List<BaseMessage> removeLastRound() {
        return removeLastRound(null);
    }

    public List<BaseMessage> removeLastRound(String sessionId) {
        List<BaseMessage> messages = getMessages(sessionId);
        if (messages.size() == 0) {
            return new ArrayList<>();
        }
        List<BaseMessage> lastRoundMessages = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            BaseMessage message = messages.get(i);
            if ("ai".equals(message.getType())) {
                lastRoundMessages.add(message);
            }
            // 只要遇到了用户的消息， 清空用户的消息，然后停止
            if ("human".equals(message.getType())) {
                lastRoundMessages.add(message);
                break;
            }
        }
        if (lastRoundMessages.size() == 0) {
            return new ArrayList<>();
        }
        List<BaseMessage> newMessages = messages.stream().filter(message -> !lastRoundMessages.contains(message))
                .collect(Collectors.toList());
        setMessages(sessionId, newMessages);
        return lastRoundMessages;
    }

    /**
     * 向存储添加系统消息
     *
     * @param message
     */
    public void addSystemMessage(String message) {
        addSystemMessage(null, message);
    }

    /**
     * 向存储添加系统消息
     *
     * @param sessionId
     * @param message
     */
    public abstract void addSystemMessage(String sessionId, String message);

    /**
     * 向存储添加用户消息
     *
     * @param message
     */
    public void addUserMessage(String message) {
        addUserMessage(null, message);
    }

    /**
     * 向存储添加用户消息
     *
     * @param sessionId
     * @param message
     */
    public abstract void addUserMessage(String sessionId, String message);

    /**
     * 向存储添加机器人消息
     *
     * @param message
     */
    public void addAIMessage(String message) {
        addAIMessage(null, message);
    }

    /**
     * 向存储添加机器人消息
     *
     * @param sessionId
     * @param message
     */
    public abstract void addAIMessage(String sessionId, String message);

    /**
     * 向存储添加工具消息
     *
     * @param message
     */
    public void addToolMessage(String message) {
        addToolMessage(null, message);
    }

    /**
     * 向存储添加工具消息
     *
     * @param message
     */
    public abstract void addToolMessage(String sessionId, String message);

    /**
     * 清空
     */
    public void clear() {
        clear(null);
    }

    /**
     * 清空
     */
    public abstract void clear(String sessionId);
}
