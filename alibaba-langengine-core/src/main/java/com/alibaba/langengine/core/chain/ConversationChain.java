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
package com.alibaba.langengine.core.chain;

import com.alibaba.langengine.core.prompt.PromptConstants;
import com.alibaba.langengine.core.memory.impl.ConversationBufferMemory;
import lombok.Data;

/**
 * 链接以进行对话并从内存中加载上下文
 *
 * @author xiaoxuan.lp
 */
@Data
public class ConversationChain extends LLMChain {

    public ConversationChain() {
        setMemory(new ConversationBufferMemory());
        setPrompt(PromptConstants.CONVERSATION_PROMPT_EN);
    }

    public ConversationChain(boolean isCH) {
        setMemory(new ConversationBufferMemory());
        if(isCH) {
            setPrompt(PromptConstants.CONVERSATION_PROMPT_CH);
        }
    }
}
