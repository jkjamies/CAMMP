/*
 * Copyright 2025-2026 Jason Jamieson
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

package com.jkjamies.cammp.mcp

import com.jkjamies.cammp.mcp.di.McpGraph
import com.jkjamies.cammp.mcp.resources.DATASOURCE_STRATEGIES_TEXT
import com.jkjamies.cammp.mcp.resources.DI_STRATEGIES_TEXT
import com.jkjamies.cammp.mcp.tools.GENERATE_FEATURE_TOOL
import com.jkjamies.cammp.mcp.tools.handleGenerateFeature
import dev.zacsweers.metro.createGraph
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ReadResourceResult
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextResourceContents
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered

fun main() {
    val graph = createGraph<McpGraph>()
    val generator = graph.generator

    val server = Server(
        serverInfo = Implementation(
            name = "cammp",
            version = CAMMP_VERSION,
        ),
        options = ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = false),
                resources = ServerCapabilities.Resources(subscribe = false, listChanged = false),
            ),
        ),
    )

    server.addTool(
        tool = GENERATE_FEATURE_TOOL,
    ) { request ->
        handleGenerateFeature(request.arguments, generator)
    }

    server.addResource(
        uri = "cammp://strategies/di",
        name = "DI Strategies",
        description = "Available dependency injection framework options for CAMMP code generation",
        mimeType = "text/markdown",
    ) {
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = DI_STRATEGIES_TEXT,
                    uri = "cammp://strategies/di",
                    mimeType = "text/markdown",
                ),
            ),
        )
    }

    server.addResource(
        uri = "cammp://strategies/datasource",
        name = "Datasource Strategies",
        description = "Available datasource module generation strategies for CAMMP",
        mimeType = "text/markdown",
    ) {
        ReadResourceResult(
            contents = listOf(
                TextResourceContents(
                    text = DATASOURCE_STRATEGIES_TEXT,
                    uri = "cammp://strategies/datasource",
                    mimeType = "text/markdown",
                ),
            ),
        )
    }

    runBlocking {
        val transport = StdioServerTransport(
            inputStream = System.`in`.asSource().buffered(),
            outputStream = System.out.asSink().buffered(),
        )
        server.createSession(transport)
    }
}
