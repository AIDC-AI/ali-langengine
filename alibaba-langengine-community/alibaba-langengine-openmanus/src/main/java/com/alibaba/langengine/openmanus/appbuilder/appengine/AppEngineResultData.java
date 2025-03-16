package com.alibaba.langengine.openmanus.appbuilder.appengine;

import lombok.Data;

import java.util.List;

@Data
public class AppEngineResultData {

    private String id;

    private String object;

    private Integer created;

    private String model;

    private AppEngineUsage usage;

    private List<AppEngineChoice> choices;
}
