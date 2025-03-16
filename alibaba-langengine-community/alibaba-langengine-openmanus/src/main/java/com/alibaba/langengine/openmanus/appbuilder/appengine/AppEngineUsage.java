package com.alibaba.langengine.openmanus.appbuilder.appengine;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppEngineUsage {

    @JsonProperty("prompt_tokens")
    private Integer promptTokens;

    @JsonProperty("completion_tokens")
    private Integer completionTokens;

    @JsonProperty("total_tokens")
    private Integer totalTokens;
}
