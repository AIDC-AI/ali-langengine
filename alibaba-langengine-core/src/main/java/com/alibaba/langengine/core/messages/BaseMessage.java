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

import com.alibaba.langengine.core.runnables.RunnableInput;
import com.alibaba.langengine.core.runnables.RunnableOutput;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Data;

import java.util.Map;

/**
 * 消息基类
 *
 * @author xiaoxuan.lp
 */
@Data
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, property= JacksonUtils.PROPERTY_CLASS_NAME)
public abstract class BaseMessage implements RunnableInput, RunnableOutput {

    /**
     * 消息文本
     */
    private String content;

    /**
     * 思维链内容
     */
    private String reasoningContent;

    /**
     * 消息原始内容
     */
    private String orignalContent;

    /**
     * 额外字段
     */
    private Map<String, Object> additionalKwargs;

    /**
     * 合计token数
     */
    private Long totalTokens = 0L;

    /**
     * 消息的类型，用于序列化
     *
     * @return
     */
    public abstract String getType();
}
