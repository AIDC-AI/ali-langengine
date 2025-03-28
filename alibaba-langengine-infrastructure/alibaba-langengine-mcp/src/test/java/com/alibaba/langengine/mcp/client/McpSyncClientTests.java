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

package com.alibaba.langengine.mcp.client;

import com.alibaba.langengine.mcp.client.transport.ServerParameters;
import com.alibaba.langengine.mcp.client.transport.StdioClientTransport;
import com.alibaba.langengine.mcp.spec.ClientMcpTransport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/**
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
@Timeout(15) // Giving extra time beyond the client timeout
class McpSyncClientTests extends AbstractMcpSyncClientTests {

	@Override
	protected ClientMcpTransport createMcpTransport() {
		ServerParameters stdioParams = ServerParameters.builder("npx")
			.args("-y", "@modelcontextprotocol/server-everything", "dir")
			.build();

		return new StdioClientTransport(stdioParams);
	}

	@Test
	void customErrorHandlerShouldReceiveErrors() {
		AtomicReference<String> receivedError = new AtomicReference<>();

		((StdioClientTransport) mcpTransport).setErrorHandler(error -> receivedError.set(error));

		String errorMessage = "Test error";
		((StdioClientTransport) mcpTransport).getErrorSink().tryEmitNext(errorMessage);

		assertThat(receivedError.get()).isNotNull().isEqualTo(errorMessage);
	}

	@Override
	protected void onStart() {
	}

	@Override
	protected void onClose() {
	}

}
