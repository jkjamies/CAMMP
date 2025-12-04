package com.github.jkjamies.cammp.feature.repositorygenerator.data

import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import java.nio.file.Path

class ModulePackageRepositoryImpl : ModulePackageRepository {
    override fun findModulePackage(moduleDir: Path): String {
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(moduleDir.toString())
            ?: error("Module dir not found: $moduleDir")
        val kotlinRoot = VfsUtil.findRelativeFile("src/main/kotlin", vf)
            ?: error("src/main/kotlin not found under $moduleDir")
        val packages = mutableSetOf<String>()
        fun visit(dir: com.intellij.openapi.vfs.VirtualFile) {
            if (!dir.isDirectory) return
            dir.children.forEach { child ->
                if (child.isDirectory) visit(child) else if (child.name.endsWith(".kt")) {
                    val text = child.contentsToByteArray(false).toString(Charsets.UTF_8)
                    val line = text.lineSequence().firstOrNull { it.trimStart().startsWith("package ") }
                    if (line != null) packages += line.removePrefix("package ").trim()
                }
            }
        }
        visit(kotlinRoot)
        if (packages.isEmpty()) error("Could not determine existing package for selected module: $moduleDir")
        val moduleName = moduleDir.fileName?.toString()?.lowercase() ?: ""
        val preferredSuffix = when {
            moduleName == "data" -> ".data"
            moduleName == "domain" -> ".domain"
            moduleName == "di" -> ".di"
            else -> null
        }
        if (preferredSuffix != null) {
            val exact = packages.firstOrNull { it.endsWith(preferredSuffix) }
            if (exact != null) return exact
            val containing = packages.firstOrNull { it.contains(preferredSuffix) }
            if (containing != null) {
                val idx = containing.indexOf(preferredSuffix)
                return containing.substring(0, idx + preferredSuffix.length)
            }
        }
        return packages.minByOrNull { it.length } ?: packages.first()
    }
}
