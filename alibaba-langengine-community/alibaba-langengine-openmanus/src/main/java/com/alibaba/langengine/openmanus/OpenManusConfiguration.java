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

import com.alibaba.langengine.core.chatmodel.BaseChatModel;
import com.alibaba.langengine.core.util.WorkPropertiesUtils;
import com.alibaba.langengine.dashscope.DashScopeModelName;
import com.alibaba.langengine.dashscope.model.DashScopeOpenAIChatModel;
import com.alibaba.langengine.openai.model.ChatModelOpenAI;
import com.alibaba.langengine.openai.model.OpenAIModelConstants;
import com.alibaba.langengine.openmanus.appbuilder.ApiGatewayChatModel;
import com.alibaba.langengine.openmanus.appbuilder.service.ApiGatewayModelType;

public class OpenManusConfiguration {

    public static String APPBUILDER_APIID = WorkPropertiesUtils.getFirstAvailable("aidc_appbuilder_appid");
    public static String APPBUILDER_AK = WorkPropertiesUtils.getFirstAvailable("aidc_appbuilder_ak");

    public static BaseChatModel manusChatModel;
    public static BaseChatModel planningChatModel;

    static {
        manusChatModel = new DashScopeOpenAIChatModel();
        manusChatModel.setModel(DashScopeModelName.QWEN25_MAX);

//        manusChatModel = new ChatModelOpenAI();
//        manusChatModel.setModel(OpenAIModelConstants.GPT_4_TURBO);

//        manusChatModel = new ApiGatewayChatModel(
//                APPBUILDER_APIID,
//                null,
//                ApiGatewayModelType.OPENAI,
//                "production",
//                APPBUILDER_AK);

        manusChatModel.setTemperature(0d);
        manusChatModel.setToolChoice("required");

        planningChatModel = new DashScopeOpenAIChatModel();
        planningChatModel.setModel(DashScopeModelName.QWEN25_MAX);

//        planningChatModel = new ChatModelOpenAI();
//        planningChatModel.setModel(OpenAIModelConstants.GPT_4_TURBO);
//
//        planningChatModel = new ApiGatewayChatModel(
//                APPBUILDER_APIID,
//                null,
//                ApiGatewayModelType.OPENAI,
//                "production",
//                APPBUILDER_AK);

        planningChatModel.setTemperature(0d);
        planningChatModel.setToolChoice("required");
    }

    public static BaseChatModel getManusChatModel() {
        return manusChatModel;
    }

    public static BaseChatModel getPlanningChatModel() {
        return planningChatModel;
    }
}
