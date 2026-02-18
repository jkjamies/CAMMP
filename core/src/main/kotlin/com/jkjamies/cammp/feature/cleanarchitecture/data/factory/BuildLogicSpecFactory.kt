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

import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Pure content factory for `build-logic/` scaffold files.
 */
interface BuildLogicSpecFactory {
    /**
     * Applies the org/package token replacement to build-logic templates.
     */
    fun applyPackageTokens(rawTemplate: String, orgCenter: String): String
}

@ContributesBinding(AppScope::class)
class BuildLogicSpecFactoryImpl : BuildLogicSpecFactory {

    override fun applyPackageTokens(rawTemplate: String, orgCenter: String): String {
        val org = sanitizeOrgCenter(orgCenter)
        val orgPath = org.replace('.', '/')

        var out = rawTemplate
        out = out.replace(Regex("\\$\\{\\s*PACKAGE\\s*}"), org)
        out = out.replace("com.PACKAGE.", "com.$org.")
        out = out.replace("com/PACKAGE/", "com/$orgPath/")
        out = out.replace("PACKAGE", org)
        return out
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

