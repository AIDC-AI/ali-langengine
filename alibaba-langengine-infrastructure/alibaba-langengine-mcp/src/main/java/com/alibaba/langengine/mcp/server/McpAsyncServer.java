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

package com.alibaba.langengine.mcp.server;

import com.alibaba.fastjson.TypeReference;
import com.alibaba.langengine.mcp.spec.*;
import com.alibaba.langengine.mcp.spec.schema.*;
import com.alibaba.langengine.mcp.spec.schema.prompts.GetPromptRequest;
import com.alibaba.langengine.mcp.spec.schema.prompts.ListPromptsResult;
import com.alibaba.langengine.mcp.spec.schema.prompts.Prompt;
import com.alibaba.langengine.mcp.spec.schema.resources.*;
import com.alibaba.langengine.mcp.spec.schema.tools.CallToolRequest;
import com.alibaba.langengine.mcp.spec.schema.tools.ListToolsResult;
import com.alibaba.langengine.mcp.spec.schema.tools.Tool;
import com.alibaba.langengine.mcp.util.Utils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * The Model Context Protocol (MCP) server implementation that provides asynchronous
 * communication.
 *
 * @author Christian Tzolov
 */
public class McpAsyncServer {

	private final static Logger logger = LoggerFactory.getLogger(McpAsyncServer.class);

	/**
	 * The MCP session implementation that manages bidirectional JSON-RPC communication
	 * between clients and servers.
	 */
	private final DefaultMcpSession mcpSession;

	private final ServerMcpTransport transport;

	private final ServerCapabilities serverCapabilities;

	private final Implementation serverInfo;

	private ClientCapabilities clientCapabilities;

	private Implementation clientInfo;

	/**
	 * Thread-safe list of tool handlers that can be modified at runtime.
	 */
	private final CopyOnWriteArrayList<ToolRegistration> tools;

	private final CopyOnWriteArrayList<ResourceTemplate> resourceTemplates;

	private final ConcurrentHashMap<String, ResourceRegistration> resources;

	private final ConcurrentHashMap<String, PromptRegistration> prompts;

	private LoggingLevel minLoggingLevel = LoggingLevel.DEBUG;

	/**
	 * Create a new McpAsyncServer with the given transport and capabilities.
	 * @param mcpTransport The transport layer implementation for MCP communication
	 * @param serverInfo The server implementation details
	 * @param serverCapabilities The server capabilities
	 * @param tools The list of tool registrations
	 * @param resources The map of resource registrations
	 * @param resourceTemplates The list of resource templates
	 * @param prompts The map of prompt registrations
	 * @param rootsChangeConsumers The list of consumers that will be notified when the
	 * roots list changes
	 */
	public McpAsyncServer(ServerMcpTransport mcpTransport, Implementation serverInfo,
			ServerCapabilities serverCapabilities, List<ToolRegistration> tools,
			Map<String, ResourceRegistration> resources, List<ResourceTemplate> resourceTemplates,
			Map<String, PromptRegistration> prompts, List<Consumer<List<Root>>> rootsChangeConsumers) {

		this.serverInfo = serverInfo;
		this.tools = new CopyOnWriteArrayList<>(tools != null ? tools : new ArrayList<>());
		this.resources = !Utils.isEmpty(resources) ? new ConcurrentHashMap<>(resources) : new ConcurrentHashMap<>();
		this.resourceTemplates = !Utils.isEmpty(resourceTemplates) ? new CopyOnWriteArrayList<>(resourceTemplates)
				: new CopyOnWriteArrayList<>();
		this.prompts = !Utils.isEmpty(prompts) ? new ConcurrentHashMap<>(prompts) : new ConcurrentHashMap<>();

		this.serverCapabilities = (serverCapabilities != null) ? serverCapabilities : new ServerCapabilities(
				null, // experimental
				new LoggingCapabilities(), // Enable logging
																		// by default
				!Utils.isEmpty(this.prompts) ? new ServerCapabilities.PromptCapabilities(false) : null,
				!Utils.isEmpty(this.resources) ? new ServerCapabilities.ResourceCapabilities(false, false)
						: null,
				!Utils.isEmpty(this.tools) ? new ServerCapabilities.ToolCapabilities(false) : null);

		Map<String, DefaultMcpSession.RequestHandler> requestHandlers = new HashMap<>();

		// Initialize request handlers for standard MCP methods
		requestHandlers.put(MethodDefined.Initialize.getValue(), initializeRequestHandler());

		// Ping MUST respond with an empty data, but not NULL response.
		requestHandlers.put(MethodDefined.Ping.getValue(), (params) -> Mono.<Object>just(""));

		// Add tools API handlers if the tool capability is enabled
		if (this.serverCapabilities.getTools() != null) {
			requestHandlers.put(MethodDefined.ToolsList.getValue(), toolsListRequestHandler());
			requestHandlers.put(MethodDefined.ToolsCall.getValue(), toolsCallRequestHandler());
		}

		// Add resources API handlers if provided
		if (!Utils.isEmpty(this.resources)) {
			requestHandlers.put(MethodDefined.ResourcesList.getValue(), resourcesListRequestHandler());
			requestHandlers.put(MethodDefined.ResourcesRead.getValue(), resourcesReadRequestHandler());
		}

		// Add resource templates API handlers if provided.
		if (!Utils.isEmpty(this.resourceTemplates)) {
			requestHandlers.put(MethodDefined.ResourcesTemplatesList.getValue(), resourceTemplateListRequestHandler());
		}

		// Add prompts API handlers if provider exists
		if (!Utils.isEmpty(this.prompts)) {
			requestHandlers.put(MethodDefined.PromptsList.getValue(), promptsListRequestHandler());
			requestHandlers.put(MethodDefined.PromptsGet.getValue(), promptsGetRequestHandler());
		}

		// Add logging API handlers if the logging capability is enabled
		if (this.serverCapabilities.getLogging() != null) {
			requestHandlers.put(MethodDefined.LoggingSetLevel.getValue(), setLoggerRequestHandler());
		}

		Map<String, DefaultMcpSession.NotificationHandler> notificationHandlers = new HashMap<>();

		notificationHandlers.put(MethodDefined.NotificationsInitialized.getValue(), (params) -> Mono.empty());

		if (Utils.isEmpty(rootsChangeConsumers)) {
			rootsChangeConsumers = Arrays.asList((roots) -> logger
					.warn("Roots list changed notification, but no consumers provided. Roots list changed: {}", roots));
		}
		notificationHandlers.put(MethodDefined.NotificationsRootsListChanged.getValue(),
				rootsListChnagedNotificationHandler(rootsChangeConsumers));

		this.transport = mcpTransport;
		this.mcpSession = new DefaultMcpSession(Duration.ofSeconds(10), mcpTransport, requestHandlers,
				notificationHandlers);
	}

	// ---------------------------------------
	// Lifecycle Management
	// ---------------------------------------
	private DefaultMcpSession.RequestHandler initializeRequestHandler() {
		return params -> {
			InitializeRequest initializeRequest = transport.unmarshalFrom(params,
					new TypeReference<InitializeRequest>() {
					});

			this.clientCapabilities = initializeRequest.getCapabilities();
			this.clientInfo = initializeRequest.getClientInfo();

			logger.info("Client initialize request - Protocol: {}, Capabilities: {}, Info: {}",
					initializeRequest.getProtocolVersion(), initializeRequest.getCapabilities(),
					initializeRequest.getClientInfo());

			if (!Schema.LATEST_PROTOCOL_VERSION.equals(initializeRequest.getProtocolVersion())) {
				return Mono
					.<Object>error(new McpError(
							"Unsupported protocol version from client: " + initializeRequest.getProtocolVersion()))
					.publishOn(Schedulers.boundedElastic());
			}

			return Mono
				.<Object>just(new InitializeResult(Schema.LATEST_PROTOCOL_VERSION, this.serverCapabilities,
						this.serverInfo, null))
				.publishOn(Schedulers.boundedElastic());
		};
	}

	/**
	 * Get the server capabilities that define the supported features and functionality.
	 * @return The server capabilities
	 */
	public ServerCapabilities getServerCapabilities() {
		return this.serverCapabilities;
	}

	/**
	 * Get the server implementation information.
	 * @return The server implementation details
	 */
	public Implementation getServerInfo() {
		return this.serverInfo;
	}

	/**
	 * Get the client capabilities that define the supported features and functionality.
	 * @return The client capabilities
	 */
	public ClientCapabilities getClientCapabilities() {
		return this.clientCapabilities;
	}

	/**
	 * Get the client implementation information.
	 * @return The client implementation details
	 */
	public Implementation getClientInfo() {
		return this.clientInfo;
	}

	/**
	 * Gracefully closes the server, allowing any in-progress operations to complete.
	 * @return A Mono that completes when the server has been closed
	 */
	public Mono<Void> closeGracefully() {
		return this.mcpSession.closeGracefully();
	}

	/**
	 * Close the server immediately.
	 */
	public void close() {
		this.mcpSession.close();
	}

	private static TypeReference<ListRootsResult> LIST_ROOTS_RESULT_TYPE_REF = new TypeReference<ListRootsResult>() {
	};

	/**
	 * Retrieves the list of all roots provided by the client.
	 * @return A Mono that emits the list of roots result.
	 */
	public Mono<ListRootsResult> listRoots() {
		return this.listRoots(null);
	}

	/**
	 * Retrieves a paginated list of roots provided by the server.
	 * @param cursor Optional pagination cursor from a previous list request
	 * @return A Mono that emits the list of roots result containing
	 */
	public Mono<ListRootsResult> listRoots(String cursor) {
		return this.mcpSession.sendRequest(MethodDefined.RootsList.getValue(), new PaginatedRequest(cursor),
				LIST_ROOTS_RESULT_TYPE_REF);
	}

	private DefaultMcpSession.NotificationHandler rootsListChnagedNotificationHandler(
			List<Consumer<List<Root>>> rootsChangeConsumers) {

		return params -> {
			return listRoots().flatMap(listRootsResult -> Mono.fromRunnable(() -> {
				rootsChangeConsumers.stream().forEach(consumer -> consumer.accept(listRootsResult.getRoots()));
			}).subscribeOn(Schedulers.boundedElastic())).onErrorResume(error -> {
				logger.error("Error handling roots list change notification", error);
				return Mono.empty();
			}).then();
		};
	};

	// ---------------------------------------
	// Tool Management
	// ---------------------------------------

	/**
	 * Add a new tool registration at runtime.
	 * @param toolRegistration The tool registration to add
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> addTool(ToolRegistration toolRegistration) {
		if (toolRegistration == null) {
			return Mono.error(new McpError("Tool registration must not be null"));
		}
		if (toolRegistration.getTool() == null) {
			return Mono.error(new McpError("Tool must not be null"));
		}
		if (toolRegistration.getCall() == null) {
			return Mono.error(new McpError("Tool call handler must not be null"));
		}
		if (this.serverCapabilities.getTools() == null) {
			return Mono.error(new McpError("Server must be configured with tool capabilities"));
		}

		// Check for duplicate tool names
		if (this.tools.stream().anyMatch(th -> th.getTool().getName().equals(toolRegistration.getTool().getName()))) {
			return Mono.error(new McpError("Tool with name '" + toolRegistration.getTool().getName() + "' already exists"));
		}

		this.tools.add(toolRegistration);
		logger.info("Added tool handler: {}", toolRegistration.getTool().getName());
		if (this.serverCapabilities.getTools().getListChanged()) {
			return notifyToolsListChanged();
		}
		return Mono.empty();
	}

	/**
	 * Remove a tool handler at runtime.
	 * @param toolName The name of the tool handler to remove
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> removeTool(String toolName) {
		if (toolName == null) {
			return Mono.error(new McpError("Tool name must not be null"));
		}
		if (this.serverCapabilities.getTools() == null) {
			return Mono.error(new McpError("Server must be configured with tool capabilities"));
		}

		boolean removed = this.tools.removeIf(toolRegistration -> toolRegistration.getTool().getName().equals(toolName));
		if (removed) {
			logger.info("Removed tool handler: {}", toolName);
			if (this.serverCapabilities.getTools().getListChanged()) {
				return notifyToolsListChanged();
			}
			return Mono.empty();
		}
		return Mono.error(new McpError("Tool with name '" + toolName + "' not found"));
	}

	/**
	 * Notifies clients that the list of available tools has changed.
	 * @return A Mono that completes when all clients have been notified
	 */
	public Mono<Void> notifyToolsListChanged() {
		return this.mcpSession.sendNotification(MethodDefined.NotificationsToolsListChanged.getValue(), null);
	}

	private DefaultMcpSession.RequestHandler toolsListRequestHandler() {
		return params -> {

			List<Tool> tools = this.tools.stream().map(toolRegistration -> {
				return toolRegistration.getTool();
			}).collect(Collectors.toList());

			return Mono.just(new ListToolsResult(tools, null));
		};
	}

	private DefaultMcpSession.RequestHandler toolsCallRequestHandler() {
		return params -> {
			CallToolRequest callToolRequest = transport.unmarshalFrom(params,
					new TypeReference<CallToolRequest>() {
					});

			Optional<ToolRegistration> toolRegistration = this.tools.stream()
				.filter(tr -> callToolRequest.getName().equals(tr.getTool().getName()))
				.findAny();

			if (!toolRegistration.isPresent()) {
				return Mono.<Object>error(new McpError("Tool not found: " + callToolRequest.getName()));
			}

			return Mono.fromCallable(() -> toolRegistration.get().getCall().apply(callToolRequest.getArguments()))
				.map(result -> (Object) result)
				.subscribeOn(Schedulers.boundedElastic());
		};
	}

	// ---------------------------------------
	// Resource Management
	// ---------------------------------------

	/**
	 * Add a new resource handler at runtime.
	 * @param resourceHandler The resource handler to add
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> addResource(ResourceRegistration resourceHandler) {
		if (resourceHandler == null || resourceHandler.getResource() == null) {
			return Mono.error(new McpError("Resource must not be null"));
		}

		if (this.serverCapabilities.getResources() == null) {
			return Mono.error(new McpError("Server must be configured with resource capabilities"));
		}

		if (this.resources.containsKey(resourceHandler.getResource().getUri())) {
			return Mono
				.error(new McpError("Resource with URI '" + resourceHandler.getResource().getUri() + "' already exists"));
		}

		this.resources.put(resourceHandler.getResource().getUri(), resourceHandler);
		logger.info("Added resource handler: {}", resourceHandler.getResource().getUri());
		if (this.serverCapabilities.getResources().getListChanged()) {
			return notifyResourcesListChanged();
		}
		return Mono.empty();
	}

	/**
	 * Remove a resource handler at runtime.
	 * @param resourceUri The URI of the resource handler to remove
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> removeResource(String resourceUri) {
		if (resourceUri == null) {
			return Mono.error(new McpError("Resource URI must not be null"));
		}
		if (this.serverCapabilities.getResources() == null) {
			return Mono.error(new McpError("Server must be configured with resource capabilities"));
		}

		ResourceRegistration removed = this.resources.remove(resourceUri);
		if (removed != null) {
			logger.info("Removed resource handler: {}", resourceUri);
			if (this.serverCapabilities.getResources().getListChanged()) {
				return notifyResourcesListChanged();
			}
			return Mono.empty();
		}
		return Mono.error(new McpError("Resource with URI '" + resourceUri + "' not found"));
	}

	/**
	 * Notifies clients that the list of available resources has changed.
	 * @return A Mono that completes when all clients have been notified
	 */
	public Mono<Void> notifyResourcesListChanged() {
		return this.mcpSession.sendNotification(MethodDefined.NotificationsResourcesListChanged.getValue(), null);
	}

	private DefaultMcpSession.RequestHandler resourcesListRequestHandler() {
		return params -> {
			List<Resource> resourceList = this.resources.values().stream().map(ResourceRegistration::getResource).collect(Collectors.toList());
			return Mono.just(new ListResourcesResult(resourceList, null));
		};
	}

	private DefaultMcpSession.RequestHandler resourceTemplateListRequestHandler() {
		return params -> Mono.just(new ListResourceTemplatesResult(this.resourceTemplates, null));

	}

	private DefaultMcpSession.RequestHandler resourcesReadRequestHandler() {
		return params -> {
			ReadResourceRequest resourceRequest = transport.unmarshalFrom(params,
					new TypeReference<ReadResourceRequest>() {
					});
			String resourceUri = resourceRequest.getUri();
			if (this.resources.containsKey(resourceUri)) {
				return Mono.fromCallable(() -> this.resources.get(resourceUri).getReadHandler().apply(resourceRequest))
					.map(result -> (Object) result)
					.subscribeOn(Schedulers.boundedElastic());
			}
			return Mono.error(new McpError("Resource not found: " + resourceUri));
		};
	}

	// ---------------------------------------
	// Prompt Management
	// ---------------------------------------

	/**
	 * Add a new prompt handler at runtime.
	 * @param promptRegistration The prompt handler to add
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> addPrompt(PromptRegistration promptRegistration) {
		if (promptRegistration == null) {
			return Mono.error(new McpError("Prompt registration must not be null"));
		}
		if (this.serverCapabilities.getPrompts() == null) {
			return Mono.error(new McpError("Server must be configured with prompt capabilities"));
		}

		if (this.prompts.containsKey(promptRegistration.getPropmpt().getName())) {
			return Mono
				.error(new McpError("Prompt with name '" + promptRegistration.getPropmpt().getName() + "' already exists"));
		}

		this.prompts.put(promptRegistration.getPropmpt().getName(), promptRegistration);

		logger.info("Added prompt handler: {}", promptRegistration.getPropmpt().getName());

		// Servers that declared the listChanged capability SHOULD send a notification,
		// when the list of available prompts changes
		if (this.serverCapabilities.getPrompts().getListChanged()) {
			return notifyPromptsListChanged();
		}
		return Mono.empty();
	}

	/**
	 * Remove a prompt handler at runtime.
	 * @param promptName The name of the prompt handler to remove
	 * @return Mono that completes when clients have been notified of the change
	 */
	public Mono<Void> removePrompt(String promptName) {
		if (promptName == null) {
			return Mono.error(new McpError("Prompt name must not be null"));
		}
		if (this.serverCapabilities.getPrompts() == null) {
			return Mono.error(new McpError("Server must be configured with prompt capabilities"));
		}

		PromptRegistration removed = this.prompts.remove(promptName);

		if (removed != null) {
			logger.info("Removed prompt handler: {}", promptName);
			// Servers that declared the listChanged capability SHOULD send a
			// notification, when the list of available prompts changes
			if (this.serverCapabilities.getPrompts().getListChanged()) {
				return this.notifyPromptsListChanged();
			}
			return Mono.empty();
		}
		return Mono.error(new McpError("Prompt with name '" + promptName + "' not found"));
	}

	/**
	 * Notifies clients that the list of available prompts has changed.
	 * @return A Mono that completes when all clients have been notified
	 */
	public Mono<Void> notifyPromptsListChanged() {
		return this.mcpSession.sendNotification(MethodDefined.NotificationsPromptsListChanged.getValue(), null);
	}

	private DefaultMcpSession.RequestHandler promptsListRequestHandler() {
		return params -> {
			// TODO: Implement pagination
			// McpSchema.PaginatedRequest request = transport.unmarshalFrom(params,
			// new TypeReference<McpSchema.PaginatedRequest>() {
			// });

			List<Prompt> promptList = this.prompts.values().stream().map(PromptRegistration::getPropmpt).collect(Collectors.toList());

			return Mono.just(new ListPromptsResult(promptList, null));
		};
	}

	private DefaultMcpSession.RequestHandler promptsGetRequestHandler() {
		return params -> {
			GetPromptRequest promptRequest = transport.unmarshalFrom(params,
					new TypeReference<GetPromptRequest>() {
					});

			// Implement prompt retrieval logic here
			if (this.prompts.containsKey(promptRequest.getName())) {
				return Mono
					.fromCallable(() -> this.prompts.get(promptRequest.getName()).getPromptHandler().apply(promptRequest))
					.map(result -> (Object) result)
					.subscribeOn(Schedulers.boundedElastic());
			}

			return Mono.error(new McpError("Prompt not found: " + promptRequest.getName()));
		};
	}

	// ---------------------------------------
	// Logging Management
	// ---------------------------------------

	/**
	 * Send a logging message notification to all connected clients. Messages below the
	 * current minimum logging level will be filtered out.
	 * @param loggingMessageNotification The logging message to send
	 * @return A Mono that completes when the notification has been sent
	 */
	public Mono<Void> loggingNotification(LoggingMessageNotification loggingMessageNotification) {

		if (loggingMessageNotification == null) {
			return Mono.error(new McpError("Logging message must not be null"));
		}

		Map<String, Object> params = this.transport.unmarshalFrom(loggingMessageNotification,
				new TypeReference<Map<String, Object>>() {
				});

		if (loggingMessageNotification.getLevel().level() < minLoggingLevel.level()) {
			return Mono.empty();
		}

		return this.mcpSession.sendNotification(MethodDefined.NotificationsMessage.getValue(), params);
	}

	/**
	 * Handles requests to set the minimum logging level. Messages below this level will
	 * not be sent.
	 * @return A handler that processes logging level change requests
	 */
	private DefaultMcpSession.RequestHandler setLoggerRequestHandler() {
		return params -> {
			LoggingLevel setLoggerRequest = transport.unmarshalFrom(params,
					new TypeReference<LoggingLevel>() {
					});

			this.minLoggingLevel = setLoggerRequest;

			return Mono.empty();
		};
	}

	// ---------------------------------------
	// Sampling
	// ---------------------------------------
	private static TypeReference<CreateMessageResult> CREATE_MESSAGE_RESULT_TYPE_REF = new TypeReference<CreateMessageResult>() {
	};

	/**
	 * Create a new message using the sampling capabilities of the client. The Model
	 * Context Protocol (MCP) provides a standardized way for servers to request LLM
	 * sampling (“completions” or “generations”) from language models via clients. This
	 * flow allows clients to maintain control over model access, selection, and
	 * permissions while enabling servers to leverage AI capabilities—with no server API
	 * keys necessary. Servers can request text or image-based interactions and optionally
	 * include context from MCP servers in their prompts.
	 * @param createMessageRequest The request to create a new message
	 * @return A Mono that completes when the message has been created
	 * @throws McpError if the client has not been initialized or does not support
	 * sampling capabilities
	 * @throws McpError if the client does not support the createMessage method
	 * @see <a href=
	 * "https://spec.modelcontextprotocol.io/specification/client/sampling/">Sampling
	 * Specification</a>
	 */
	public Mono<CreateMessageResult> createMessage(CreateMessageRequest createMessageRequest) {

		if (this.clientCapabilities == null) {
			return Mono.error(new McpError("Client must be initialized. Call the initialize method first!"));
		}
		if (this.clientCapabilities.getSampling() == null) {
			return Mono.error(new McpError("Client must be configured with sampling capabilities"));
		}
		return this.mcpSession.sendRequest(MethodDefined.SamplingCreateMessage.getValue(), createMessageRequest,
				CREATE_MESSAGE_RESULT_TYPE_REF);
	}

}
