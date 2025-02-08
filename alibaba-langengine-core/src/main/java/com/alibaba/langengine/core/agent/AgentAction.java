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
package com.alibaba.langengine.core.agent;

import com.alibaba.fastjson.annotation.JSONField;
import com.alibaba.langengine.core.tool.BaseTool;
import lombok.Data;

import java.util.List;

/**
 * 代理要采取的行动
 *
 * @author xiaoxuan.lp
 */
@Data
public class AgentAction implements AgentNextStep {

    /**
     * 工具key
     */
    private String tool;

    /**
     * 工具输入
     */
    private String toolInput;

    /**
     * 工具结果
     */
    private String observation;

    /**
     * 思考记录
     */
    private String log;

    /**
     * 下一轮执行的工具集，如果为空表示不需要动态变更下一轮执行工具
     */
    @JSONField(serialize = false)
    private List<BaseTool> nextTools;

    private String prevId;
}
