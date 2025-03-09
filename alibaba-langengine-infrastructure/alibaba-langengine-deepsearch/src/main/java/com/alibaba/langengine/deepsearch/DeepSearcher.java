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
package com.alibaba.langengine.deepsearch;

import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.deepsearch.agent.*;
import com.alibaba.langengine.deepsearch.vectorstore.RetrievalResultData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DeepSearcher {

    private RAGRouter ragRouter;
    private NaiveRAGAgent naiveRAG;

    private BaseChatModel llm;
    private VectorStore vectorStore;

    public void initConfig(BaseChatModel llm, VectorStore vectorStore) {
        this.llm = llm;
        this.vectorStore = vectorStore;
        this.naiveRAG = new NaiveRAGAgent(llm, vectorStore);
        List<RAGAgent> ragAgents = new ArrayList<>();
        ChainOfRAGAgent chainOfRAGAgent = new ChainOfRAGAgent(llm, vectorStore);
        DeepSearchAgent deepSearchAgent = new DeepSearchAgent(llm, vectorStore);
        ragAgents.add(chainOfRAGAgent);
        ragAgents.add(deepSearchAgent);
        ragRouter = new RAGRouter(llm, ragAgents);
    }

    public RetrievalResultData query(String originalQuery) {
        return query(originalQuery, 3);
    }

    public RetrievalResultData query(String originalQuery, int maxIter) {
        Map<String, Object> kwargs = new HashMap<>();
        kwargs.put("max_iter", maxIter);
        return ragRouter.query(originalQuery, kwargs);
    }

    public RetrievalResultData naiveRagQuery(String query) {
        return naiveRagQuery(query, 3);
    }

    public RetrievalResultData naiveRagQuery(String query, int topK) {
        Map<String, Object> kwargs = new HashMap<>();
        kwargs.put("top_k", topK);
        return naiveRAG.query(query, kwargs);
    }

    public BaseChatModel getLlm() {
        return llm;
    }

    public VectorStore getVectorStore() {
        return vectorStore;
    }
}
