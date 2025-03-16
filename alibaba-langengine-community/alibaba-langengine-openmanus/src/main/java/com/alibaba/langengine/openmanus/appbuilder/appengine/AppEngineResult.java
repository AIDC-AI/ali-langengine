package com.alibaba.langengine.openmanus.appbuilder.appengine;

import lombok.Data;

@Data
public class AppEngineResult {

    private Integer code;

    private String message;

    private AppEngineResultData data;
}
