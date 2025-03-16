package com.alibaba.langengine.openmanus.appbuilder.service;

import io.reactivex.Single;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface ApiGatewayApi {

    @POST
    Single<ApiGatewayResult> execute(@Url String url, @Body Map<String, Object> request, @HeaderMap Map<String, String> headers);

    @Streaming
    @POST
    Call<ResponseBody> executeStream(@Url String url, @Body Map<String, Object> request, @HeaderMap Map<String, String> headers);
}
