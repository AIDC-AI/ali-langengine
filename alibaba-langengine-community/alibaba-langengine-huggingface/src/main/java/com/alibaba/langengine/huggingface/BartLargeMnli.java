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
package com.alibaba.langengine.huggingface;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.huggingface.completion.CompletionRequest;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * This task is super useful to try out classification with zero code, you simply pass a sentence/paragraph and the possible labels for that sentence, and you get a result.
 * 这个任务对于尝试使用零代码进行分类非常有用，您只需传递一个句子/段落和该句子的可能标签，您就会得到一个结果。
 *
 * https://huggingface.co/facebook/bart-large-mnli
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class BartLargeMnli extends HuggingfaceLLM {

    /**
     * 模型名称
     * required
     */
    private String model = "facebook/bart-large-mnli";

    /**
     * a list of strings that are potential classes for inputs. (max 10 candidate_labels, for more, simply run multiple requests, results are going to be misleading if using too many candidate_labels anyway. If you want to keep the exact same, you can simply run multi_label=True and do the scaling on your end. )
     */
    private List<String> candidateLabels = new ArrayList<>();

    /**
     * (Default: true). Boolean. There is a cache layer on the inference API to speedup requests we have already seen. Most models can use those results as is as models are deterministic (meaning the results will be the same anyway). However if you use a non deterministic model, you can set this parameter to prevent the caching mechanism from being used resulting in a real new query.
     */
    private boolean useCache = true;

    /**
     * (Default: false) Boolean. If the model is not ready, wait for it instead of receiving 503. It limits the number of requests required to get your inference done. It is advised to only set this flag to true after receiving a 503 error as it will limit hanging in your application to known places.
     */
    private boolean waitForModel = false;

    /**
     * 是否流模式
     */
    private boolean stream = false;

    public String run(String prompt, List<String> stops, Consumer<String> consumer, Map<String, Object> extraAttributes) {
        CompletionRequest.CompletionRequestBuilder builder = CompletionRequest.builder()
                .inputs(prompt);

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("candidate_labels", candidateLabels);
        builder.parameters(parameters);

        Map<String, Object> options = new HashMap<>();
        options.put("use_cache", useCache);
        options.put("wait_for_model", waitForModel);
        builder.options(options);

        CompletionRequest completionRequest = builder.build();
        List<String> answerContentList = new ArrayList<>();
        Object responseMap = getService().createCompletion(model, completionRequest);
        answerContentList.add(JSON.toJSONString(responseMap));
        String responseContent = answerContentList.stream().collect(Collectors.joining("\n"));
        return responseContent;
    }
}
