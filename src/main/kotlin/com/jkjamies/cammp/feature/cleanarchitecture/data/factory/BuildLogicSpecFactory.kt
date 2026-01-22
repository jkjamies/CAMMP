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

