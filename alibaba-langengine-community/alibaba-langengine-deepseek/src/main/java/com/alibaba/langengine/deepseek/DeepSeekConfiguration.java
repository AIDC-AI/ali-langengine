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
package com.alibaba.langengine.deepseek;

import com.alibaba.langengine.core.util.WorkPropertiesUtils;

/**
 * deepseek model configuration
 *
 * @author xiaoxuan.lp
 */
public class DeepSeekConfiguration {

    /**
     * deepseek server url
     */
    public static String DEEPSEEK_SERVER_URL = WorkPropertiesUtils.get("deepseek_server_url");

    /**
     * deepseek api key
     * deepseek_api_key是配置在系统的配置文件中
     * DEEPSEEK_API_KEY是业内的通用配置，在环境变量中
     */
    public static String DEEPSEEK_API_KEY = WorkPropertiesUtils.getFirstAvailable("deepseek_api_key","DEEPSEEK_API_KEY");

    /**
     * deepseek api timeout
     */
    public static String DEEPSEEK_AI_TIMEOUT = WorkPropertiesUtils.get("deepseek_api_timeout", 300L);
}
