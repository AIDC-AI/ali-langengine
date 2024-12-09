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
package com.alibaba.langengine.agentframework.config;

import com.alibaba.langengine.agentframework.manager.AgentProcessDefineManager;
import com.alibaba.langengine.agentframework.model.dataobject.AgentProcessDefineDO;
import org.springframework.stereotype.Component;

@Component
public class MockAgentProcessDefineManager implements AgentProcessDefineManager {
    @Override
    public Long addDefineNewVersion(AgentProcessDefineDO agentProcessDefineDO) {
        return 0L;
    }

    @Override
    public AgentProcessDefineDO getLatestVersionByProcessDefinitionId(String processDefinitionId) {
        return null;
    }

    @Override
    public AgentProcessDefineDO getByProcessDefinitionIdAndVersion(String processDefinitionId, Integer version) {
        return null;
    }
}