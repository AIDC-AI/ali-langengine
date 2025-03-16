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
package com.alibaba.langengine.openmanus;

import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.indexes.Document;
import com.alibaba.langengine.core.textsplitter.RecursiveCharacterTextSplitter;
import com.alibaba.langengine.core.vectorstore.memory.InMemoryDB;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.embeddings.DashScopeEmbeddings;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.deepsearch.DeepSearcher;
import com.alibaba.langengine.docloader.pdf.PDFDocLoader;
import com.alibaba.langengine.openmanus.agent.BaseAgent;
import com.alibaba.langengine.openmanus.agent.ManusAgent;
import com.alibaba.langengine.openmanus.flow.PlanningFlow;
import com.alibaba.langengine.openmanus.tool.DeepSearchTool;
import com.alibaba.langengine.openmanus.tool.DocLoaderTool;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class OpenManusTest {

    @BeforeEach
    public void setup() {
        URL resource = getClass().getClassLoader().getResource("data/chromedriver");
        if (resource == null) {
            throw new IllegalStateException("Chromedriver not found in resources");
        }
        String chromedriverPath = Paths.get(resource.getPath()).toFile().getAbsolutePath();
        System.setProperty("webdriver.chrome.driver", chromedriverPath);
    }

    @Test
    public void test_run_1() {
        ManusAgent manusAgent = new ManusAgent();
        String request = "我想搜索下杭州的天气";
        String response = manusAgent.run(request);
        System.out.println(response);
    }

    @Test
    public void test_run_2() {
        ManusAgent manusAgent = new ManusAgent();
        String request = "帮我写一个hello的字符串到本地文件 /Users/xiaoxuan.lp/works/auto.txt 中";
        String response = manusAgent.run(request);
        System.out.println(response);
    }

    @Test
    public void test_run_4() {
        ManusAgent manusAgent = new ManusAgent();
//        String request = "百度搜索下今天杭州天气如何，并且帮我总结下,写到/Users/xiaoxuan.lp/works/tmp/auto.txt";
        String request = "百度搜索下今天杭州天气如何，并且帮我总结下,把总结内容生成一张含这些内容的图片，写到/Users/xiaoxuan.lp/works/tmp/auto.png";
        String response = manusAgent.run(request);
        System.out.println(response);
    }

    @Test
    public void test_run_3() {
        ManusAgent manusAgent = new ManusAgent();

        DeepSearchTool deepSearchTool = new DeepSearchTool();
        String path = "data/LangEngine.pdf";
        String filePath = getClass().getClassLoader().getResource(path).getPath();
        PDFDocLoader loader = new PDFDocLoader();
        loader.setFilePath(filePath);
        List<Document> documentList = loader.load();

        RecursiveCharacterTextSplitter textSplitter = new RecursiveCharacterTextSplitter();
        textSplitter.setMaxChunkSize(1500);
        textSplitter.setMaxChunkOverlap(100);
        List<Document> chunks = textSplitter.splitDocuments(documentList);

        DeepSearcher deepSearcher = new DeepSearcher();
        BaseChatModel llm = new DashScopeOpenAIChatModel();
        llm.setModel(DashScopeModelName.QWEN25_MAX);

        InMemoryDB vectordb = new InMemoryDB();
        vectordb.setEmbedding(new DashScopeEmbeddings());
        deepSearcher.initConfig(llm, vectordb);

        deepSearcher.getVectorStore().addDocuments(chunks);
        deepSearchTool.setDeepSearcher(deepSearcher);

        manusAgent.addTool(deepSearchTool);

        String request = "写一篇关于LangEngine的论文，然后写到本地文件 /Users/xiaoxuan.lp/works/article.txt 中";
        String response = manusAgent.run(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_1() {
        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", new ManusAgent());
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "我想搜索下杭州的天气";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_2() {
        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", new ManusAgent());
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "帮我写一个hello的字符串到本地文件 /Users/xiaoxuan.lp/works/auto.txt 中";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_3() {
        ManusAgent manusAgent = new ManusAgent();

        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", manusAgent);
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "从百度上搜索LangEngine，写一篇关于它的论文，然后写到本地文件 /Users/xiaoxuan.lp/works/article.txt 中";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_4() {
        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", new ManusAgent());
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "帮我在京东网站上输入iphone 16 pro max，并在右边按钮点击搜索，新打开网页进行截图，然后写到本地文件 /Users/xiaoxuan.lp/works/article.txt 中";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_5() {
        ManusAgent manusAgent = new ManusAgent();

        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", manusAgent);
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "用百度搜索下LangEngine，写成一篇论文，并存储到本地文件 /Users/xiaoxuan.lp/works/article.txt 中";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_6() {
        ManusAgent manusAgent = new ManusAgent();

        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", manusAgent);
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "用百度搜索搜索一下阿里巴巴最近一周的股价，并绘制一个股价趋势图并保存到/Users/xiaoxuan.lp/works/tmp";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_7() {
        ManusAgent manusAgent = new ManusAgent();

        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", manusAgent);
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "我们是一家对人工智能领域技术有深入研究的科技咨询公司。请为我们创建一份潜在客户表单。目标公司为B轮前发展阶段、需要人工智能技术赋能的美国B2B公司。列出至少15家公司，清楚写明联系方式、公司业务介绍、地址等具体信息。制作一个仪表盘，文件生成到/tmp/index.html，然后用python3 -m http.server 8000启动。";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_8() {
        ManusAgent manusAgent = new ManusAgent();

        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", manusAgent);
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "\"x * y = 8\\n\" +\n" +
                "                \"x / y = 2\\n\" +\n" +
                "                \"\\n\" +\n" +
                "                \"x和y是正整数，求解x和y的值";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_9() {
        ManusAgent manusAgent = new ManusAgent();

        String path = "data/resume_example.pdf";
        String filePath = getClass().getClassLoader().getResource(path).getPath();

        manusAgent.addTool(new DocLoaderTool());

        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", manusAgent);
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "Create a minimalist, elegant business card for me inspired by Apple's design philosophy. I've attached my resume for reference.";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }

    @Test
    public void test_run_planning_10() {
        ManusAgent manusAgent = new ManusAgent();

        manusAgent.addTool(new DocLoaderTool());

        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", manusAgent);
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        String request = "请将我的API Excel文件总结分析下，每一个owner负责的API的总调用量，生成一个结构化的简单网页";
        String response = planningFlow.execute(request);
        System.out.println(response);
    }
}
