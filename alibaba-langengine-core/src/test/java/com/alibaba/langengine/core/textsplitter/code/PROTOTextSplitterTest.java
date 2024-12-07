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
package com.alibaba.langengine.core.textsplitter.code;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.indexes.Document;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PROTOTextSplitterTest {

    @Test
    public void test_createDocuments() {
        // success
        PROTOTextSplitter textSplitter = new PROTOTextSplitter();
        textSplitter.setMaxChunkSize(16);
        textSplitter.setMaxChunkOverlap(0);
        textSplitter.setSeparatorRegex(true);
        String text = "syntax = \"proto3\";\n" +
                "\n" +
                "package example;\n" +
                "\n" +
                "message Person {\n" +
                "    string name = 1;\n" +
                "    int32 age = 2;\n" +
                "    repeated string hobbies = 3;\n" +
                "}";
        List<Document> documents = textSplitter.createDocuments(Arrays.asList(new String[] { text }), new ArrayList<>());
        System.out.println(JSON.toJSONString(documents));
    }
}