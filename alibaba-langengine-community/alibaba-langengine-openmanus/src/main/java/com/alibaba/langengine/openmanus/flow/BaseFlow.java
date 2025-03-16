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
package com.alibaba.langengine.openmanus.flow;

import com.alibaba.langengine.openmanus.agent.BaseAgent;

import java.util.List;
import java.util.Map;

public abstract class BaseFlow {

    protected Map<String, BaseAgent> agents;
    protected List<String> tools;

    public BaseFlow(Map<String, BaseAgent> agents, Map<String, Object> data) {
        setAgents(agents);
        data.put("agents", agents);
    }

    public BaseAgent getAgent(String key) {
        return agents.get(key);
    }

    public void addAgent(String key, BaseAgent agent) {
        agents.put(key, agent);
    }

    public abstract String execute(String inputText);

    public Map<String, BaseAgent> getAgents() {
        return agents;
    }

    public void setAgents(Map<String, BaseAgent> agents) {
        this.agents = agents;
    }
}
