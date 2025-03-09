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

import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.deepsearch.vectorstore.RetrievalResultData;
import com.alibaba.langengine.deepsearch.utils.VectorStoreUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public class NaiveRAGAgent extends RAGAgent {
    private BaseChatModel llm;
    private VectorStore vectorDb;

    private static final String SUMMARY_PROMPT =
            "You are a AI content analysis expert, good at summarizing content. Please summarize a specific and detailed answer or report based on the previous queries and the retrieved document chunks.\n\n" +
                    "Original Query: %s\n\n" +
                    "Related Chunks:\n%s";

    public NaiveRAGAgent(BaseChatModel llm, VectorStore vectorDb) {
        this.llm = llm;
        this.vectorDb = vectorDb;
    }

    @Override
    public String getDescription() {
        return "NaiveRAG";
    }

    public RetrievalResultData retrieve(String query, Map<String, Object> kwargs) {
        Long consumeTokens = 0l;
        int nTokenRoute = 0;

        consumeTokens += nTokenRoute;
        List<Document> allRetrievedResults = new ArrayList<>();

        int topK = 3;
        if(kwargs.get("top_k") != null) {
            topK = Integer.parseInt(kwargs.get("top_k").toString());
        }
        List<Document> retrievalResults = vectorDb.similaritySearch(query, topK);
        allRetrievedResults.addAll(retrievalResults);

        allRetrievedResults = VectorStoreUtils.deduplicateResults(allRetrievedResults);
        return new RetrievalResultData(allRetrievedResults, consumeTokens);
    }

    public RetrievalResultData query(String query, Map<String, Object> kwargs) {
        RetrievalResultData retrievalData = retrieve(query, kwargs);
        List<Document> allRetrievedResults = retrievalData.getDocuments();
        Long nTokenRetrieval = retrievalData.getConsumeTokens();

        StringBuilder chunkTexts = new StringBuilder();
        for (Document chunk : allRetrievedResults) {
            chunkTexts.append(chunk.getPageContent());
        }

        String miniChunkStr = formatChunks(allRetrievedResults);
        String summaryPrompt = String.format(SUMMARY_PROMPT, query, miniChunkStr);
        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent(summaryPrompt);
        messages.add(humanMessage);
        BaseMessage chatResponse = llm.run(messages);
        AIMessage aiMessage = (AIMessage) chatResponse;
        String finalAnswer = aiMessage.getContent();

        log.info("\n==== FINAL ANSWER====\n");

        return new RetrievalResultData(finalAnswer, allRetrievedResults, nTokenRetrieval + aiMessage.getTotalTokens());
    }

    private String formatChunks(List<Document> chunks) {
        StringBuilder miniChunkStr = new StringBuilder();
        for (int i = 0; i < chunks.size(); i++) {
            miniChunkStr.append("<chunk_").append(i).append(">\n")
                    .append(chunks.get(i).getPageContent())
                    .append("\n</chunk_").append(i).append(">\n");
        }
        return miniChunkStr.toString();
    }
}
