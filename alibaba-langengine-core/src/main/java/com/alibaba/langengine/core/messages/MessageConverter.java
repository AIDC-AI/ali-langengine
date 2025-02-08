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

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageConstant;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessageContent;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Message转换器
 *
 * @author xiaoxuan.lp
 */
@Data
public class MessageConverter {

    public static final String HUMAN_PREFIX = "Human";
    public static final String AI_PREFIX = "AI";
    public static final String SYSTEM_PREFIX = "System";
    public static final String FUNCTION_PREFIX = "Function";
    public static final String TOOL_PREFIX = "Tool";


    public static String getBufferString(List<BaseMessage> messages) {
        return getBufferString(messages, HUMAN_PREFIX, AI_PREFIX, SYSTEM_PREFIX, FUNCTION_PREFIX, TOOL_PREFIX);
    }
    public static String getBufferString(List<BaseMessage> messages,
                                         String humanPrefix,
                                         String aiPrefix,
                                         String systemPrefix,
                                         String functionPrefix,
                                         String toolPrefix) {
        List<String> stringMessages = new ArrayList<>();
        messages.stream().forEach(message -> {
            String role = null;
            if(message instanceof HumanMessage) {
                role = humanPrefix;
            } else if (message instanceof AIMessage) {
                role = aiPrefix;
            } else if (message instanceof SystemMessage) {
                role = systemPrefix;
            } else if (message instanceof FunctionMessage) {
                role = functionPrefix;
            } else if (message instanceof ToolMessage) {
                role = toolPrefix;
            } else if (message instanceof com.alibaba.langengine.core.messages.ChatMessage) {
                role = ((com.alibaba.langengine.core.messages.ChatMessage) message).getRole();
            } else {
                throw new RuntimeException("Got unsupported message type: " + message.getType());
            }
            // 判断是否为空
            if(message.getContent() != null) {
                if (message.getContent().contains(role + ":")) {
                    stringMessages.add(message.getContent());
                } else if (message.getContent().startsWith(role)) {
                    stringMessages.add(message.getContent());
                } else {
                    stringMessages.add(String.format("%s: %s", role, message.getContent()));
                }
            }
        });
        return stringMessages.stream().collect(Collectors.joining("\n"));
    }

    public static List<ChatMessage> convertMessageToChatMessage(List<BaseMessage> messages) {
        return messages.stream().map(message -> convertMessageToChatMessage(message)).collect(Collectors.toList());
    }

    public static ChatMessage convertMessageToChatMessage(BaseMessage message) {
        // Assuming BaseMessage, ChatMessage, HumanMessage, AIMessage, SystemMessage, and FunctionMessage are classes
        // you have defined in Java

        ChatMessage chatMessage = new ChatMessage();

        if (message instanceof com.alibaba.langengine.core.messages.ChatMessage) {
            chatMessage.setRole(((com.alibaba.langengine.core.messages.ChatMessage)message).getRole());
            chatMessage.setContent(message.getContent());
        } else if (message instanceof HumanMessage) {
            chatMessage.setRole("user");
            chatMessage.setContent(message.getContent());
            if (message.getAdditionalKwargs() != null && message.getAdditionalKwargs().containsKey(ChatMessageConstant.CHAT_MESSAGE_CONTENTS_KEY)) {
                chatMessage.setContentWithPojo((List<ChatMessageContent>)(message.getAdditionalKwargs().get(ChatMessageConstant.CHAT_MESSAGE_CONTENTS_KEY)));
            }
        } else if (message instanceof MultiHumanMessage) {
            chatMessage.setRole("user");
            chatMessage.setContentWithPojo(((MultiHumanMessage) message).getChatMessageContent());
        }  else if (message instanceof MultiAIMessage) {
            chatMessage.setRole("assistant");
            chatMessage.setContentWithPojo(((MultiAIMessage) message).getChatMessageContent());
        } else if (message instanceof AIMessage) {
            chatMessage.setRole("assistant");
            chatMessage.setContent(message.getContent());
            if (message.getAdditionalKwargs() != null && message.getAdditionalKwargs().containsKey("function_call")) {
                chatMessage.setFunctionCall((Map<String, Object>)(message.getAdditionalKwargs().get("function_call")));
                if("".equals(message.getContent())) {
                    chatMessage.setContent(null);
                }
            }
            if (message.getAdditionalKwargs() != null && message.getAdditionalKwargs().containsKey("tool_calls")) {
                chatMessage.setToolCalls((List<Map<String, Object>>) (message.getAdditionalKwargs().get("tool_calls")));
//                if ("".equals(message.getContent())) {
//                    chatMessage.setContent(null);
//                }
                if(message.getContent() == null) {
                    chatMessage.setContent("");
                }
            }
            chatMessage.setPrefix(((AIMessage) message).getPrefix());
        } else if (message instanceof SystemMessage) {
            chatMessage.setRole("system");
            chatMessage.setContent(message.getContent());
        } else if (message instanceof ToolMessage) {
            chatMessage.setRole("tool");
            chatMessage.setContent(message.getContent());
            chatMessage.setToolCallId(((ToolMessage) message).getTool_call_id());
        } else if (message instanceof FunctionMessage) {
            chatMessage.setRole("function");
            chatMessage.setContent(message.getContent());
            chatMessage.setName(((FunctionMessage) message).getName());
        } else if (message instanceof com.alibaba.langengine.core.messages.ChatMessage) {
            chatMessage.setRole(((com.alibaba.langengine.core.messages.ChatMessage)message).getRole());
            chatMessage.setContent(message.getContent());
        } else {
            throw new IllegalArgumentException("Got unknown type " + message.getClass().getSimpleName());
        }

        if (message.getAdditionalKwargs() != null && message.getAdditionalKwargs().containsKey("name")) {
            chatMessage.setName((String)message.getAdditionalKwargs().get("name"));
        }
        return chatMessage;
    }

    public static BaseMessage convertChatMessageToMessage(ChatMessage chatMessage) {
        if("assistant".equals(chatMessage.getRole())) {
            AIMessage aiMessage = new AIMessage();
            if(chatMessage.getFunctionCall() != null && chatMessage.getFunctionCall().size() > 0) {
                Map<String, Object> functionCallMap = new HashMap<>();
                functionCallMap.put("function_call", chatMessage.getFunctionCall());
                aiMessage.setToolUse(true);
                aiMessage.setAdditionalKwargs(functionCallMap);
            } else if(chatMessage.getToolCalls() != null && chatMessage.getToolCalls().size() > 0) {
                Map<String, Object> functionCallMap = new HashMap<>();
                functionCallMap.put("tool_calls", chatMessage.getToolCalls());
                aiMessage.setToolUse(true);
                aiMessage.setAdditionalKwargs(functionCallMap);
            } else if(chatMessage.getContent() != null) {
                aiMessage.setContent(chatMessage.getContent().toString());
            }
            return aiMessage;
        }
        return null;
    }
}
