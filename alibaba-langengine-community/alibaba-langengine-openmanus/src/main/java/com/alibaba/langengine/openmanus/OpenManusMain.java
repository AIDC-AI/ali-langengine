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
package com.alibaba.langengine.openmanus;

import com.alibaba.langengine.openmanus.agent.BaseAgent;
import com.alibaba.langengine.openmanus.agent.ManusAgent;
import com.alibaba.langengine.openmanus.flow.PlanningFlow;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

@Slf4j
public class OpenManusMain {

    public static void main(String[] args) {
        URL resource = OpenManusMain.class.getClassLoader().getResource("data/chromedriver");
        if (resource == null) {
            throw new IllegalStateException("Chromedriver not found in resources");
        }
        String chromedriverPath = Paths.get(resource.getPath()).toFile().getAbsolutePath();
        System.setProperty("webdriver.chrome.driver", chromedriverPath);

        ManusAgent manusAgent = new ManusAgent();

        Map<String, BaseAgent> agentMap = new HashMap<String, BaseAgent>() {{
            put("manus", manusAgent);
        }};
        Map<String, Object> data = new HashMap<>();
        PlanningFlow planningFlow = new PlanningFlow(agentMap, data);

        Scanner scanner = new Scanner(System.in);

        try {
            System.out.print("Enter your prompt: ");
            String prompt = scanner.nextLine().trim();

            if (prompt.isEmpty()) {
                System.out.println("Empty prompt provided.");
                return;
            }

            System.out.println("Processing your request...");

            try {
                long startTime = System.currentTimeMillis();
                String result = planningFlow.execute(prompt);

                long elapsedTime = System.currentTimeMillis() - startTime;
                System.out.println("Request processed in " + elapsedTime / 1000.0 + " seconds");
                log.info(result);
            } catch (Exception e) {
                log.error("Error: " + e.getMessage());
            }

        } catch (Throwable e) {
            log.error("Unexpected Error: " + e.getMessage());
        }

        System.out.println("Please press any key to end:");
        String input = scanner.nextLine();
        System.out.println("You finished");
        scanner.close();
    }
}
