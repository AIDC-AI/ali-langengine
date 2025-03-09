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
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.vectorstore.VectorStore;
import com.alibaba.langengine.deepsearch.utils.OutputParserUtils;
import com.alibaba.langengine.deepsearch.vectorstore.RetrievalResultData;
import com.alibaba.langengine.deepsearch.utils.VectorStoreUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Slf4j
public class DeepSearchAgent extends RAGAgent {

    private BaseChatModel llm;
    private VectorStore vectorDb;
    private boolean parallelProcSubQueries = false;

    private static final String SUB_QUERY_PROMPT =
            "To answer this question more comprehensively, please break down the original question into up to four sub-questions. Return as list of str.\n" +
                    "If this is a very simple question and no decomposition is necessary, then keep the only one original question in the python code list.\n\n" +
                    "Original Question: %s\n\n" +
                    "<EXAMPLE>\nExample input:\n\"Explain deep learning\"\n\nExample output:\n[\n" +
                    "    \"What is deep learning?\",\n    \"What is the difference between deep learning and machine learning?\",\n" +
                    "    \"What is the history of deep learning?\"\n]</EXAMPLE>\n\n" +
                    "Provide your response in a python code list of str format:";

    private static final String RERANK_PROMPT =
            "Based on the query questions and the retrieved chunk, to determine whether the chunk is helpful in answering any of the query question, you can only return \"YES\" or \"NO\", without any other information.\n\n" +
                    "Query Questions: %s\nRetrieved Chunk: %s\n\nIs the chunk helpful in answering the any of the questions?";

    private static final String REFLECT_PROMPT =
            "Determine whether additional search queries are needed based on the original query, previous sub queries, and all retrieved document chunks. If further research is required, provide a Python list of up to 3 search queries. If no further research is required, return an empty list.\n\n" +
                    "If the original query is to write a report, then you prefer to generate some further queries, instead return an empty list.\n\n" +
                    "Original Query: %s\n\nPrevious Sub Queries: %s\n\nRelated Chunks:\n%s\n\nRespond exclusively in valid List of str format without any other text.";

    private static final String SUMMARY_PROMPT =
            "You are a AI content analysis expert, good at summarizing content. Please summarize a specific and detailed answer or report based on the previous queries and the retrieved document chunks.\n\n" +
                    "Original Query: %s\n\nPrevious Sub Queries: %s\n\nRelated Chunks:\n%s\n\n";

    public DeepSearchAgent(BaseChatModel llm, VectorStore vectorDb) {
        this.llm = llm;
        this.vectorDb = vectorDb;
    }

    private List<String> generateSubQueries(String originalQuery) {
        String content = String.format(SUB_QUERY_PROMPT, originalQuery);
        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent(content);
        messages.add(humanMessage);
        BaseMessage chatResponse = llm.run(messages);

        return OutputParserUtils.literalEval(chatResponse.getContent());
    }

    private RetrievalResultData searchChunksFromVectorDB(String query, List<String> subQueries) {
        Long consumeTokens = 0L;

        List<Document> allRetrievedResults = new ArrayList<>();
        List<Document> retrievedResults = vectorDb.similaritySearch(query, 5);
        if (retrievedResults.isEmpty()) {
            log.info("<search> No relevant document chunks found! </search>\n");
        } else {
            int acceptedChunkNum = 0;
            for (Document retrievedResult : retrievedResults) {
                String rerankContent = String.format(RERANK_PROMPT, query + subQueries, "<chunk>" + retrievedResult.getPageContent() + "</chunk>");

                List<BaseMessage> messages = new ArrayList<>();
                HumanMessage humanMessage = new HumanMessage();
                humanMessage.setContent(rerankContent);
                messages.add(humanMessage);
                BaseMessage chatResponse = llm.run(messages);

                consumeTokens += chatResponse.getTotalTokens();
                String responseContent = chatResponse.getContent().trim();
                if (responseContent.contains("YES") && !responseContent.contains("NO")) {
                    allRetrievedResults.add(retrievedResult);
                    acceptedChunkNum++;
                }
            }

            if (acceptedChunkNum > 0) {
                log.info("<search> Accept " + acceptedChunkNum + " document chunk(s) </search>\n");
            } else {
                log.info("<search> No document chunk accepted! </search>\n");
            }
        }
        return new RetrievalResultData(allRetrievedResults, consumeTokens);
    }

    private CompletableFuture<RetrievalResultData> searchChunksFromVectorDBAsync(String query, List<String> subQueries) {
        return CompletableFuture.supplyAsync(() -> {
            return searchChunksFromVectorDB(query, subQueries);
        });
    }

    private List<String> generateGapQueries(String originalQuery, List<String> allSubQueries, List<Document> allChunks) {
        String reflectPrompt = String.format(REFLECT_PROMPT, originalQuery, allSubQueries, formatChunkTexts(allChunks));

        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent(reflectPrompt);
        messages.add(humanMessage);
        BaseMessage chatResponse = llm.run(messages);

        return OutputParserUtils.literalEval(chatResponse.getContent());
    }

    @Override
    public String getDescription() {
        return "This agent is suitable for handling general and simple queries, such as given a topic and then writing a report, survey, or article.";
    }

    @Override
    public RetrievalResultData retrieve(String originalQuery, Map<String, Object> kwargs) {
        int maxIter = 3;
        if(kwargs.get("maxIter") != null) {
            maxIter = Integer.parseInt(kwargs.get("maxIter").toString());
        }

        log.info("<query> " + originalQuery + " </query>\n");
        List<Document> allSearchRes = new ArrayList<>();
        List<String> allSubQueries = new ArrayList<>();
        Long totalTokens = 0L;

        List<String> subQueries = generateSubQueries(originalQuery);
        totalTokens += subQueries.size();
        if (subQueries.isEmpty()) {
            log.info("No sub queries were generated by the LLM. Exiting.");
            return new RetrievalResultData(new ArrayList<>(), totalTokens);
        } else {
            log.info(" Break down the original query into new sub queries: " + subQueries + "\n");
        }
        allSubQueries.addAll(subQueries);
        List<String> subGapQueries = subQueries;

        for (int iter = 0; iter < maxIter; iter++) {
            log.info(">> Iteration: " + (iter + 1) + "\n");

            if(parallelProcSubQueries) {
                // async
                List<CompletableFuture<RetrievalResultData>> searchTasks = new ArrayList<>();
                for (String query : subGapQueries) {
                    searchTasks.add(searchChunksFromVectorDBAsync(query, subGapQueries));
                }

                CompletableFuture.allOf(searchTasks.toArray(new CompletableFuture[0])).join();
                for (CompletableFuture<RetrievalResultData> task : searchTasks) {
                    RetrievalResultData result = null;
                    try {
                        result = task.get();
                        totalTokens += result.getConsumeTokens();
                        allSearchRes.addAll(result.getDocuments());
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    } catch (ExecutionException e) {
                        throw new RuntimeException(e);
                    }
                }
            } else {
                // sync
                for (String query : subGapQueries) {
                    RetrievalResultData result = searchChunksFromVectorDB(query, subGapQueries);
                    totalTokens += result.getConsumeTokens();
                    allSearchRes.addAll(result.getDocuments());
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            allSearchRes = VectorStoreUtils.deduplicateResults(allSearchRes);
            if (iter == maxIter - 1) {
                log.info(" Exceeded maximum iterations. Exiting. \n");
                break;
            }
            log.info(" Reflecting on the search results... \n");
            subGapQueries = generateGapQueries(originalQuery, allSubQueries, allSearchRes);
            totalTokens += subGapQueries.size();
            if (subGapQueries.isEmpty()) {
                log.info(" No new search queries were generated. Exiting. \n");
                break;
            } else {
                log.info(" New search queries for next iteration: " + subGapQueries + " \n");
                allSubQueries.addAll(subGapQueries);
            }
        }
        allSearchRes = VectorStoreUtils.deduplicateResults(allSearchRes);

        RetrievalResultData data = new RetrievalResultData(allSearchRes, totalTokens);
        data.setAdditionalInfo(Collections.singletonMap("allSubQueries", allSubQueries));
        return data;
    }

    @Override
    public RetrievalResultData query(String query, Map<String, Object> kwargs) {
        RetrievalResultData retrievalResultData = retrieve(query, kwargs);
        List<Document> allRetrievedResults = retrievalResultData.getDocuments();
        if (allRetrievedResults.isEmpty()) {
            return new RetrievalResultData("No relevant information found for query '" + query + "'.", new ArrayList<>(), retrievalResultData.getConsumeTokens());
        }
        List<String> allSubQueries = (List<String>) retrievalResultData.getAdditionalInfo().get("allSubQueries");
        log.info(" Summarize answer from all " + allRetrievedResults.size() + " retrieved chunks... \n");
        String summaryPrompt = String.format(SUMMARY_PROMPT, query, allSubQueries, formatChunkTexts(allRetrievedResults));

        List<BaseMessage> messages = new ArrayList<>();
        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent(summaryPrompt);
        messages.add(humanMessage);
        BaseMessage chatResponse = llm.run(messages);

        log.info("\n==== FINAL ANSWER====\n");
        log.info(chatResponse.getContent());

        return new RetrievalResultData(chatResponse.getContent(), allRetrievedResults, retrievalResultData.getConsumeTokens() + chatResponse.getTotalTokens());
    }

    private String formatChunkTexts(List<Document> retrievedResults) {
        StringBuilder chunkStr = new StringBuilder();
        for (int i = 0; i < retrievedResults.size(); i++) {
            String text = retrievedResults.get(i).getPageContent();
            chunkStr.append("<chunk_").append(i).append(">\n").append(text).append("\n</chunk_").append(i).append(">\n");
        }
        return chunkStr.toString();
    }
}
