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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static org.apache.commons.lang3.StringUtils.EMPTY;

/**
 * python辅助工具
 *
 * @author xiaoxuan.lp
 */
@Slf4j
public class PythonUtils {

    private static final String OS = System.getProperty("os.name");


    /**
     * 调用python方法
     *
     * @param pythonFilePath
     * @param args
     * @return
     */
    public static String invokeMethod(String pythonFilePath, String... args) {
        return executePythonCode(getCommand(pythonFilePath, args));
    }

    /**
     * 通过resource文件调用python方法
     *
     * @param classType
     * @param pythonFileName
     * @param args
     * @return
     */
    public static String invokeMethodAsResource(Class<?> classType, String pythonFileName, String... args) {
        String pythonFilePath = classType.getClassLoader().getResource(pythonFileName).getPath();
        return executePythonCode(getCommand(pythonFilePath, args));
    }

    public static String invokePythonCode(String pythonCodeTemplate, String... args) {
        String pythonCode = String.format(pythonCodeTemplate, args);
        return executePythonCode(getCommandWithCode(pythonCode));
    }

    public static String invokePythonCodeWithArch(String pythonCodeTemplate, boolean arm64, String... args) {
        String pythonCode = String.format(pythonCodeTemplate, args);
        return executePythonCode(getCommandWithCode(pythonCode, arm64));
    }

    public static String executePythonCode(String... command) {
        try {
            Process process = new ProcessBuilder(command).start();

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
            String errorResult = read(errorReader);
            log.info("read python error={}", errorResult);

            BufferedReader inputReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String successResult = read(inputReader);
            log.info("read python success={}", successResult);

            return successResult;
        } catch (Exception e) {
            log.error("executePythonCode error", e);
            return null;
        }
    }

    private static String[] getCommand(String pythonFilePath, String... args) {
        List<String> commands = new ArrayList<>();
        if (OS.startsWith("Windows")) {
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add("python");
        } else {
            commands.add("python3");
        }
        commands.add(pythonFilePath);
        if (args != null) {
            commands.addAll(Arrays.asList(args).stream()
                    .filter(o -> o != null).collect(Collectors.toList()));
        }
        return commands.toArray(new String[]{});
    }

    private static String[] getCommandWithCode(String pythonCode) {
        return getCommandWithCode(pythonCode, null);
    }

    private static String[] getCommandWithCode(String pythonCode, Boolean arm64) {
        List<String> commands = new ArrayList<>();
        if (OS.startsWith("Windows")) {
            commands.add("cmd.exe");
            commands.add("/c");
            commands.add("python");
            commands.add("-c");
        } else {
            if(arm64 != null) {
                commands.add("arch");
                commands.add(arm64 ? "-arm64" : "-x86_64");
            }
            commands.add("python3");
            commands.add("-c");
        }
        commands.add(pythonCode);
        return commands.toArray(new String[]{});
    }

    private static String read(BufferedReader reader) {
        List<String> resultList = new ArrayList<>();
        String response = EMPTY;
        while (true) {
            try {
                if (!((response = reader.readLine()) != null))
                    break;
            } catch (IOException e) {
            }
            resultList.add(response);
        }
        return  resultList.stream().collect(Collectors.joining("\n"));
    }
}
