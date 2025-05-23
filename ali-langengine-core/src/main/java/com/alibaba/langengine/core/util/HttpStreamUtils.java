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
package com.alibaba.langengine.core.util;

import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

/**
 * @author aihe.ah
 * @time 2023/12/7
 * 功能说明：
 */
@Slf4j
public class HttpStreamUtils {

    public static String convertStreamToString(InputStream is) {
        try {
            StringBuilder sb = new StringBuilder();
            String line;

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append("\n");
                }
            }

            return sb.toString();
        } catch (Exception e) {
            // 可以根据需要添加更具体的异常处理逻辑
            log.error("Error processing audio generation", e);
            return "";
        }

    }
}
