/**
 * Copyright (C) 2024 AIDC-AI
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.core.model.fastchat;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

/**
 * A wrapper class to fit the OpenAI engine and search endpoints
 */
@Data
public class OpenAiResponse<T> {
    /**
     * A list containing the actual results
     */
    public List<T> data;

    /**
     * The type of object returned, should be "list"
     */
    public String object;

    /**
     * The first id included
     */
    @JsonProperty("first_id")
    public String firstId;

    /**
     * The last id included
     */
    @JsonProperty("last_id")
    public String lastId;

    /**
     * True if there are objects after lastId
     */
    @JsonProperty("has_more")
    public boolean hasMore;
}
