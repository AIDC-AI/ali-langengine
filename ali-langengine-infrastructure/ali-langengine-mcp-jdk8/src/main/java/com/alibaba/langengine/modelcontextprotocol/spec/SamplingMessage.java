/*
 * Copyright 2025 Alibaba Group Holding Ltd.
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
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Objects;

/**
 * A message for sampling.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class SamplingMessage {
    private final Role role;
    private final Content content;

    public SamplingMessage(
            @JsonProperty("role") Role role,
            @JsonProperty("content") Content content) {
        this.role = role;
        this.content = content;
    }

    public Role role() {
        return role;
    }

    public Content content() {
        return content;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SamplingMessage that = (SamplingMessage) o;
        return role == that.role &&
               Objects.equals(content, that.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(role, content);
    }

    @Override
    public String toString() {
        return "SamplingMessage{" +
               "role=" + role +
               ", content=" + content +
               '}';
    }
}
