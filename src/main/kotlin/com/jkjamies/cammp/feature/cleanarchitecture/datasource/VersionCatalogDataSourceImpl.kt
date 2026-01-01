package com.jkjamies.cammp.feature.cleanarchitecture.datasource

import com.jkjamies.cammp.feature.cleanarchitecture.data.datasource.VersionCatalogDataSource
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import java.nio.file.Path

class VersionCatalogDataSourceImpl(
    private val fs: FileSystemRepository
) : VersionCatalogDataSource {

    override fun getLibraryAlias(
        tomlPath: Path,
        alias: String,
        group: String,
        artifact: String,
        version: String?,
        versionRef: String?
    ): String {
        val sections = parseToml(tomlPath)
        
        // Check if library exists
        val existingAlias = findLibraryAlias(sections["libraries"] ?: emptyList(), group, artifact)
        if (existingAlias != null) return existingAlias

        val ref = versionRef ?: alias
        // Add version if needed
        if (version != null) {
            addVersion(sections, ref, version)
        }

        // Add library
        val libraryEntry = buildString {
            append("$alias = { group = \"$group\", name = \"$artifact\"")
            if (version != null) {
                append(", version.ref = \"$ref\"")
            }
            append(" }")
        }
        addToSection(sections, "libraries", libraryEntry)

        writeToml(tomlPath, sections)
        return alias
    }

    override fun getPluginAlias(
        tomlPath: Path,
        alias: String,
        id: String,
        version: String?,
        versionRef: String?
    ): String {
        val sections = parseToml(tomlPath)

        // Check if plugin exists
        val existingAlias = findPluginAlias(sections["plugins"] ?: emptyList(), id)
        if (existingAlias != null) return existingAlias

        val ref = versionRef ?: alias
        // Add version if needed
        if (version != null) {
            addVersion(sections, ref, version)
        }

        // Add plugin
        val pluginEntry = buildString {
            append("$alias = { id = \"$id\"")
            if (version != null) {
                append(", version.ref = \"$ref\"")
            }
            append(" }")
        }
        addToSection(sections, "plugins", pluginEntry)

        writeToml(tomlPath, sections)
        return alias
    }

    private fun parseToml(path: Path): MutableMap<String, MutableList<String>> {
        val sections = mutableMapOf<String, MutableList<String>>()
        if (!fs.exists(path)) return sections

        val text = fs.readText(path) ?: return sections
        var currentSection = "header" // content before any section
        
        text.lines().forEach { line ->
            val trimmed = line.trim()
            // Handle comments in section headers
            val potentialHeader = trimmed.substringBefore("#").trim()
            
            if (potentialHeader.startsWith("[") && potentialHeader.endsWith("]")) {
                currentSection = potentialHeader.substring(1, potentialHeader.length - 1)
            } else {
                if (line.isNotBlank()) {
                    sections.getOrPut(currentSection) { mutableListOf() }.add(line)
                }
            }
        }
        return sections
    }

    private fun writeToml(path: Path, sections: Map<String, List<String>>) {
        val sb = StringBuilder()
        
        // Order: versions, libraries, bundles, plugins
        val order = listOf("versions", "libraries", "bundles", "plugins")
        
        // Write header content if any (usually comments or empty lines at top)
        sections["header"]?.forEach { sb.appendLine(it) }

        order.forEach { sectionName ->
            val lines = sections[sectionName]
            if (!lines.isNullOrEmpty()) {
                if (sb.isNotEmpty()) sb.appendLine()
                sb.appendLine("[$sectionName]")
                lines.forEach { sb.appendLine(it) }
            }
        }

        // Write any other sections not in the standard order
        sections.keys.filter { it !in order && it != "header" }.forEach { sectionName ->
            if (sb.isNotEmpty()) sb.appendLine()
            sb.appendLine("[$sectionName]")
            sections[sectionName]?.forEach { sb.appendLine(it) }
        }

        fs.writeText(path, sb.toString())
    }

    private fun findLibraryAlias(lines: List<String>, group: String, artifact: String): String? {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || !trimmed.contains("=")) continue
            
            if ((trimmed.contains("\"$group:$artifact\"") || trimmed.contains("'$group:$artifact'")) ||
                (trimmed.contains("group = \"$group\"") && trimmed.contains("name = \"$artifact\""))) {
                return trimmed.substringBefore("=").trim()
            }
        }
        return null
    }

    private fun findPluginAlias(lines: List<String>, id: String): String? {
        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.startsWith("#") || !trimmed.contains("=")) continue
            
            if (trimmed.contains("\"$id\"") || trimmed.contains("'$id'")) {
                return trimmed.substringBefore("=").trim()
            }
        }
        return null
    }

    private fun addVersion(sections: MutableMap<String, MutableList<String>>, key: String, version: String) {
        val versions = sections.getOrPut("versions") { mutableListOf() }
        // Check if key already exists
        val exists = versions.any { it.trim().startsWith("$key =") || it.trim().startsWith("$key=") }
        if (!exists) {
            versions.add("$key = \"$version\"")
        }
    }

    private fun addToSection(sections: MutableMap<String, MutableList<String>>, sectionName: String, entry: String) {
        sections.getOrPut(sectionName) { mutableListOf() }.add(entry)
    }
}
