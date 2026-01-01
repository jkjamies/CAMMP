package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsRepository
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
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

    override fun ensureVersionCatalogPluginAliases(
        projectBase: Path,
        orgSegment: String,
        enabledModules: List<String>
    ): Boolean {
        val gradleDir = projectBase.resolve("gradle")
        val catalog = gradleDir.resolve("libs.versions.toml")
        val safeOrg = orgSegment.trim().ifEmpty { "cammp" }
        // Ensure org name starts with lowercase for convention plugin IDs
        val conventionOrg = safeOrg.replaceFirstChar { it.lowercase() }
        
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
                val id = "com.$conventionOrg.convention.android.library.$layer"
                sb.appendLine("$alias = { id = \"$id\" }")
            }
            if (!gradleDir.exists()) gradleDir.createDirectories()
            catalog.writeText(sb.toString())
            return true
        }

        var text = catalog.readText()
        if (!Regex("(?m)^\\s*\\[plugins]\\s*").containsMatchIn(text)) {
            if (!text.endsWith("\n")) text += "\n"
            text += "\n[plugins]\n"
        }
        val pluginsSectionRegex = Regex("(?s)\\[plugins]\\s*(.*?)(?=\\n\\s*\\[[^\\]]+]|$)")
        val match = pluginsSectionRegex.find(text)
        val pluginsBlock = match?.groups?.get(1)?.value ?: ""
        val additions = StringBuilder()
        requiredLayers.forEach { layer ->
            val alias = "convention-android-library-$layer"
            val id = "com.$conventionOrg.convention.android.library.$layer"
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

    override fun ensureAppDependency(projectBase: Path, root: String, feature: String, diMode: DiMode): Boolean {
        val appBuild = projectBase.resolve("app/build.gradle.kts")
        if (!appBuild.exists()) return false
        var text = appBuild.readText()
        var changed = false

        val rootSegments = root.replace('\\', '/').split('/')
            .filter { it.isNotBlank() }
        val projectPath = buildString {
            append(':')
            if (rootSegments.isNotEmpty()) {
                append(rootSegments.joinToString(":"))
                append(':')
            }
            append(feature)
            append(":di")
        }

        val dependencyLine = "implementation(project(\"$projectPath\"))"

        if (!text.contains(dependencyLine)) {
            // Try to insert into dependencies block
            val dependenciesRegex = Regex("(?s)dependencies\\s*\\{(.*?)\\}")
            val match = dependenciesRegex.find(text)

            if (match != null) {
                text = text.replace(dependenciesRegex) { mr ->
                    val content = mr.groups[1]?.value ?: ""
                    "dependencies {$content\n    $dependencyLine\n}"
                }
                changed = true
            } else {
                // If no dependencies block, append one
                text += "\n\ndependencies {\n    $dependencyLine\n}"
                changed = true
            }
        }

        // Handle Kotlin 2.3.0+ metadata workaround for Hilt
        if (diMode == DiMode.HILT) {
            // Check Kotlin version in catalog
            val catalog = projectBase.resolve("gradle/libs.versions.toml")
            if (catalog.exists()) {
                val catalogText = catalog.readText()
                val kotlinVersionRegex = Regex("(?m)^\\s*kotlin\\s*=\\s*\"([^\"]+)\"")
                val match = kotlinVersionRegex.find(catalogText)
                val kotlinVersion = match?.groups?.get(1)?.value

                if (kotlinVersion != null && isKotlinVersionAtLeast(kotlinVersion, "2.3.0")) {
                    // 1. Ensure library in version catalog
                    val catalogChanged = ensureKotlinMetadataInCatalog(projectBase)
                    
                    // 2. Ensure dependency in app/build.gradle.kts
                    val metadataDependency = "ksp(libs.kotlin.metadata.jvm)"
                    if (!text.contains(metadataDependency)) {
                        val dependenciesRegex = Regex("(?s)dependencies\\s*\\{(.*?)\\}")
                        val matchDep = dependenciesRegex.find(text)
                        if (matchDep != null) {
                            text = text.replace(dependenciesRegex) { mr ->
                                val content = mr.groups[1]?.value ?: ""
                                "dependencies {$content\n    $metadataDependency\n}"
                            }
                            changed = true
                        }
                    }
                    if (catalogChanged) changed = true
                }
            }
        }

        if (changed) {
            appBuild.writeText(text)
        }
        return changed
    }

    private fun isKotlinVersionAtLeast(current: String, target: String): Boolean {
        val currentParts = current.split('.').mapNotNull { it.toIntOrNull() }
        val targetParts = target.split('.').mapNotNull { it.toIntOrNull() }
        
        for (i in 0 until maxOf(currentParts.size, targetParts.size)) {
            val c = currentParts.getOrElse(i) { 0 }
            val t = targetParts.getOrElse(i) { 0 }
            if (c > t) return true
            if (c < t) return false
        }
        return true
    }

    private fun ensureKotlinMetadataInCatalog(projectBase: Path): Boolean {
        val catalog = projectBase.resolve("gradle/libs.versions.toml")
        if (!catalog.exists()) return false
        var text = catalog.readText()
        var changed = false

        // Ensure version
        if (!text.contains("kotlin-metadata-jvm = \"2.3.0\"")) {
             if (!Regex("(?m)^\\[versions]\\s*").containsMatchIn(text)) {
                 if (!text.endsWith("\n")) text += "\n"
                 text += "\n[versions]\n"
             }
             val versionsRegex = Regex("(?s)\\[versions]\\s*(.*?)(?=\\n\\s*\\[[^\\]]+]|$)")
             val match = versionsRegex.find(text)
             if (match != null) {
                 val block = match.groups[1]?.value ?: ""
                 if (!block.contains("kotlin-metadata-jvm")) {
                     text = text.replace(versionsRegex) { mr ->
                         val content = mr.groups[1]?.value ?: ""
                         val newContent = if (content.isNotBlank() && !content.endsWith("\n")) content + "\n" else content
                         "[versions]\n" + newContent + "kotlin-metadata-jvm = \"2.3.0\"\n"
                     }
                     changed = true
                 }
             }
        }

        // Ensure library
        if (!text.contains("kotlin-metadata-jvm = { group = \"org.jetbrains.kotlin\", name = \"kotlin-metadata-jvm\", version.ref = \"kotlin-metadata-jvm\" }")) {
             if (!Regex("(?m)^\\[libraries]\\s*").containsMatchIn(text)) {
                 if (!text.endsWith("\n")) text += "\n"
                 text += "\n[libraries]\n"
             }
             val libsRegex = Regex("(?s)\\[libraries]\\s*(.*?)(?=\\n\\s*\\[[^\\]]+]|$)")
             val match = libsRegex.find(text)
             if (match != null) {
                 val block = match.groups[1]?.value ?: ""
                 if (!block.contains("kotlin-metadata-jvm = {")) {
                     text = text.replace(libsRegex) { mr ->
                         val content = mr.groups[1]?.value ?: ""
                         val newContent = if (content.isNotBlank() && !content.endsWith("\n")) content + "\n" else content
                         "[libraries]\n" + newContent + "kotlin-metadata-jvm = { group = \"org.jetbrains.kotlin\", name = \"kotlin-metadata-jvm\", version.ref = \"kotlin-metadata-jvm\" }\n"
                     }
                     changed = true
                 }
             }
        }
        
        if (changed) {
            catalog.writeText(text)
        }
        return changed
    }
}
