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

import com.alibaba.langengine.core.indexes.Document;

import java.util.List;
import java.util.Map;

public class RetrievalResultData {

    private String answer;

    private List<Document> documents;

    private Long consumeTokens;

    private Map<String, Object> additionalInfo;

    public RetrievalResultData(List<Document> documents, Long consumeTokens) {
        setDocuments(documents);
        setConsumeTokens(consumeTokens);
    }

    public RetrievalResultData(String answer, List<Document> documents, Long consumeTokens) {
        setAnswer(answer);
        setDocuments(documents);
        setConsumeTokens(consumeTokens);
    }

    public List<Document> getDocuments() {
        return documents;
    }

    public void setDocuments(List<Document> documents) {
        this.documents = documents;
    }

    public Long getConsumeTokens() {
        return consumeTokens;
    }

    public void setConsumeTokens(Long consumeTokens) {
        this.consumeTokens = consumeTokens;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public Map<String, Object> getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(Map<String, Object> additionalInfo) {
        this.additionalInfo = additionalInfo;
    }
}
