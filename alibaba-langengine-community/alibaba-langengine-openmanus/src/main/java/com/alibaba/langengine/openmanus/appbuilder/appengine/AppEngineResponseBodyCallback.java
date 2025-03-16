package com.alibaba.langengine.openmanus.appbuilder.appengine;

import com.alibaba.langengine.core.model.fastchat.service.FastChatError;
import com.alibaba.langengine.core.model.fastchat.service.FastChatHttpException;
import com.alibaba.langengine.core.model.fastchat.service.SSE;
import com.alibaba.langengine.core.model.fastchat.service.SSEFormatException;
import com.alibaba.langengine.core.util.JacksonUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.reactivex.FlowableEmitter;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.HttpException;
import retrofit2.Response;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * 响应回调
 *
 * @author xiaoxuan.lp
 */
public class AppEngineResponseBodyCallback implements Callback<ResponseBody> {
    private static final ObjectMapper mapper = JacksonUtils.defaultObjectMapper();

    private FlowableEmitter<SSE> emitter;
    private boolean emitDone;

    public AppEngineResponseBodyCallback(FlowableEmitter<SSE> emitter, boolean emitDone) {
        this.emitter = emitter;
        this.emitDone = emitDone;
    }

    @Override
    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
        BufferedReader reader = null;

        try {
            if (!response.isSuccessful()) {
                HttpException e = new HttpException(response);
                ResponseBody errorBody = response.errorBody();

                if (errorBody == null) {
                    throw e;
                } else {
                    FastChatError error = mapper.readValue(
                            errorBody.string(),
                            FastChatError.class
                    );
                    throw new FastChatHttpException(error, e, e.code());
                }
            }

            InputStream in = response.body().byteStream();
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            SSE sse = null;

            while (!emitter.isCancelled() && (line = reader.readLine()) != null) {
                if (line.startsWith("data:")) {
                    String data = line.substring(5).trim();
                    sse = new SSE(data);
                    emitter.onNext(sse);
                    sse = null;
                } else {
                    if (line.startsWith("id:")
                            || line.startsWith("event:")
                            || line.startsWith("retry:")
                            || line.startsWith(":HTTP_STATUS/200")) {
                        continue;
                    }
                    throw new SSEFormatException("Invalid sse format! " + line);
                }
            }

            emitter.onComplete();

        } catch (Throwable t) {
            onFailure(call, t);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    // do nothing
                }
            }
        }
    }

    @Override
    public void onFailure(Call<ResponseBody> call, Throwable t) {
        emitter.onError(t);
    }
}
