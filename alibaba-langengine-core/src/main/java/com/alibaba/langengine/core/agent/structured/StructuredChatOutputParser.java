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
package com.alibaba.langengine.core.agent.structured;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.langengine.core.agent.AgentAction;
import com.alibaba.langengine.core.agent.AgentOutputParser;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StructuredChatOutputParser
 *
 * @author xiaoxuan.lp
 */
@Slf4j
@Data
public class StructuredChatOutputParser extends AgentOutputParser {

    private static final String FINAL_ANSWER_ACTION = "Final Answer";

    @Override
    public String getFormatInstructions() {
        return PromptConstants.FORMAT_INSTRUCTIONS;
    }

    @Override
    public String getParserType() {
        return "structured_chat";
    }

    @Override
    public Object parse(String text) {
        String regex = "```(.*?)```?";
        Pattern pattern = Pattern.compile(regex, Pattern.DOTALL);
        Matcher matcher = pattern.matcher(text);
        if(matcher.find()) {
            String actionInput = matcher.group(1);
            actionInput = actionInput.replaceAll("\\{\\{\\{\\{", "{")
                    .replaceAll("\\}\\}\\}\\}", "}")
                    .replaceAll("\\{\\{\\{", "{")
                    .replaceAll("\\}\\}\\}", "}")
                    .replaceAll("\\{\\{", "{")
                    .replaceAll("\\}\\}", "}");
            // 兼容下逻辑，如果是非JSON格式，则直接返回
            try {
                Object response = JSON.parse(actionInput);
                if(response instanceof JSONArray) {
                    log.warn("Got multiple action responses: %s", response);
                    response = ((JSONArray) response).get(0);
                }
                if(!(response instanceof JSONObject)) {
                    return getAgentFinish(text);
                }
                JSONObject responseObj = (JSONObject) response;
                if(responseObj.get("action").toString() != null &&
                    responseObj.get("action").toString().indexOf(FINAL_ANSWER_ACTION) >= 0) {
                    String finalAnswer = responseObj.get("action_input").toString();
                    return getAgentFinish(finalAnswer);
                }
                AgentAction agentAction = new AgentAction();
                agentAction.setTool(responseObj.get("action").toString());
                agentAction.setToolInput(JSON.toJSONString(responseObj.get("action_input")));
                agentAction.setLog(text);
                return agentAction;
            }catch (Exception e){
                log.error("Error parsing action input: {}", actionInput, e);
                return getAgentFinish(text);
            }
        } else {
            return getAgentFinish(text);
        }
    }
}
