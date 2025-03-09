/**
 * Copyright (C) 2024 AIDC-AI
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.langengine.deepsearch.utils;

import com.alibaba.langengine.core.indexes.Document;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class VectorStoreUtils {

    public static List<Document> deduplicateResults(List<Document> results) {
        Set<String> allTextSet = new HashSet<>();
        List<Document> deduplicatedResults = new ArrayList<>();

        for (Document result : results) {
            if (!allTextSet.contains(result.getPageContent())) {
                allTextSet.add(result.getPageContent());
                deduplicatedResults.add(result);
            }
        }
        return deduplicatedResults;
    }
}
