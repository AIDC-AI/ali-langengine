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

import java.util.List;
import java.util.Objects;

/**
 * Result of a resources/list request.
 * 
 * JDK 1.8 compatible version.
 */
@JsonInclude(JsonInclude.Include.NON_ABSENT)
@JsonIgnoreProperties(ignoreUnknown = true)
@Data
public class ListResourcesResult {
    private final List<Resource> resources;
    private final String nextCursor;

    public ListResourcesResult(
            @JsonProperty("resources") List<Resource> resources,
            @JsonProperty("nextCursor") String nextCursor) {
        this.resources = resources;
        this.nextCursor = nextCursor;
    }

    public List<Resource> resources() {
        return resources;
    }

    public String nextCursor() {
        return nextCursor;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ListResourcesResult that = (ListResourcesResult) o;
        return Objects.equals(resources, that.resources) &&
               Objects.equals(nextCursor, that.nextCursor);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resources, nextCursor);
    }

    @Override
    public String toString() {
        return "ListResourcesResult{" +
               "resources=" + resources +
               ", nextCursor='" + nextCursor + '\'' +
               '}';
    }
}
