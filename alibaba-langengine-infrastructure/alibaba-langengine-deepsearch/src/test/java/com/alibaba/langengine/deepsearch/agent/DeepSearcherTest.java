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
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.embeddings.DashScopeEmbeddings;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.deepsearch.DeepSearcher;
import com.alibaba.langengine.deepsearch.utils.CrawlUtils;
import com.alibaba.langengine.deepsearch.vectorstore.RetrievalResultData;
import com.alibaba.langengine.docloader.pdf.PDFDocLoader;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

public class DeepSearcherTest {

    private DeepSearcher deepSearcher;

    @BeforeEach
    public void set_data_vector_db() {
        // offline data to vectordb
//        String path = "data/WhatisMilvus.pdf";
        String path = "data/LangEngine.pdf";
        String filePath = getClass().getClassLoader().getResource(path).getPath();
        PDFDocLoader loader = new PDFDocLoader();
        loader.setFilePath(filePath);
        List<Document> documentList = loader.load();

        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkSize(1500);
        textSplitter.setMaxChunkOverlap(100);
        List<Document> chunks = textSplitter.splitDocuments(documentList);

        deepSearcher = new DeepSearcher();
        BaseChatModel llm = new DashScopeOpenAIChatModel();
        llm.setModel(DashScopeModelName.QWEN25_MAX);

        InMemoryDB vectordb = new InMemoryDB();
        vectordb.setEmbedding(new DashScopeEmbeddings());
        deepSearcher.initConfig(llm, vectordb);

        deepSearcher.getVectorStore().addDocuments(chunks);

        // online data to vectordb
        // langengine框架介绍文章
//        String csdnUrl = "https://blog.csdn.net/sunleepy2008/article/details/144469585";
//        CrawlUtils.crawlForDeepSearcher(deepSearcher, csdnUrl);
    }

    @Test
    public void test_query() {
//        String query = "Write a report comparing Milvus with other vector databases.";
//        String query = "写一篇关于LangEngine的论文";
        String query = "LangEngine的Retrieval模块是做什么用的";
        RetrievalResultData retrievalResultData = deepSearcher.query(query);
        System.out.println(JSON.toJSONString(retrievalResultData));
    }


    @Test
    public void test_naiveRagQuery() {
//        String query = "Write a report comparing Milvus with other vector databases.";
//        String query = "比较一下阿里LangEngine和其他AI应用开发框架的区别";
//        String query = "写一篇关于LangEngine的论文";
        String query = "LangEngine的Retrieval模块是做什么用的";
        RetrievalResultData retrievalResultData = deepSearcher.naiveRagQuery(query);
        System.out.println(JSON.toJSONString(retrievalResultData));
    }

}
