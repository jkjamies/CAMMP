package com.github.jkjamies.cammp.feature.cleanarchitecture.data

import com.github.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsRepository
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

class GradleSettingsRepositoryImpl : GradleSettingsRepository {
    override fun ensureIncludes(projectBase: Path, root: String, feature: String, modules: List<String>): Boolean {
        val settings = projectBase.resolve("settings.gradle.kts")
        if (!settings.exists()) return false
        var text = settings.readText()
        var updated = false
        val rootSegments = root.replace('\\', '/').split('/')
            .filter { it.isNotBlank() }
        modules.forEach { m ->
            val includePath = buildString {
                append(':')
                if (rootSegments.isNotEmpty()) {
                    append(rootSegments.joinToString(":"))
                    append(':')
                }
                append(feature)
                append(':')
                append(m)
            }
            val includeLine = "include(\"$includePath\")"
            if (!text.contains(includeLine)) {
                text += "\n$includeLine"
                updated = true
            }
        }
        if (updated) settings.writeText(text)
        return updated
    }

    override fun ensureIncludeBuild(projectBase: Path, buildLogicName: String): Boolean {
        val settings = projectBase.resolve("settings.gradle.kts")
        if (!settings.exists()) return false
        var text = settings.readText()
        val token = "includeBuild(\"$buildLogicName\")"
        return if (!text.contains(token)) {
            text += "\n$token"
            settings.writeText(text)
            true
        } else false
    }

    override fun ensureVersionCatalogPluginAliases(projectBase: Path, orgSegment: String, enabledModules: List<String>): Boolean {
        val gradleDir = projectBase.resolve("gradle")
        val catalog = gradleDir.resolve("libs.versions.toml")
        val safeOrg = orgSegment.trim().ifEmpty { "cammp" }
        val requiredLayers = buildList {
            add("domain"); add("data")
            if (enabledModules.contains("di")) add("di")
            if (enabledModules.contains("presentation")) add("presentation")
            if (enabledModules.contains("dataSource")) add("dataSource")
            if (enabledModules.contains("remoteDataSource")) add("remoteDataSource")
            if (enabledModules.contains("localDataSource")) add("localDataSource")
        }

        if (!catalog.exists()) {
            val sb = StringBuilder()
            sb.appendLine("[versions]")
            sb.appendLine()
            sb.appendLine("[libraries]")
            sb.appendLine()
            sb.appendLine("[bundles]")
            sb.appendLine()
            sb.appendLine("[plugins]")
            requiredLayers.forEach { layer ->
                val alias = "convention-android-library-$layer"
                val id = "com.$safeOrg.convention.android.library.$layer"
                sb.appendLine("$alias = { id = \"$id\" }")
            }
            if (!gradleDir.exists()) gradleDir.createDirectories()
            catalog.writeText(sb.toString())
            return true
        }

        var text = catalog.readText()
        if (!Regex("(?m)^\\[plugins]\\s*").containsMatchIn(text)) {
            if (!text.endsWith("\n")) text += "\n"
            text += "\n[plugins]\n"
        }
        val pluginsSectionRegex = Regex("(?s)\\[plugins]\\s*(.*?)(?:\\n\\[[^\\]]+]|$)")
        val match = pluginsSectionRegex.find(text)
        val pluginsBlock = match?.groups?.get(1)?.value ?: ""
        val additions = StringBuilder()
        requiredLayers.forEach { layer ->
            val alias = "convention-android-library-$layer"
            val id = "com.$safeOrg.convention.android.library.$layer"
            val aliasRegex = Regex("(?m)^\\s*${Regex.escape(alias)}\\s*=\\s*\\{.*\\}")
            if (!aliasRegex.containsMatchIn(pluginsBlock)) {
                additions.appendLine("$alias = { id = \"$id\" }")
            }
        }
        if (additions.isNotEmpty()) {
            val updated = text.replace(pluginsSectionRegex) { mr ->
                val existing = mr.groups[1]?.value ?: ""
                val content = if (existing.isNotBlank() && !existing.endsWith("\n")) existing + "\n" else existing
                "[plugins]\n" + content + additions.toString()
            }
            catalog.writeText(updated)
            return true
        }
        return false
    }
}
