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
package com.alibaba.langengine.core.runnables.tools;

import com.alibaba.langengine.core.tool.DefaultTool;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.Map;

@Slf4j
public class TmpCommonTool extends DefaultTool {

    public TmpCommonTool(String name, String description, Map<String, Object> args) {
        setName(name);
        setDescription(description);
        setArgs(args);
    }

    public TmpCommonTool(String name, String description) {
        setName(name);
        setDescription(description);
    }
    @Override
    public ToolExecuteResult run(String toolInput) {


        if(StringUtils.equals(getName(),"realTimeCongestIdxTool")){
            log.warn("realTimeCongestIdxTool toolInput:" + toolInput);
            return new ToolExecuteResult("{\"time\":\"20230601\",\"路况状态\":\"路况拥堵严重\",\"速度\":\"1.36公⾥/⼩时\",\"名称\":\"北京市\",\"拥堵延时指数\":\"10.45\"}");
        }

        if(StringUtils.equals(getName(),"reasonTrafficTool")){
            log.warn("reasonTrafficTool toolInput:" + toolInput);
            return new ToolExecuteResult("因为有大型活动，造成了交通拥堵");
        }

        if(StringUtils.equals(getName(),"suggestTrafficTool")){
            log.warn("reasonTrafficTool toolInput:" + toolInput);
            return new ToolExecuteResult("建议您错峰出行", true);
        }
        return new ToolExecuteResult("未知异常", true);
    }
}
