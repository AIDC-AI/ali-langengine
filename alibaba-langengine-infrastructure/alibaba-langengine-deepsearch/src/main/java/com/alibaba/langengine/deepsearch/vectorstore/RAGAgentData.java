/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.deepsearch.vectorstore;

import com.alibaba.langengine.deepsearch.agent.RAGAgent;

public class RAGAgentData {

    private RAGAgent agent;

    private Long totalToken;

    public RAGAgentData(RAGAgent agent, Long totalToken) {
        this.agent = agent;
        this.totalToken = totalToken;
    }

    public RAGAgent getAgent() {
        return agent;
    }

    public void setAgent(RAGAgent agent) {
        this.agent = agent;
    }

    public Long getTotalToken() {
        return totalToken;
    }

    public void setTotalToken(Long totalToken) {
        this.totalToken = totalToken;
    }
}
