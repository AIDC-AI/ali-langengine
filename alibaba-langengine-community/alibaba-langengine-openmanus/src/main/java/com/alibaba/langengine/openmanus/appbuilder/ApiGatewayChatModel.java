package com.alibaba.langengine.openmanus.appbuilder;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.messages.AIMessage;
import com.alibaba.langengine.core.messages.BaseMessage;
import com.alibaba.langengine.core.messages.HumanMessage;
import com.alibaba.langengine.core.messages.MessageConverter;
import com.alibaba.langengine.core.model.ResponseCollector;
import com.alibaba.langengine.core.model.fastchat.completion.chat.*;
import com.alibaba.langengine.core.util.LLMUtils;
import com.alibaba.langengine.openmanus.appbuilder.appengine.AppEngineChoice;
import com.alibaba.langengine.openmanus.appbuilder.appengine.AppEngineResultData;
import com.alibaba.langengine.openmanus.appbuilder.service.ApiGatewayChatService;
import com.alibaba.langengine.openmanus.appbuilder.service.ApiGatewayLLMException;
import com.alibaba.langengine.openmanus.appbuilder.service.ApiGatewayModelType;
import com.alibaba.langengine.openmanus.appbuilder.service.ApiGatewayResult;
import com.google.common.collect.Maps;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * AIDC APaaS ApiGateway ChatModel
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class ApiGatewayChatModel extends BaseChatModel<ChatCompletionRequest> {

    private static final String STREAM_REFERENCE_KEY = "streamReference";

    private static final String PRE_ENV = "pre";

    public static Map<String, String> gatewayInvokePathTemplatePathMap = new HashMap<>();

    static {
        gatewayInvokePathTemplatePathMap.put("pre", "https://pre-apaas.alibaba-inc.com/");
        gatewayInvokePathTemplatePathMap.put("production", "https://apaas.alibaba-inc.com/");
    }

    private ApiGatewayChatService service;

    /**
     * 环境，包括:pre、production
     */
    private String env;

    /**
     * api code
     */
    private String apiCode;

    /**
     * api version
     */
    private String apiVersion;

    /**
     * 模型名称，例如o1-mini、o1-preview
     */
    private String modelName;

    /**
     * 模型类型
     */
    private ApiGatewayModelType modelType;

    /**
     * The maximum number of tokens that can be generated in the chat completion.
     * The total length of input tokens and generated tokens is limited by the model's context length.
     */
    private Integer maxTokens;

    /**
     * What sampling temperature to use, between 0 and 2. Higher values like 0.8 will make the output more random, while lower values like 0.2 will make it more focused and deterministic.
     * We generally recommend altering this or top_p but not both.
     */
    private Double temperature;

    /**
     * An alternative to sampling with temperature, called nucleus sampling, where the model considers the results of the tokens with top_p probability mass. So 0.1 means only the tokens comprising the top 10% probability mass are considered.
     * We generally recommend altering this or temperature but not both.
     */
    private Double topP;

    /**
     * Model considers the results of top_k tokens. So top_k=1 means no sampling, i.e., greedy generation.
     */
    private Integer topK;

    /**
     * 业务参数
     */
    private Map<String, Object> paramJson;

    /**
     * 是否自动增加flag
     */
    private boolean autoLlmFlag = false;

    /**
     * 是否流模式
     */
    private boolean stream = false;

    /**
     * 默认增量流式
     */
    private Boolean sseInc = true;

    /**
     * 请求id
     */
    private String requestId;

    /**
     *  请求超时时间（秒）
     */
    private Integer timeoutSecond = 300;

    /**
     * 是否异步化调用
     */
    private boolean async = false;
    /**
     * 和网关-应用打通的全局唯一id
     */
    private String uniqueId;

    private String aibRpcId;

    /**
     * 是否openai格式
     */
    private Boolean openaiFormat = true;

    /**
     * 是否toolCall格式
     */
    private Boolean toolCallFormat = false;

    public ApiGatewayChatModel(String apiCode, String apiVersion, ApiGatewayModelType modelType, String env) {
        this(apiCode, apiVersion, modelType, env, null);
    }

    public ApiGatewayChatModel(String apiCode, String apiVersion, ApiGatewayModelType modelType, String env, String apiKey) {
        setApiCode(apiCode);
        setApiVersion(apiVersion);
        setModelType(modelType);
        setEnv(env);
        String serverUrl = getGatewayPath(env);
        service = new ApiGatewayChatService(serverUrl, Duration.ofSeconds(timeoutSecond), true, apiKey);
    }

    @Override
    public BaseMessage run(List<BaseMessage> messages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        log.info("extraAttributes is " + JSON.toJSONString(extraAttributes));
        List<ChatMessage> chatMessages = MessageConverter.convertMessageToChatMessage(messages);

        for(int i = 0; i < chatMessages.size(); i++) {
            ChatMessage chatMessage = chatMessages.get(i);
            if("tool".equals(chatMessage.getRole())) {
                chatMessage.setRole("function");
                ChatMessage prevChatMessage = chatMessages.get(i - 1);
                if(prevChatMessage.getFunctionCall() != null && prevChatMessage.getFunctionCall().get("name") != null) {
                    prevChatMessage.getFunctionCall().get("name");
                    chatMessage.setName(prevChatMessage.getFunctionCall().get("name").toString());
                }
            }
        }

        //  o1-mini and o1-preview system messages are not supported.
        if(!StringUtils.isEmpty(modelName) && ("o1-mini".equals(modelName) || "o1-preview".equals(modelName))) {
            chatMessages = chatMessages.stream().filter(e -> !ChatMessageRole.SYSTEM.value().equals(e.getRole())).collect(Collectors.toList());
        }

        int index = 0;
        for (ChatMessage chatMessage : chatMessages) {
            if(!ChatMessageRole.USER.value().equals(chatMessage.getRole())) {
                continue;
            }
            if(chatMessage.getContent() == null) {
                continue;
            }
            if(chatMessage.getContent() instanceof List) {
                List list = (List) chatMessage.getContent();
                if(list.size() == 0) {
                    continue;
                }
                if(!(list.get(0) instanceof ChatMessageContent)) {
                    continue;
                }
                for (ChatMessageContent chatMessageContent : (List<ChatMessageContent>)list) {
                    if(!ChatMessageContent.TYPE_TEXT.equals(chatMessageContent.getType())) {
                        continue;
                    }
                    String request = generateFinalPrompt(chatMessageContent.getText());
                    chatMessageContent.setText(request);
                    log.info("list finalPrompt:" + (index++) + " " + request);
                }
            } else {
                String finalPrompt = generateFinalPrompt(chatMessage.getContent().toString());
                chatMessage.setContent(finalPrompt);
                log.info("string finalPrompt:" + (index++) + " " + finalPrompt);
            }
        }

        boolean currentStream = stream || (consumer != null);

        Map<String, Object> request = new HashMap<>();
        request.put("apiCode", apiCode);
        request.put("version", apiVersion);
        request.put("stream", currentStream);
        if(StringUtils.isNotEmpty(uniqueId)) {
            request.put("uniqueId", uniqueId);
        } else if (MapUtils.isNotEmpty(extraAttributes) && extraAttributes.containsKey("uniqueId")) {
            request.put("uniqueId", MapUtils.getString(extraAttributes, "uniqueId"));
        }
        if(StringUtils.isNotEmpty(aibRpcId)) {
            request.put("aibRpcId", aibRpcId);
        } else if (MapUtils.isNotEmpty(extraAttributes) && extraAttributes.containsKey("aibRpcId")) {
            request.put("aibRpcId", MapUtils.getString(extraAttributes, "aibRpcId"));
        }

        if(async) {
            request.put("async", true);
            request.put("multi", false);
        }
        if(extraAttributes != null) {
            if(extraAttributes.get("extra") != null) {
                request.put("extra", extraAttributes.get("extra"));
            }
            if(extraAttributes.get("taskPriority") != null && extraAttributes.get("taskPriority") instanceof String) {
                request.put("taskPriority", extraAttributes.get("taskPriority"));
            }
        }

        Map<String, Object> paramJson = new HashMap<>();
        request.put("paramJson", paramJson);

        paramJson.put("messages", chatMessages);
        if(functions != null) {
            if(isStandardToolCall()) {
                List<StandaredToolDefinition> toolDefinitions = functions.stream()
                        .map(e -> {
                            StandaredToolDefinition toolDefinition = new StandaredToolDefinition();
                            toolDefinition.setFunction(e);
                            return toolDefinition;
                        }).collect(Collectors.toList());
                paramJson.put("tools", toolDefinitions);
            } else {
                paramJson.put("functions", functions);
            }
        }
        if(maxTokens != null) {
            if(isGptProxy()) {
                paramJson.put("max_tokens", maxTokens);
            } else {
                paramJson.put("max_new_tokens", maxTokens);
            }
        }
        if(temperature != null) {
            paramJson.put("temperature", temperature);
        }
        if(stops != null && stops.size() > 0) {
            // TODO 后面需要调整成stop
            paramJson.put("stop_words", stops);
            paramJson.put("stop", stops);
        }
        if(!StringUtils.isEmpty(modelName)) {
            paramJson.put("model", modelName);
        }

         if(topP != null) {
            paramJson.put("top_p", topP);
        }
        if(topK != null) {
            paramJson.put("top_k", topK);
        }

        log.info("chatMessages:" + (chatMessages != null ? JSON.toJSONString(chatMessages) : null));
        log.info("functions:" + (functions != null ? JSON.toJSONString(functions) : null));

        AtomicReference<BaseMessage> baseMessage = new AtomicReference<>();

        AtomicReference<ResponseCollector> answerContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<Object> functionCallContent = new AtomicReference<>();
        AtomicReference<ResponseCollector> functionCallNameContent = new AtomicReference<>(new ResponseCollector(sseInc));
        AtomicReference<ResponseCollector> argumentContent = new AtomicReference<>(new ResponseCollector(sseInc));
        log.info("ApiGatewayChatModel request is " + JSON.toJSONString(request));
        log.info("ApiGatewayChatModel requestId is " + requestId);

        try {
            if ("Ovis".equals(apiCode)) {
                runMarcoVL(request, baseMessage, answerContent,
                        currentStream, consumer, stops);
            } else if (isGptProxy()) {
                runGpt(request, baseMessage, answerContent,
                        functionCallContent, functionCallNameContent, argumentContent,
                        currentStream, consumer, stops);
            } else {
                runQwen(request, baseMessage, answerContent,
                        functionCallContent, functionCallNameContent, argumentContent,
                        currentStream, consumer, stops);
            }
        } catch(ApiGatewayLLMException e) {
            throw e;
        } catch (Throwable throwable) {
            log.error(apiCode + " runLlm error", throwable);
            throw new RuntimeException(apiCode + " runLlm error", throwable);
        }
        return baseMessage.get();
    }

    @Override
    public ChatCompletionRequest buildRequest(List<ChatMessage> chatMessages, List<FunctionDefinition> functions, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public BaseMessage runRequest(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    @Override
    public BaseMessage runRequestStream(ChatCompletionRequest request, List<String> stops, Consumer<BaseMessage> consumer, Map<String, Object> extraAttributes) {
        return null;
    }

    private void runMarcoVL(Map<String, Object> request,
                            AtomicReference<BaseMessage> baseMessage,
                            AtomicReference<ResponseCollector> answerContent,
                            boolean currentStream,
                            Consumer<BaseMessage> consumer,
                            List<String> stops) {
        log.info("runMarcoVL:" + apiCode + ":" + System.currentTimeMillis());
        if (currentStream) {
            service.executeStream(request, requestId)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(apiGatewayResult -> {

                        String apiGatewayResultString = JSON.toJSONString(apiGatewayResult);
                        log.info(apiCode + " stream apiGatewayResult is " + apiGatewayResultString);

                        if(!apiGatewayResult.getSuccess()) {
                            throw new ApiGatewayLLMException(apiGatewayResult, apiCode + " apiGatewayResult is error, reason is " + apiGatewayResultString, requestId);
//                            throw new RuntimeException("apiGatewayResult is error, reason is " + apiGatewayResultString);
                        }
                        if(apiGatewayResult.getData() == null) {
                            log.error("apiGatewayResult data is null");
                            return;
                        }
                        String result = apiGatewayResult.getData().getResult();
                        if(StringUtils.isEmpty(result)) {
                            log.error("apiGatewayResult data result is null");
                            return;
                        }
                        AppEngineResultData appEngineResult = JSON.parseObject(result, AppEngineResultData.class);
                        log.info(apiCode + " stream apiGatewayResult result is " + result);
                        String appEngineResultString = result;

                        if (appEngineResult == null
                                || appEngineResult.getChoices() == null
                                || appEngineResult.getChoices().size() == 0) {
                            throw new RuntimeException("apiGatewayResult choices is empty, reason is " + appEngineResultString);
                        }
                        AppEngineChoice choice = appEngineResult.getChoices().get(0);
                        if(choice.getMessage().getContent() == null) {
                            return;
                        }
                        String responseContent = choice.getMessage().getContent().toString();
                        answerContent.get().collect(responseContent);

                        AIMessage message = new AIMessage();
                        message.setContent(answerContent.get().joining());
                        log.warn(apiCode + " stream message:" + JSON.toJSONString(message));
                        if (consumer != null) {
                            consumer.accept(message);
                        }
                        baseMessage.set(message);
                    });
        } else {
            ApiGatewayResult apiGatewayResult = service.execute(request, requestId);
            String apiGatewayResultString = JSON.toJSONString(apiGatewayResult);
            log.info(apiCode + " apiGatewayResult is " + apiGatewayResultString);

            if(!apiGatewayResult.getSuccess()) {
                throw new ApiGatewayLLMException(apiGatewayResult, apiCode + " apiGatewayResult is error, reason is " + apiGatewayResultString, requestId);
//                throw new RuntimeException("apiGatewayResult is error, reason is " + apiGatewayResultString);
            }
            if(apiGatewayResult.getData() == null) {
                log.error("apiGatewayResult data is null");
                return;
            }
            String result = apiGatewayResult.getData().getResult();
            if(StringUtils.isEmpty(result)) {
                log.error("apiGatewayResult data result is null");
                return;
            }
            log.info(apiCode + " apiGatewayResult result is " + result);
            if(async) {
                AIMessage message = buildAsyncTaskMessage(result);
                baseMessage.set(message);
                return;
            }
            AppEngineResultData appEngineResult = JSON.parseObject(result, AppEngineResultData.class);
            String appEngineResultString = result;

            if (appEngineResult == null
                    || appEngineResult.getChoices() == null
                    || appEngineResult.getChoices().size() == 0) {
                throw new RuntimeException("apiGatewayResult choices is empty, reason is " + appEngineResultString);
            }

            String responseContent = null;
            if(appEngineResult.getChoices().get(0).getDelta() != null) {
                responseContent = appEngineResult.getChoices().get(0).getDelta().getContent().toString();
            } else if(appEngineResult.getChoices().get(0).getMessage() != null) {
                responseContent = appEngineResult.getChoices().get(0).getMessage().getContent().toString();
            }
            responseContent = LLMUtils.interceptAnswerWithStopsSplit(responseContent, stops);
//            if (containUsage) {
//                UsageAnswerResult usageAnswerResult = new UsageAnswerResult();
//                usageAnswerResult.setAnswer(responseContent);
//                usageAnswerResult.setInputTokens(appEngineResult.getUsage() != null ? appEngineResult.getUsage().getPromptTokens() : 0L);
//                usageAnswerResult.setOutputTokens(appEngineResult.getUsage() != null ? appEngineResult.getUsage().getCompletionTokens() : 0L);
//
//                responseContent = JSON.toJSONString(usageAnswerResult);
//                log.warn(model + " answer:" + responseContent);
//            } else {
                log.warn(apiCode + " answer:" + responseContent);
//            }
            AIMessage message = new AIMessage();
            message.setContent(responseContent);

            baseMessage.set(message);
        }
    }

    private void runGpt(Map<String, Object> request,
                        AtomicReference<BaseMessage> baseMessage,
                        AtomicReference<ResponseCollector> answerContent,
                        AtomicReference<Object> functionCallContent,
                        AtomicReference<ResponseCollector> functionCallNameContent,
                        AtomicReference<ResponseCollector> argumentContent,
                        boolean currentStream,
                        Consumer<BaseMessage> consumer,
                        List<String> stops) {
        log.info("runGpt:" + apiCode + ":" + System.currentTimeMillis());
        if(currentStream) {
            service.executeStream(request, requestId)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(apiGatewayResult -> {
                        
                        String apiGatewayResultString = JSON.toJSONString(apiGatewayResult);
                        log.info(apiCode + " stream apiGatewayResult is " + apiGatewayResultString);

                        if(!apiGatewayResult.getSuccess()) {
                            throw new ApiGatewayLLMException(apiGatewayResult, apiCode + " apiGatewayResult is error, reason is " + apiGatewayResultString, requestId);
//                            throw new RuntimeException("apiGatewayResult is error, reason is " + apiGatewayResultString);
                        }
                        if(apiGatewayResult.getData() == null) {
                            log.error("apiGatewayResult data is null");
                            return;
                        }
                        String result = apiGatewayResult.getData().getResult();
                        if(StringUtils.isEmpty(result)) {
                            log.error("apiGatewayResult data result is null");
                            return;
                        }
                        com.alibaba.langengine.openmanus.appbuilder.completion.ChatCompletionResult appEngineResult = JSON.parseObject(result, com.alibaba.langengine.openmanus.appbuilder.completion.ChatCompletionResult.class);
                        log.info(apiCode + " stream apiGatewayResult result is " + result);
                        String appEngineResultString = result;
                        
                        if(appEngineResult.getChoices() == null) {
                            throw new RuntimeException(apiCode + " apiGatewayResult choices is empty, reason is " + appEngineResultString);
                        }
                        if(appEngineResult.getChoices().size() == 0) {
                            return;
                        }
                        com.alibaba.langengine.openmanus.appbuilder.completion.ChatCompletionChoice choice = appEngineResult.getChoices().get(0);
                        // finish_reason不为空时，流式结束
                        if(!StringUtils.isEmpty(choice.getFinishReason())) {
                            if("stop".equals(choice.getFinishReason())) {
                                ChatMessage message = choice.getMessage();
                                if(message != null) {
                                    if (message.getContent() != null || !StringUtils.isEmpty(message.getReasoningContent())) {
                                        answerContent.get().thinkCollect(message.getReasoningContent());
                                        if(message.getContent() != null) {
                                        answerContent.get().collect(String.valueOf(message.getContent()));
                                        }
                                        String answer = answerContent.get().joining();
                                        if (answer != null) {
                                            if (consumer != null) {
                                                log.warn(apiCode + " stream answer:" + answer);
                                                AIMessage aiMessage = new AIMessage();
                                                aiMessage.setContent(answer);
                                                consumer.accept(aiMessage);
                                            }
                                        }
                                    }
                                }
                            }
                            return;
                        }
                        ChatMessage message = choice.getMessage();
                        if(message != null) {
                            if (message.getContent() != null || !StringUtils.isEmpty(message.getReasoningContent())) {
                                answerContent.get().thinkCollect(message.getReasoningContent());
                                if(message.getContent() != null) {
                                    answerContent.get().collect(String.valueOf(message.getContent()));
                                }
                                String answer = answerContent.get().joining();

                                if (answer != null) {
                                    if (consumer != null) {
                                        log.warn(apiCode + " stream answer:" + answer);
                                        AIMessage aiMessage = new AIMessage();
                                        aiMessage.setContent(answer);
                                        consumer.accept(aiMessage);
                                    }
                                }
                            } else if (message.getFunctionCall() != null && message.getFunctionCall().size() > 0) {
                                Map<String, Object> functionCallMap = Maps.newHashMap();
                                if (message.getFunctionCall().get("name") != null) {
                                    functionCallNameContent.get().collect(message.getFunctionCall().get("name").toString());
                                }
                                if (message.getFunctionCall().get("arguments") != null) {
                                    argumentContent.get().collect(message.getFunctionCall().get("arguments").toString());
                                }
                                functionCallMap.put("function_call", new HashMap<String, String>() {{
                                    put("name", functionCallNameContent.get().joining());
                                    put("arguments", argumentContent.get().joining());
                                }});
                                functionCallContent.set(functionCallMap);

                                if (functionCallContent.get() != null) {
                                    if (consumer != null) {
                                        String functionCallContentString = JSON.toJSONString(functionCallContent.get());
                                        log.warn(apiCode + " stream answer:" + functionCallContentString);
                                        AIMessage aiMessage = new AIMessage();
                                        aiMessage.setAdditionalKwargs((Map<String, Object>) functionCallContent.get());
//                                        aiMessage.setContent(JSON.toJSONString(aiMessage.getAdditionalKwargs()));
                                        consumer.accept(aiMessage);
                                    }
                                }
                            } else if (message.getToolCalls() != null && message.getToolCalls().size() > 0) {
                                Map<String, Object> toolCallMap = message.getToolCalls().get(0);
                                // TODO 看下这里格式对不对
                                if(toolCallMap.get("function") != null) {
                                    Object functionObj = toolCallMap.get("function");
                                    Map<String, Object> functionMap = (Map<String, Object>) functionObj;
                                    if (functionMap.get("name") != null) {
                                        functionCallNameContent.get().collect(functionMap.get("name").toString());
                                    }
                                    if (functionMap.get("arguments") != null) {
                                        argumentContent.get().collect(functionMap.get("arguments").toString());
                                    }

                                    Map<String, Object> toolCall = new HashMap<>();
                                    toolCall.put("function", new HashMap<String, Object>() {{
                                        put("name", functionCallNameContent.get().joining());
                                        put("arguments", argumentContent.get().joining());
                                    }});
                                    toolCall.put("type", "function");
                                    toolCall.put("id", "tooluse_" + UUID.randomUUID());

                                    List<Map<String, Object>> toolCallList = new ArrayList<>();
                                    toolCallList.add(toolCall);

                                    Map<String, Object> toolCalls = new HashMap<>();
                                    toolCalls.put("tool_calls", toolCallList);
                                    functionCallContent.set(toolCalls);

                                    if (functionCallContent.get() != null) {
                                        if (consumer != null) {
                                            String functionCallContentString = JSON.toJSONString(functionCallContent.get());
                                            log.warn(apiCode + " stream answer:" + functionCallContentString);
                                            AIMessage aiMessage = new AIMessage();
                                            aiMessage.setAdditionalKwargs((Map<String, Object>) functionCallContent.get());
//                                        aiMessage.setContent(JSON.toJSONString(aiMessage.getAdditionalKwargs()));
                                            consumer.accept(aiMessage);
                                        }
                                    }
                                }
                            }
                        }
                    });
        } else {
            ApiGatewayResult apiGatewayResult = service.execute(request, requestId);
            String apiGatewayResultString = JSON.toJSONString(apiGatewayResult);
            log.info(apiCode + " appEngineResult is " + apiGatewayResultString);

            if(!apiGatewayResult.getSuccess()) {
                throw new ApiGatewayLLMException(apiGatewayResult, apiCode + " apiGatewayResult is error, reason is " + apiGatewayResultString, requestId);
//                throw new RuntimeException("apiGatewayResult is error, reason is " + apiGatewayResultString);
            }
            if(apiGatewayResult.getData() == null) {
                log.error("apiGatewayResult data is null");
                return;
            }

            String result = apiGatewayResult.getData().getResult();
            if(StringUtils.isEmpty(result)) {
                log.error("apiGatewayResult data result is null");
                return;
            }

            log.info(apiCode + " apiGatewayResult is " + result);
            if(async) {
                AIMessage message = buildAsyncTaskMessage(result);
                baseMessage.set(message);
                return;
            }

            Map<String, Object> resultMap = JSON.parseObject(result, new TypeReference<Map<String, Object>>(){});
            Map<String, Object> dataMap = (Map<String, Object>) resultMap.get("data");
            Map<String, Object> structDataMap = (Map<String, Object>) dataMap.get("structData");
            Map<String, Object> llmResultMap = (Map<String, Object>) structDataMap.get("message");
            Map<String, Object> llmDataMap =  (Map<String, Object>)  llmResultMap.get("data");

            com.alibaba.langengine.openmanus.appbuilder.completion.ChatCompletionResult appEngineResult = JSON.parseObject(JSON.toJSONBytes(llmDataMap), com.alibaba.langengine.openmanus.appbuilder.completion.ChatCompletionResult.class);
            String appEngineResultString = result;
            
            log.info(apiCode + " invoke gpt apiGatewayResult result is " + appEngineResultString);
            if(appEngineResult.getChoices() == null || appEngineResult.getChoices().size() == 0) {
                throw new RuntimeException(apiCode + " invoke gpt apiGatewayResult choices is empty, reason is " + appEngineResultString);
            }
            Object answer;
            ChatMessage message = appEngineResult.getChoices().get(0).getMessage();

            // 是有可能同时有getFunctionCall，也有message的情况的
            if (message.getFunctionCall() != null && !message.getFunctionCall().isEmpty()) {
//                Map<String, Object> functionCallMap = new HashMap<>();
//                List<Map<String, Object>> toolCalls = new ArrayList<>();
//                Map<String, Object> toolCall = new HashMap<>();
//                Map<String, Object> function = JSON.parseObject(JSON.toJSONString(message.getFunctionCall()), new TypeReference<Map<String, Object>>() {});
//                toolCall.put("function", function);
//                toolCalls.add(toolCall);
//
//                functionCallMap.put("tool_calls", toolCalls);
//                functionCallContent.set(functionCallMap);

                Map<String, Object> functionCallMap = Maps.newHashMap();
                functionCallMap.put("function_call", message.getFunctionCall());
                functionCallContent.set(functionCallMap);
            }
            if (message.getContent() != null || !StringUtils.isEmpty(message.getReasoningContent())){
                answer = message.getContent();
                if (answer != null) {
                    answerContent.get().collect(String.valueOf(answer));
                }
                answerContent.get().thinkCollectAll(message.getReasoningContent());
            }
        }

        if(functionCallContent.get() != null) {
            AIMessage aiMessage = new AIMessage();
            aiMessage.setAdditionalKwargs((Map<String, Object>)functionCallContent.get());
            aiMessage.setContent( answerContent.get().joining());
            baseMessage.set(aiMessage);
            log.info("functionCallContent get:" + JSON.toJSONString(aiMessage));
            return;
        }

        String responseContent = answerContent.get().joining();
        responseContent = LLMUtils.interceptAnswerWithStopsSplit(responseContent, stops);

        log.warn(apiCode + " final answer:" + responseContent);

        AIMessage aiMessage = new AIMessage();
        aiMessage.setContent(responseContent);
        baseMessage.set(aiMessage);
    }

    private void runQwen(Map<String, Object> request,
                         AtomicReference<BaseMessage> baseMessage,
                         AtomicReference<ResponseCollector> answerContent,
                         AtomicReference<Object> functionCallContent,
                         AtomicReference<ResponseCollector> functionCallNameContent,
                         AtomicReference<ResponseCollector> argumentContent,
                         boolean currentStream,
                         Consumer<BaseMessage> consumer,
                         List<String> stops) {
        log.info("runQwen:" + apiCode + ":" + System.currentTimeMillis());
        if (currentStream) {
            service.executeStream(request, requestId)
                    .doOnError(Throwable::printStackTrace)
                    .blockingForEach(apiGatewayResult -> {
                        String apiGatewayResultString = JSON.toJSONString(apiGatewayResult);
                        log.info(apiCode + " stream apiGatewayResult is " + apiGatewayResultString);

                        if(!apiGatewayResult.getSuccess()) {
                            throw new ApiGatewayLLMException(apiGatewayResult, apiCode + " apiGatewayResult is error, reason is " + apiGatewayResultString, requestId);
//                            throw new RuntimeException("apiGatewayResult is error, reason is " + apiGatewayResultString);
                        }
                        if(apiGatewayResult.getData() == null) {
                            log.error("apiGatewayResult data is null");
                            return;
                        }
                        String result = apiGatewayResult.getData().getResult();
                        if(StringUtils.isEmpty(result)) {
                            log.error("apiGatewayResult data result is null");
                            return;
                        }

                        AppEngineResultData appEngineResult = JSON.parseObject(result, AppEngineResultData.class);
                        log.info(apiCode + " stream apiGatewayResult result is " + result);

                        if (appEngineResult.getChoices() == null
                                || appEngineResult.getChoices().size() == 0) {
                            throw new RuntimeException("apiGatewayResult choices is empty, reason is " + result);
                        }

                        String finishReason = appEngineResult.getChoices().get(0).getFinishReason();
                        if(finishReason != null) {
                            if(finishReason.equals("stop")) {
                                AppEngineChoice choice = appEngineResult.getChoices().get(0);
                                ChatMessage chatMessage = choice.getMessage();
                                if(chatMessage != null) {
                                    chatMessage.setRole("assistant");
                                    BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
                                    baseMessage.set(message);
                                }
                            } else if(finishReason.equals("function_call")){
                                AppEngineChoice choice = appEngineResult.getChoices().get(0);
                                log.info("function_call:{}", JSON.toJSONString(choice));
                                ChatMessage chatMessage;
                                if(choice.getDelta() != null) {
                                    chatMessage = choice.getDelta();
                                } else {
                                    chatMessage = choice.getMessage();
                                }
                                answerContent.get().thinkCollect(chatMessage.getReasoningContent());
                                if(chatMessage.getContent() != null) {
                                    answerContent.get().collect(chatMessage.getContent().toString());
                                }
                                chatMessage.setRole("assistant");
                                BaseMessage message = MessageConverter.convertChatMessageToMessage(chatMessage);
//                                functionCallMap.put("function_call", new HashMap<String, String>() {{
//                                    put("name", functionCallNameContent.get().joining());
//                                    put("arguments", argumentContent.get().joining());
//                                }});
                                String response = answerContent.get().joining();
                                message.setContent(response);
                                functionCallContent.set(message.getAdditionalKwargs());

                                baseMessage.set(message);
                            }
                        } else {
                            String reasoningContent = null;
                            String responseContent = null;
                            ChatMessage deltaMessage = appEngineResult.getChoices().get(0).getDelta();
                            ChatMessage chatMessage = appEngineResult.getChoices().get(0).getMessage();
                            if(deltaMessage != null) {
                                if(deltaMessage.getContent() != null) {
                                    responseContent = deltaMessage.getContent().toString();
                                }
                                if(!StringUtils.isEmpty(deltaMessage.getReasoningContent())) {
                                    reasoningContent = deltaMessage.getReasoningContent();
                                }
                            } else if(chatMessage != null) {
                                if(chatMessage.getContent() != null) {
                                    responseContent = chatMessage.getContent().toString();
                                }
                                if(!StringUtils.isEmpty(chatMessage.getReasoningContent())) {
                                    reasoningContent = chatMessage.getReasoningContent();
                                }
                            }
                            answerContent.get().thinkCollect(reasoningContent);
                            answerContent.get().collect(responseContent);
                            AIMessage message = new AIMessage();
                            message.setContent(answerContent.get().joining());
                            log.warn(apiCode + " stream message:" + JSON.toJSONString(message));
                            if (message != null) {
                                baseMessage.set(message);
                                if (consumer != null) {
                                    consumer.accept(message);
                                }
                            }
                        }});


            if(functionCallContent.get() != null) {
                Map<String, Object> functionCall = (Map<String, Object>)functionCallContent.get();
                log.warn(apiCode + " final answer:" + JSON.toJSONString(functionCall));
                AIMessage aiMessage = new AIMessage();
                aiMessage.setAdditionalKwargs(functionCall);
                baseMessage.set(aiMessage);
                return;
            }

            String responseContent = answerContent.get().joining();
            responseContent = LLMUtils.interceptAnswerWithStopsSplit(responseContent, stops);

            log.warn(apiCode + " final answer:" + responseContent);

            AIMessage aiMessage = new AIMessage();
            aiMessage.setContent(responseContent);
            baseMessage.set(aiMessage);

        } else {
            ApiGatewayResult apiGatewayResult = service.execute(request, requestId);
            String apiGatewayResultString = JSON.toJSONString(apiGatewayResult);
            log.info(apiCode + " apiGatewayResult is " + apiGatewayResultString);

            if(!apiGatewayResult.getSuccess()) {
                throw new ApiGatewayLLMException(apiGatewayResult, apiCode + " apiGatewayResult is error, reason is " + apiGatewayResultString, requestId);
//                throw new RuntimeException("apiGatewayResult is error, reason is " + apiGatewayResultString);
            }
            if(apiGatewayResult.getData() == null) {
                log.error("apiGatewayResult data is null");
                return;
            }
            String result = apiGatewayResult.getData().getResult();
            if(StringUtils.isEmpty(result)) {
                log.error("apiGatewayResult data result is null");
                return;
            }

            log.info(apiCode + " apiGatewayResult result is " + result);
            if(async) {
                AIMessage message = buildAsyncTaskMessage(result);
                baseMessage.set(message);
                return;
            }
            AppEngineResultData appEngineResult = JSON.parseObject(result, AppEngineResultData.class);

            if (appEngineResult.getChoices() == null
                    || appEngineResult.getChoices().size() == 0) {
                throw new RuntimeException("apiGatewayResultData choices is empty:" + result);
            }

            String responseContent = appEngineResult.getChoices().get(0).getMessage().getContent().toString();
            responseContent = LLMUtils.interceptAnswerWithStopsSplit(responseContent, stops);
//            if (containUsage) {
//                UsageAnswerResult usageAnswerResult = new UsageAnswerResult();
//                usageAnswerResult.setAnswer(responseContent);
//                usageAnswerResult.setInputTokens(appEngineResult.getUsage() != null ? appEngineResult.getUsage().getPromptTokens() : 0L);
//                usageAnswerResult.setOutputTokens(appEngineResult.getUsage() != null ? appEngineResult.getUsage().getCompletionTokens() : 0L);
//
//                responseContent = JSON.toJSONString(usageAnswerResult);
//                log.warn(model + " answer:" + responseContent);
//            } else {
                log.warn(apiCode + " answer:" + responseContent);
//            }
            AIMessage message = new AIMessage();
            message.setContent(responseContent);
            baseMessage.set(message);
        }
    }

    private String getGatewayPath(String env) {
        if(StringUtils.isEmpty(env)) {
            return gatewayInvokePathTemplatePathMap.get("production");
        }
        String path = gatewayInvokePathTemplatePathMap.get(env);
        if(StringUtils.isEmpty(path)) {
            return gatewayInvokePathTemplatePathMap.get("production");
        }
        return path;
    }

    private String generateFinalPrompt(String original) {
        return LLMUtils.generateFinalPrompt(original, autoLlmFlag, modelType.name());
    }

    private AIMessage buildAsyncTaskMessage(String result) {
        String taskId = null;
        Map<String, Object> taskResult = JSON.parseObject(result, Map.class);
        if(taskResult != null && taskResult.get("taskId") != null) {
            taskId = (String)taskResult.get("taskId");
        }
        AIMessage message = new AIMessage();
        message.setContent(taskId);
        return message;
    }

    private boolean isGptProxy() {
        return openaiFormat;
    }

    private boolean isStandardToolCall() {
        return toolCallFormat;
    }

    public static void main(String[] args) {
        ApiGatewayChatModel llm = new ApiGatewayChatModel(
                "290899077434863616",
                null,
                ApiGatewayModelType.OPENAI,
                "production",
                "053049-e6c3-MuivW-nmhedsfyIr8zHQ");

        List<BaseMessage> messages = new ArrayList<>();

        HumanMessage humanMessage = new HumanMessage();
        humanMessage.setContent("今天杭州天气怎么样？");
        messages.add(humanMessage);

        List<FunctionDefinition> functions = new ArrayList<>();
        FunctionDefinition functionDefinition = new FunctionDefinition();
        functionDefinition.setName("get_current_weather");
        functionDefinition.setDescription("Get the current weather in a given location.");
        FunctionParameter functionParameter = new FunctionParameter();
        functionParameter.setRequired(Arrays.asList(new String[] { "location" }));

        Map<String, FunctionProperty> propertyMap = new HashMap<>();

        FunctionProperty functionProperty = new FunctionProperty();
        functionProperty.setType("string");
        functionProperty.setDescription("The city and state, e.g. San Francisco, CA");
        propertyMap.put("location", functionProperty);

        functionProperty = new FunctionProperty();
        functionProperty.setType("string");
        List<String> enums = new ArrayList<>();
        enums.add("celsius");
        enums.add("fahrenheit");
        functionProperty.setEnums(enums);
        functionProperty.setDescription("The temperature unit.");
        propertyMap.put("unit", functionProperty);

        functionParameter.setProperties(propertyMap);
        functionDefinition.setParameters(functionParameter);
        functions.add(functionDefinition);

        BaseMessage baseMessage = llm.run(messages, functions, null, null, null);
        System.out.println("response:" + JSON.toJSONString(baseMessage));
    }
}
