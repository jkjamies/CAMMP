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

package com.jkjamies.cammp.feature.cleanarchitecture.data.factory

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Creates source file content for generated modules.
 *
 * Pure content factory: no filesystem access.
 */
interface ModuleSourceSpecFactory {
    fun placeholderKotlinFile(params: CleanArchitectureParams, moduleName: String, featureName: String): String

    fun packageName(params: CleanArchitectureParams, moduleName: String, featureName: String): String
}

@ContributesBinding(AppScope::class)
internal class ModuleSourceSpecFactoryImpl : ModuleSourceSpecFactory {

    override fun packageName(params: CleanArchitectureParams, moduleName: String, featureName: String): String {
        val safeOrg = sanitizeOrgCenter(params.orgCenter)
        return "com.$safeOrg.${params.root}.$featureName.$moduleName"
    }

    override fun placeholderKotlinFile(params: CleanArchitectureParams, moduleName: String, featureName: String): String {
        val pkg = packageName(params, moduleName, featureName)
        return """
            package $pkg

            class Placeholder
        """.trimIndent()
    }

    private fun sanitizeOrgCenter(input: String): String {
        val trimmed = input.trim()
        val unwrapped = if (trimmed.startsWith("\${") && trimmed.endsWith("}")) {
            trimmed.removePrefix("\${").removeSuffix("}")
        } else trimmed
        val withoutLeading = unwrapped.removePrefix("com.").removePrefix("org.")
        val cleaned = withoutLeading.replace(Regex("[^A-Za-z0-9_.]"), "").trim('.')
        return cleaned.ifBlank { "cammp" }.replaceFirstChar { it.lowercase() }
    }
}
