package com.alibaba.langengine.openmanus.appbuilder.service;

import lombok.Data;

@Data
public class ApiGatewayResult {

    private Integer code;
    private String message;
    private Boolean success;
    private String requestId;

    private ApiGatewayResultData data;
}
