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
package com.alibaba.langengine.dashscope;

/**
 * The LLMs provided by Alibaba Cloud, performs better than most LLMs in Asia languages.
 */
public class DashScopeModelName {

    public static final String QWEN25_MAX = "qwen-max-2025-01-25";

    /**
     * 通义千问系列效果最好的模型，适合复杂、多步骤的任务。
     * 当前等同qwen-max-2024-09-19
     */
    public static final String QWEN_MAX = "qwen-max";

    /**
     * 始终等同最新快照版
     */
    public static final String QWEN_MAX_LATEST = "qwen-max-latest";

    /**
     * 能力均衡，推理效果、成本和速度介于通义千问-Max和通义千问-Turbo之间，适合中等复杂任务。
     * 当前等同qwen-plus-2024-09-19
     */
    public static final String QWEN_PLUS = "qwen-plus";

    /**
     * 始终等同最新快照版
     */
    public static final String QWEN_PLUS_LATEST = "qwen-plus-latest";

    /**
     * 通义千问系列速度最快、成本很低的模型，适合简单任务。
     * 当前等同qwen-turbo-2024-09-19
     */
    public static final String QWEN_TURBO = "qwen-turbo";

    /**
     * 始终等同最新快照版
     */
    public static final String QWEN_TURBO_LATEST = "qwen-turbo-latest";

    /**
     * 支持总结和分析长达千万字的文档，且成本极低。
     */
    public static final String QWEN_LONG = "qwen-long";

    /**
     * 通义千问VL是具有视觉（图像）理解能力的文本生成模型，不仅能进行OCR（图片文字识别），还能进一步总结和推理，
     * 例如从商品照片中提取属性，根据习题图进行解题等。
     * 当前等同qwen-vl-max-2024-08-09
     */
    public static final String QWEN_VL_MAX = "qwen-vl-max";

    /**
     * 始终等同最新快照版
     */
    public static final String QWEN_VL_MAX_LATEST = "qwen-vl-max-latest";

    /**
     * 通义千问Audio是音频理解模型，支持输入多种音频（人类语音、自然音、音乐、歌声）和文本，并输出文本。
     * 该模型不仅能对输入的音频进行转录，还具备更深层次的语义理解、情感分析、音频事件检测、语音聊天等能力。
     */
    public static final String QWEN_VL_PLUS = "qwen-vl-plus";

    /**
     * 始终等同最新快照版
     */
    public static final String QWEN_VL_PLUS_LATEST = "qwen-vl-plus-latest";

    /**
     * 通义千问Audio是音频理解模型，支持输入多种音频（人类语音、自然音、音乐、歌声）和文本，并输出文本。
     */
    public static final String QWEN_AUDIO_TURBO = "qwen-audio-turbo";

    /**
     * 通义千问数学模型是专门用于数学解题的语言模型。
     * 当前等同qwen-math-plus-2024-09-19
     */
    public static final String QWEN_MATH_PLUS = "qwen-math-plus";

    /**
     * 始终等同最新快照版
     */
    public static final String QWEN_MATH_PLUS_LATEST = "qwen-math-plus-latest";

    /**
     * 通义千问代码模型。
     * 当前等同qwen-coder-turbo-2024-09-19
     */
    public static final String QWEN_CODER_TURBO = "qwen-coder-turbo";

    /**
     * 等同qwen-coder-turbo最新的快照版本
     */
    public static final String QWEN_CODER_TURBO_LATEST = "qwen-coder-turbo-latest";

    public static final String QWEN25_72B_INSTRUCT = "qwen2.5-72b-instruct";
    public static final String QWEN25_32B_INSTRUCT = "qwen2.5-32b-instruct";
    public static final String QWEN25_14B_INSTRUCT = "qwen2.5-14b-instruct";
    public static final String QWEN25_7B_INSTRUCT = "qwen2.5-7b-instruct";
    public static final String QWEN25_3B_INSTRUCT = "qwen2.5-3b-instruct";
    public static final String QWEN25_1_5B_INSTRUCT = "qwen2.5-1.5b-instruct";
    public static final String QWEN25_0_5B_INSTRUCT = "qwen2.5-0.5b-instruct";



    public static final String QWEN_7B_CHAT = "qwen-7b-chat";  // Qwen open sourced 7-billion-parameters version
    public static final String QWEN_14B_CHAT = "qwen-14b-chat";  // Qwen open sourced 14-billion-parameters version

    // Use with QwenEmbeddingModel
    public static final String TEXT_EMBEDDING_V1 = "text-embedding-v1";

    public static final String DEEPSEEK_R1 = "deepseek-r1";
    public static final String DEEPSEEK_V3 = "deepseek-v3";
}