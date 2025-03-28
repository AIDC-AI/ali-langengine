package com.alibaba.langengine.openmanus.appbuilder.completion;

import com.alibaba.langengine.core.model.fastchat.completion.chat.ChatMessage;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * A chat completion generated by Vicuna
 */
@Slf4j
@Data
public class ChatCompletionChoice {

    /**
     * This index of this completion in the returned list.
     */
    Integer index;

    ChatMessage message;

    ChatMessage delta;

    public ChatMessage getMessage() {
//        log.info("ChatCompletionChoice message is {} delta is {}", JSON.toJSONString(message), JSON.toJSONString(delta));
        if(message == null) {
            return delta;
        }
        return message;
    }

    /**
     * The reason why GPT stopped generating, for example "length".
     */
    @JsonProperty("finish_reason")
    String finishReason;
}
