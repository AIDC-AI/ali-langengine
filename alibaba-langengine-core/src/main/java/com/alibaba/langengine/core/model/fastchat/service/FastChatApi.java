/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.core.model.fastchat.service;

import com.alibaba.langengine.core.model.fastchat.OpenAiResponse;
import com.alibaba.langengine.core.model.fastchat.audio.Text2SpeechRequest;
import com.alibaba.langengine.core.model.fastchat.completion.CompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.CompletionResult;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionRequest;
import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatCompletionResult;
import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingRequest;
import com.alibaba.langengine.core.model.fastchat.embedding.EmbeddingResult;
import com.alibaba.langengine.core.model.fastchat.file.File;
import com.alibaba.langengine.core.model.fastchat.finetune.FineTuneEvent;
import com.alibaba.langengine.core.model.fastchat.finetune.FineTuneRequest;
import com.alibaba.langengine.core.model.fastchat.finetune.FineTuneResult;
import com.alibaba.langengine.core.model.fastchat.messages.Message;
import com.alibaba.langengine.core.model.fastchat.messages.MessageFile;
import com.alibaba.langengine.core.model.fastchat.messages.MessageRequest;
import com.alibaba.langengine.core.model.fastchat.messages.ModifyMessageRequest;
import com.alibaba.langengine.core.model.fastchat.moderation.ModerationRequest;
import com.alibaba.langengine.core.model.fastchat.moderation.ModerationResult;
import com.alibaba.langengine.core.model.fastchat.runs.*;
import com.alibaba.langengine.core.model.fastchat.threads.Thread;
import com.alibaba.langengine.core.model.fastchat.threads.ThreadRequest;
import com.alibaba.langengine.core.model.fastchat.engine.Engine;
import com.alibaba.langengine.core.model.fastchat.image.CreateImageRequest;
import com.alibaba.langengine.core.model.fastchat.image.ImageResult;
import com.alibaba.langengine.core.model.fastchat.model.Model;

import com.alibaba.langengine.core.model.fastchat.assistants.*;
import io.reactivex.Single;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

/**
 * FastChat API
 *
 * @author xiaoxuan.lp
 */
public interface FastChatApi {

    @GET("v1/models")
    Single<FastChatResponse<Model>> listModels();

    @GET("v1/models/{model_id}")
    Single<Model> getModel(@Path("model_id") String modelId);

    @POST("v1/completions")
    Single<CompletionResult> createCompletion(@Body CompletionRequest request);

    @Streaming
    @POST("v1/completions")
    Call<ResponseBody> createCompletionStream(@Body CompletionRequest request);

    @POST("v1/chat/completions")
    Single<ChatCompletionResult> createChatCompletion(@Body ChatCompletionRequest request);

    @Streaming
    @POST("v1/chat/completions")
    Call<ResponseBody> createChatCompletionStream(@Body ChatCompletionRequest request);

    @POST("v1/embeddings")
    Single<EmbeddingResult> createEmbeddings(@Body EmbeddingRequest request);

    //    @POST("/{embeddings_path}")
    //    Single<Object> createGenericEmbeddings(@Path("embeddings_path") String embeddingsPath, @Body
    //    EmbeddingRequest request);

    @Deprecated
    @POST("v1/engines/{engine_id}/embeddings")
    Single<EmbeddingResult> createEmbeddings(@Path("engine_id") String engineId, @Body EmbeddingRequest request);

    @GET("v1/files")
    Single<FastChatResponse<File>> listFiles();

    @Multipart
    @POST("v1/files")
    Single<File> uploadFile(@Part("purpose") RequestBody purpose, @Part MultipartBody.Part file);

    @DELETE("v1/files/{file_id}")
    Single<DeleteResult> deleteFile(@Path("file_id") String fileId);

    @GET("v1/files/{file_id}")
    Single<File> retrieveFile(@Path("file_id") String fileId);

    @POST("v1/fine-tunes")
    Single<FineTuneResult> createFineTune(@Body FineTuneRequest request);

    @POST("v1/completions")
    Single<CompletionResult> createFineTuneCompletion(@Body CompletionRequest request);

    @GET("v1/fine-tunes")
    Single<FastChatResponse<FineTuneResult>> listFineTunes();

    @GET("v1/fine-tunes/{fine_tune_id}")
    Single<FineTuneResult> retrieveFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("v1/fine-tunes/{fine_tune_id}/cancel")
    Single<FineTuneResult> cancelFineTune(@Path("fine_tune_id") String fineTuneId);

    @GET("v1/fine-tunes/{fine_tune_id}/events")
    Single<FastChatResponse<FineTuneEvent>> listFineTuneEvents(@Path("fine_tune_id") String fineTuneId);

    @DELETE("v1/models/{fine_tune_id}")
    Single<DeleteResult> deleteFineTune(@Path("fine_tune_id") String fineTuneId);

    @POST("v1/images/generations")
    Single<ImageResult> createImage(@Body CreateImageRequest request);

    @POST("v1/images/edits")
    Single<ImageResult> createImageEdit(@Body RequestBody requestBody);

    @POST("v1/images/variations")
    Single<ImageResult> createImageVariation(@Body RequestBody requestBody);

    @POST("v1/moderations")
    Single<ModerationResult> createModeration(@Body ModerationRequest request);

    @Deprecated
    @GET("v1/engines")
    Single<FastChatResponse<Engine>> getEngines();

    @Deprecated
    @GET("v1/engines/{engine_id}")
    Single<Engine> getEngine(@Path("engine_id") String engineId);

    /**
     * 生成音频/v1/audio/speech
     */
    @POST("v1/audio/speech")
    Single<ResponseBody> createAudioSpeech(@Body Text2SpeechRequest speechRequest);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("v1/assistants")
    Single<Assistant> createAssistant(@Body AssistantRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/assistants/{assistant_id}")
    Single<Assistant> retrieveAssistant(@Path("assistant_id") String assistantId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("v1/assistants/{assistant_id}")
    Single<Assistant> modifyAssistant(@Path("assistant_id") String assistantId, @Body ModifyAssistantRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("v1/assistants/{assistant_id}")
    Single<DeleteResult> deleteAssistant(@Path("assistant_id") String assistantId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/assistants")
    Single<OpenAiResponse<Assistant>> listAssistants(@QueryMap Map<String, Object> filterRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("v1/assistants/{assistant_id}/files")
    Single<AssistantFile> createAssistantFile(@Path("assistant_id") String assistantId, @Body AssistantFileRequest fileRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/assistants/{assistant_id}/files/{file_id}")
    Single<AssistantFile> retrieveAssistantFile(@Path("assistant_id") String assistantId, @Path("file_id") String fileId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("v1/assistants/{assistant_id}/files/{file_id}")
    Single<DeleteResult> deleteAssistantFile(@Path("assistant_id") String assistantId, @Path("file_id") String fileId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/assistants/{assistant_id}/files")
    Single<OpenAiResponse<Assistant>> listAssistantFiles(@Path("assistant_id") String assistantId, @QueryMap Map<String, Object> filterRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("v1/threads")
    Single<Thread> createThread(@Body ThreadRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/threads/{thread_id}")
    Single<Thread> retrieveThread(@Path("thread_id") String threadId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("v1/threads/{thread_id}")
    Single<Thread> modifyThread(@Path("thread_id") String threadId, @Body ThreadRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @DELETE("v1/threads/{thread_id}")
    Single<DeleteResult> deleteThread(@Path("thread_id") String threadId);


    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("v1/threads/{thread_id}/messages")
    Single<Message> createMessage(@Path("thread_id") String threadId, @Body MessageRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/threads/{thread_id}/messages/{message_id}")
    Single<Message> retrieveMessage(@Path("thread_id") String threadId, @Path("message_id") String messageId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @POST("v1/threads/{thread_id}/messages/{message_id}")
    Single<Message> modifyMessage(@Path("thread_id") String threadId, @Path("message_id") String messageId, @Body ModifyMessageRequest request);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/threads/{thread_id}/messages")
    Single<OpenAiResponse<Message>> listMessages(@Path("thread_id") String threadId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/threads/{thread_id}/messages")
    Single<OpenAiResponse<Message>> listMessages(@Path("thread_id") String threadId, @QueryMap Map<String, Object> filterRequest);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/threads/{thread_id}/messages/{message_id}/files/{file_id}")
    Single<MessageFile> retrieveMessageFile(@Path("thread_id") String threadId, @Path("message_id") String messageId, @Path("file_id") String fileId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/threads/{thread_id}/messages/{message_id}/files")
    Single<OpenAiResponse<MessageFile>> listMessageFiles(@Path("thread_id") String threadId, @Path("message_id") String messageId);

    @Headers({"OpenAI-Beta: assistants=v1"})
    @GET("v1/threads/{thread_id}/messages/{message_id}/files")
    Single<OpenAiResponse<MessageFile>> listMessageFiles(@Path("thread_id") String threadId, @Path("message_id") String messageId, @QueryMap Map<String, Object> filterRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("v1/threads/{thread_id}/runs")
    Single<Run> createRun(@Path("thread_id") String threadId, @Body RunCreateRequest runCreateRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("v1/threads/{thread_id}/runs/{run_id}")
    Single<Run> retrieveRun(@Path("thread_id") String threadId, @Path("run_id") String runId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("v1/threads/{thread_id}/runs/{run_id}")
    Single<Run> modifyRun(@Path("thread_id") String threadId, @Path("run_id") String runId, @Body Map<String, String> metadata);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("v1/threads/{thread_id}/runs")
    Single<OpenAiResponse<Run>> listRuns(@Path("thread_id") String threadId, @QueryMap Map<String, String> listSearchParameters);


    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/{thread_id}/runs/{run_id}/submit_tool_outputs")
    Single<Run> submitToolOutputs(@Path("thread_id") String threadId, @Path("run_id") String runId, @Body SubmitToolOutputsRequest submitToolOutputsRequest);


    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/{thread_id}/runs/{run_id}/cancel")
    Single<Run> cancelRun(@Path("thread_id") String threadId, @Path("run_id") String runId);

    @Headers("OpenAI-Beta: assistants=v1")
    @POST("/v1/threads/runs")
    Single<Run> createThreadAndRun(@Body CreateThreadAndRunRequest createThreadAndRunRequest);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("/v1/threads/{thread_id}/runs/{run_id}/steps/{step_id}")
    Single<RunStep> retrieveRunStep(@Path("thread_id") String threadId, @Path("run_id") String runId, @Path("step_id") String stepId);

    @Headers("OpenAI-Beta: assistants=v1")
    @GET("/v1/threads/{thread_id}/runs/{run_id}/steps")
    Single<OpenAiResponse<RunStep>> listRunSteps(@Path("thread_id") String threadId, @Path("run_id") String runId, @QueryMap Map<String, String> listSearchParameters);
}
