/*
 * Copyright 2025 Alibaba Group Holding Ltd.
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
package com.alibaba.langengine.modelcontextprotocol.spec;

import lombok.Data;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.concurrent.CompletableFuture;

/**
 * Defines the asynchronous transport layer for the Model Context Protocol (MCP).
 *
 * <p>
 * The McpTransport interface provides the foundation for implementing custom transport
 * mechanisms in the Model Context Protocol. It handles the bidirectional communication
 * between the client and server components, supporting asynchronous message exchange
 * using JSON-RPC format.
 * </p>
 *
 * <p>
 * Implementations of this interface are responsible for:
 * </p>
 * <ul>
 * <li>Managing the lifecycle of the transport connection</li>
 * <li>Handling incoming messages and errors from the server</li>
 * <li>Sending outbound messages to the server</li>
 * </ul>
 *
 * <p>
 * The transport layer is designed to be protocol-agnostic, allowing for various
 * implementations such as WebSocket, HTTP, or custom protocols.
 * </p>
 *
 * JDK 1.8 compatible version.
 *
 * @author Christian Tzolov
 * @author Dariusz Jędrzejczyk
 */
public interface McpTransport {

    /**
     * Closes the transport connection and releases any associated resources.
     *
     * <p>
     * This method ensures proper cleanup of resources when the transport is no longer
     * needed. It should handle the graceful shutdown of any active connections.
     * </p>
     */
    default void close() {
        this.closeGracefully();
    }

    /**
     * Closes the transport connection and releases any associated resources
     * asynchronously.
     * @return a {@link CompletableFuture<Void>} that completes when the connection has been closed.
     */
    CompletableFuture<Void> closeGracefully();

    /**
     * Sends a message to the peer asynchronously.
     *
     * <p>
     * This method handles the transmission of messages to the server in an asynchronous
     * manner. Messages are sent in JSON-RPC format as specified by the MCP protocol.
     * </p>
     * @param message the {@link JSONRPCMessage} to be sent to the server
     * @return a {@link CompletableFuture<Void>} that completes when the message has been sent
     */
    CompletableFuture<Void> sendMessage(JSONRPCMessage message);

    /**
     * Unmarshals the given data into an object of the specified type.
     * @param <T> the type of the object to unmarshal
     * @param data the data to unmarshal
     * @param typeRef the type reference for the object to unmarshal
     * @return the unmarshalled object
     */
    <T> T unmarshalFrom(Object data, TypeReference<T> typeRef);
}
