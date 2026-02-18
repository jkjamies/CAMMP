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

package com.jkjamies.cammp.mcp.tools

import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase.CleanArchitectureGenerator
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.Tool
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.boolean
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonArray
import kotlinx.serialization.json.putJsonObject
import java.nio.file.Paths

val GENERATE_FEATURE_TOOL = Tool(
    name = "generate_feature",
    description = "Generate a Clean Architecture module structure for a feature. " +
        "Creates domain, data, datasource, presentation, DI, and API modules " +
        "with Gradle build files, convention plugins, and DI wiring.",
    inputSchema = ToolSchema(
        properties = buildJsonObject {
            putJsonObject("featureName") {
                put("type", "string")
                put("description", "Feature name, supports paths e.g. 'account/login'")
            }
            putJsonObject("projectPath") {
                put("type", "string")
                put("description", "Absolute path to the project root directory")
            }
            putJsonObject("packageName") {
                put("type", "string")
                put("description", "Base package name, e.g. 'com.example.app'")
            }
            putJsonObject("rootModule") {
                put("type", "string")
                put("description", "Root module path in settings.gradle (default: 'app')")
            }
            putJsonObject("datasourceStrategy") {
                put("type", "string")
                put("description", "Datasource strategy: none, combined, local_only, remote_only, local_and_remote")
                putJsonArray("enum") {
                    add(JsonPrimitive("none"))
                    add(JsonPrimitive("combined"))
                    add(JsonPrimitive("local_only"))
                    add(JsonPrimitive("remote_only"))
                    add(JsonPrimitive("local_and_remote"))
                }
            }
            putJsonObject("diStrategy") {
                put("type", "string")
                put("description", "Dependency injection framework: hilt, koin, koin_annotations, metro")
                putJsonArray("enum") {
                    add(JsonPrimitive("hilt"))
                    add(JsonPrimitive("koin"))
                    add(JsonPrimitive("koin_annotations"))
                    add(JsonPrimitive("metro"))
                }
            }
            putJsonObject("includePresentation") {
                put("type", "boolean")
                put("description", "Whether to include a presentation module (default: true)")
            }
            putJsonObject("includeApiModule") {
                put("type", "boolean")
                put("description", "Whether to include an API module (default: false)")
            }
            putJsonObject("includeDiModule") {
                put("type", "boolean")
                put("description", "Whether to include a DI module (default: true)")
            }
        },
        required = listOf("featureName", "projectPath", "packageName"),
    ),
)

suspend fun handleGenerateFeature(
    arguments: JsonObject?,
    generator: CleanArchitectureGenerator,
): CallToolResult {
    if (arguments == null) {
        return CallToolResult(
            content = listOf(TextContent("Error: No arguments provided")),
            isError = true,
        )
    }

    val featureName = arguments["featureName"]?.jsonPrimitive?.contentOrNull
    val projectPath = arguments["projectPath"]?.jsonPrimitive?.contentOrNull
    val packageName = arguments["packageName"]?.jsonPrimitive?.contentOrNull

    if (featureName.isNullOrBlank() || projectPath.isNullOrBlank() || packageName.isNullOrBlank()) {
        return CallToolResult(
            content = listOf(TextContent("Error: featureName, projectPath, and packageName are required")),
            isError = true,
        )
    }

    val rootModule = arguments["rootModule"]?.jsonPrimitive?.contentOrNull ?: "app"
    val datasourceStrategy = parseDatasourceStrategy(
        arguments["datasourceStrategy"]?.jsonPrimitive?.contentOrNull ?: "none"
    )
    val diStrategy = parseDiStrategy(
        arguments["diStrategy"]?.jsonPrimitive?.contentOrNull ?: "hilt"
    )
    val includePresentation = arguments["includePresentation"]?.jsonPrimitive?.booleanOrNull ?: true
    val includeApiModule = arguments["includeApiModule"]?.jsonPrimitive?.booleanOrNull ?: false
    val includeDiModule = arguments["includeDiModule"]?.jsonPrimitive?.booleanOrNull ?: true

    if (datasourceStrategy == null) {
        return CallToolResult(
            content = listOf(
                TextContent(
                    "Error: Invalid datasourceStrategy. " +
                        "Valid values: none, combined, local_only, remote_only, local_and_remote"
                )
            ),
            isError = true,
        )
    }

    if (diStrategy == null) {
        return CallToolResult(
            content = listOf(
                TextContent("Error: Invalid diStrategy. Valid values: hilt, koin, koin_annotations, metro")
            ),
            isError = true,
        )
    }

    val params = CleanArchitectureParams(
        projectBasePath = Paths.get(projectPath),
        root = rootModule,
        feature = featureName,
        orgCenter = packageName,
        includePresentation = includePresentation,
        includeApiModule = includeApiModule,
        includeDiModule = includeDiModule,
        datasourceStrategy = datasourceStrategy,
        diStrategy = diStrategy,
    )

    return generator(params).fold(
        onSuccess = { result ->
            val summary = buildString {
                appendLine("Feature '$featureName' generated successfully.")
                appendLine()
                if (result.created.isNotEmpty()) {
                    appendLine("Created modules:")
                    result.created.forEach { appendLine("  - $it") }
                }
                if (result.skipped.isNotEmpty()) {
                    appendLine("Skipped (already exist):")
                    result.skipped.forEach { appendLine("  - $it") }
                }
                if (result.settingsUpdated) appendLine("settings.gradle.kts updated with new module includes.")
                if (result.buildLogicCreated) appendLine("Build logic (convention plugins) created.")
                if (result.message.isNotBlank()) {
                    appendLine()
                    appendLine(result.message)
                }
            }
            CallToolResult(content = listOf(TextContent(summary)))
        },
        onFailure = { error ->
            CallToolResult(
                content = listOf(TextContent("Error generating feature: ${error.message}")),
                isError = true,
            )
        },
    )
}

internal fun parseDatasourceStrategy(value: String): DatasourceStrategy? = when (value.lowercase()) {
    "none" -> DatasourceStrategy.None
    "combined" -> DatasourceStrategy.Combined
    "local_only" -> DatasourceStrategy.LocalOnly
    "remote_only" -> DatasourceStrategy.RemoteOnly
    "local_and_remote" -> DatasourceStrategy.RemoteAndLocal
    else -> null
}

internal fun parseDiStrategy(value: String): DiStrategy? = when (value.lowercase()) {
    "hilt" -> DiStrategy.Hilt
    "koin" -> DiStrategy.Koin(useAnnotations = false)
    "koin_annotations" -> DiStrategy.Koin(useAnnotations = true)
    "metro" -> DiStrategy.Metro
    else -> null
}
