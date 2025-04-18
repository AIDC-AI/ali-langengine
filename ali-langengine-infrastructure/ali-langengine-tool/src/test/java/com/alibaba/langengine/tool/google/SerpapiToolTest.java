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
package com.alibaba.langengine.tool.google;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.core.tool.ToolExecuteResult;
import org.junit.jupiter.api.Test;

public class SerpapiToolTest {

    @Test
    public void test_run() {
        // success
        SerpapiTool tool = new SerpapiTool();
        tool.setNum(2);
//        ToolExecuteResult result = tool.run("Thai food dinner recipes");
        ToolExecuteResult result = tool.run("淘宝网是什么时候建立的？");
        System.out.println(JSON.toJSONString(result));
    }
}
