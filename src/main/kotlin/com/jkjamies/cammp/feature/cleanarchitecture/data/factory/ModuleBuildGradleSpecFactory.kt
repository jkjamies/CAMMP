package com.jkjamies.cammp.feature.cleanarchitecture.data.factory

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/**
 * Creates the `build.gradle.kts` content for a generated clean-architecture module.
 *
 * This is a pure content factory: no filesystem access, no side effects.
 */
interface ModuleBuildGradleSpecFactory {
    fun create(
        params: CleanArchitectureParams,
        moduleName: String,
        featureName: String,
        enabledModules: List<String>,
        rawTemplate: String,
    ): String
}

@ContributesBinding(AppScope::class)
@Inject
class ModuleBuildGradleSpecFactoryImpl : ModuleBuildGradleSpecFactory {

    override fun create(
        params: CleanArchitectureParams,
        moduleName: String,
        featureName: String,
        enabledModules: List<String>,
        rawTemplate: String,
    ): String {
        val safeOrg = sanitizeOrgCenter(params.orgCenter)
        val namespace = "com.$safeOrg.${params.root}.$featureName.$moduleName"

        val projectPrefix = buildString {
            append(':')
            val rootSegments = params.root.replace('\\', '/').split('/').filter { it.isNotBlank() }
            if (rootSegments.isNotEmpty()) {
                append(rootSegments.joinToString(":"))
                append(':')
            }
            append(params.feature)
        }

        val dependencies = buildString {
            when (moduleName) {
                "domain" -> {
                    if (params.includeApiModule) {
                        appendLine("    implementation(project(\"$projectPrefix:api\"))")
                    }
                }
                "data" -> appendLine("    implementation(project(\"$projectPrefix:domain\"))")
                "di" -> enabledModules.filter { it != "di" }.forEach { dep ->
                    appendLine("    implementation(project(\"$projectPrefix:$dep\"))")
                }
                "presentation" -> {
                    if (params.includeApiModule) {
                        appendLine("    implementation(project(\"$projectPrefix:api\"))")
                    } else {
                        appendLine("    implementation(project(\"$projectPrefix:domain\"))")
                    }
                }
                "dataSource", "remoteDataSource", "localDataSource" ->
                    appendLine("    implementation(project(\"$projectPrefix:data\"))")
            }
        }

        return rawTemplate
            .replace(Regex("\\$\\{\\s*NAMESPACE\\s*}"), namespace)
            .replace(Regex("\\$\\{\\s*DEPENDENCIES\\s*}"), dependencies.trimEnd())
            .let { replacePackageTokens(it, safeOrg) }
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

    private fun replacePackageTokens(text: String, orgCenter: String): String {
        val org = sanitizeOrgCenter(orgCenter)
        val orgPath = org.replace('.', '/')
        var out = text.replace(Regex("\\$\\{\\s*PACKAGE\\s*}"), org)
        out = out.replace("com.PACKAGE.", "com.$org.")
        out = out.replace("com/PACKAGE/", "com/$orgPath/")
        out = out.replace("PACKAGE", org)
        return out
    }
}
