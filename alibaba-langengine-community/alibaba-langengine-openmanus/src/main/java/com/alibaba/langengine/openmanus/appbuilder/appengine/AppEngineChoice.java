package com.alibaba.langengine.openmanus.appbuilder.appengine;

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class AppEngineChoice {

    private Integer index;

    @JsonProperty("finish_reason")
    private String finishReason;

    private ChatMessage message;

    private ChatMessage delta;
}
