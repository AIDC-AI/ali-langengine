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
package com.alibaba.langengine.deepsearch.agent;

import com.alibaba.langengine.deepsearch.vectorstore.RetrievalResultData;

import java.util.Map;

public abstract class RAGAgent {

    public abstract String getDescription();

    /**
     * Retrieve document results from the knowledge base.
     *
     */
    public abstract RetrievalResultData retrieve(String query, Map<String, Object> kwargs);

    /**
     * Query the agent and return the answer.
     *
     */
    public abstract RetrievalResultData query(String query, Map<String, Object> kwargs);
}
