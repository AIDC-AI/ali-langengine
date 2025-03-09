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

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.embeddings.DashScopeEmbeddings;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.deepsearch.vectorstore.RetrievalResultData;
import com.alibaba.langengine.docloader.pdf.PDFDocLoader;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NaiveRAGTest {

    @Test
    public void test_run() {
        String filePath = getClass().getClassLoader().getResource("data/WhatisMilvus.pdf").getPath();
        PDFDocLoader loader = new PDFDocLoader();
        loader.setFilePath(filePath);
        List<Document> documentList = loader.load();

//        System.out.println(JSON.toJSONString(documentList));

        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkSize(1500);
        textSplitter.setMaxChunkOverlap(100);
        List<Document> chunks = textSplitter.splitDocuments(documentList);

//        System.out.println(JSON.toJSONString(chunks));

//        OpenAIEmbeddings embeddings = new OpenAIEmbeddings();
        DashScopeEmbeddings embeddings = new DashScopeEmbeddings();
        InMemoryDB inMemoryDB = new InMemoryDB();
        inMemoryDB.setEmbedding(embeddings);
        inMemoryDB.addDocuments(chunks);

//        ChatModelOpenAI llm = new ChatModelOpenAI();
//        llm.setModel(OpenAIModelConstants.GPT_4_TURBO);
        DashScopeOpenAIChatModel llm = new DashScopeOpenAIChatModel();
        llm.setModel(DashScopeModelName.QWEN25_MAX);

        NaiveRAGAgent agent = new NaiveRAGAgent(llm, inMemoryDB);
        Map<String, Object> kwargs = new HashMap<>();
        kwargs.put("topK", 3);
        RetrievalResultData retrievalResultData = agent.query("Write a report comparing Milvus with other vector databases.", kwargs);
        System.out.println(JSON.toJSONString(retrievalResultData));
    }
}
