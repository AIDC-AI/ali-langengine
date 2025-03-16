package com.alibaba.langengine.openmanus.appbuilder.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.model.fastchat.service.RetrofitInitService;
import io.reactivex.Flowable;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.net.Proxy;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class ApiGatewayChatService extends RetrofitInitService<ApiGatewayApi> {

    public ApiGatewayChatService() {
        super();
    }

    public ApiGatewayChatService(String serverUrl, Duration timeout, boolean authentication, String token) {
        super(serverUrl, timeout, authentication, token);
    }

    public ApiGatewayChatService(String serverUrl, Duration timeout, boolean authentication, String token, Proxy proxy) {
        super(serverUrl, timeout, authentication, token, proxy);
    }

    @Override
    public Class<ApiGatewayApi> getServiceApiClass() {
        return ApiGatewayApi.class;
    }

    public ApiGatewayResult execute(Map<String, Object> request, String requestId) {
        Map<String, String> headers = new HashMap<>();
        if(!StringUtils.isEmpty(requestId)) {
            headers.put("Eagleeye-Traceid", requestId);
        }

        String uniqueId = MapUtils.getString(request, "uniqueId");
        if(StringUtils.isNotEmpty(uniqueId)) {
            headers.put("apaas-unique-id", uniqueId);
        }

        String aibRpcId = MapUtils.getString(request, "aibRpcId");
        if(StringUtils.isNotEmpty(aibRpcId)) {
            headers.put("apaas-aib-rpc-id", aibRpcId);
        }

        headers.put("ak", getToken());
        log.info("ApiGatewayChatService execute headers is " + JSON.toJSONString(headers));
        return execute(getApi().execute("/common/invoke", request, headers));
    }

    public Flowable<ApiGatewayResult> executeStream(Map<String, Object> request, String requestId) {
        Map<String, String> headers = new HashMap<>();
        if(!StringUtils.isEmpty(requestId)) {
            headers.put("Eagleeye-Traceid", requestId);
        }

        String uniqueId = MapUtils.getString(request, "uniqueId");
        if(StringUtils.isNotEmpty(uniqueId)) {
            headers.put("apaas-unique-id", uniqueId);
        }

        String aibRpcId = MapUtils.getString(request, "aibRpcId");
        if(StringUtils.isNotEmpty(aibRpcId)) {
            headers.put("apaas-aib-rpc-id", aibRpcId);
        }

        headers.put("ak", getToken());
        log.info("ApiGatewayChatService executeStream headers is " + JSON.toJSONString(headers));
        return stream(getApi().executeStream("/common/invoke",  request, headers), ApiGatewayResult.class);
    }
}
