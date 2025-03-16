package com.alibaba.langengine.openmanus.appbuilder.service;

public class ApiGatewayLLMException extends RuntimeException {

    /**
     * 网关返回错误码
     */
    private ApiGatewayResult apiGatewayResult;

    private String errorMessage;

    private String requestId;

    public ApiGatewayLLMException(ApiGatewayResult apiGatewayResult, String errorMessage, String requestId) {
        super(errorMessage);
        this.apiGatewayResult = apiGatewayResult;
        this.errorMessage = errorMessage;
    }

    public ApiGatewayResult getApiGatewayResult() {
        return apiGatewayResult;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public String getRequestId() {
        return requestId;
    }
}
