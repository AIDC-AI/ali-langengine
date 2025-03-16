/*
 * Copyright 2024-2024 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.alibaba.langengine.mcp.demo;

import com.alibaba.fastjson.JSON;
import com.alibaba.langengine.mcp.client.McpClient;
import com.alibaba.langengine.mcp.client.McpSyncClient;
import com.alibaba.langengine.mcp.client.transport.ServerParameters;
import com.alibaba.langengine.mcp.client.transport.StdioClientTransport;
import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptRequest;
import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptResult;
import com.alibaba.langengine.mcp.spec.schema.prompts.ListPromptsResult;
import com.alibaba.langengine.mcp.spec.schema.prompts.Prompt;
import com.alibaba.langengine.mcp.spec.schema.resources.ListResourcesResult;
import com.alibaba.langengine.mcp.spec.schema.resources.Resource;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolRequest;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolResult;
import com.alibaba.langengine.mcp.spec.schema.tools.ListToolsResult;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;

import static com.alibaba.langengine.mcp.client.transport.ServerParameters.*;

/**
 * @author Christian Tzolov
 * @since 1.0.0
 */
public class ClientSessionTests2 {

	private static String getDbPath() {
		return Paths.get(System.getProperty("user.dir"), "/alibaba-langengine-infrastructure/alibaba-langengine-mcp/test.db").toString();
	}

	public static void main(String[] args) {

		String dbPath = getDbPath();
		ServerParameters stdioParams = ServerParameters.builder("uvx")
				.args("mcp-server-sqlite", "--db-path",
						dbPath)
				.build();

//		String filePath = getFilePath();
//		ServerParameters stdioParams = ServerParameters.builder("npx")
//				.args("-y", "@modelcontextprotocol/server-filesystem", filePath)
//				.build();

		try (McpSyncClient clientSession = McpClient.using(new StdioClientTransport(stdioParams))
			.requestTimeout(Duration.ofSeconds(30))
			.sync()) {

			clientSession.initialize();

			ListToolsResult tools = clientSession.listTools(null);
			System.out.println("Tools: " + JSON.toJSONString(tools));

			clientSession.ping();

//			 CallToolRequest callToolRequest = new CallToolRequest("echo",
//			 Collections.singletonMap("message", "Hello MCP Spring AI!"));
//			 CallToolResult callToolResult = clientSession.callTool(callToolRequest);
//			 System.out.println("Call Tool Result: " + callToolResult);
//			 clientSession.sendRootsListChanged();

			// Resources
			ListResourcesResult resources = clientSession.listResources(null);
			System.out.println("Resources Size: " + resources.getResources().size());
			System.out.println("Resources: " + JSON.toJSONString(resources));
			for (Resource resource : resources.getResources()) {
				System.out.println(clientSession.readResource(resource));
			}

			ListPromptsResult prompts = clientSession.listPrompts(null);
			System.out.println("Prompts Size: " + prompts.getPrompts().size());
			System.out.println("Prompts: " + JSON.toJSONString(prompts));

			GetPromptRequest getPromptRequest = new GetPromptRequest("mcp-demo", Collections.singletonMap("topic", "hello world"));
			GetPromptResult getPrompt = clientSession.getPrompt(getPromptRequest);
			System.out.println("getPrompt: " + JSON.toJSONString(getPrompt));

			// var resourceTemplate = clientSession.listResourceTemplates(null);
			// System.out.println("Resource Templates: " + resourceTemplate);

			// stdioClient.awaitForExit();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

}
