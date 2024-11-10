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
package com.alibaba.langengine.core.prompt;

import com.alibaba.langengine.core.prompt.impl.PromptTemplate;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

public class PromptTemplateTest {

    @Test
    public void test_format() {
        // success
        String template = "I want you to act as a naming consultant for new companies.\n" +
                "What is a good name for a company that makes {product}?";
        PromptTemplate promptTemplate = new PromptTemplate();
        promptTemplate.setTemplate(template);
        Map<String, Object> context = new HashMap<>();
        context.put("product", "nike");
        String prompt = promptTemplate.format(context);
        System.out.println(prompt);
    }
}