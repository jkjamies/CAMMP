package com.github.jkjamies.cammp.feature.usecasegenerator.data

import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import java.nio.file.Path

class ModulePackageRepositoryImpl : ModulePackageRepository {
    override fun findModulePackage(moduleDir: Path): String? {
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(moduleDir.toString())
            ?: return null
        val kotlinRoot = VfsUtil.findRelativeFile("src/main/kotlin", vf) ?: return null
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
        if (packages.isEmpty()) return null
        // Prefer exact .domain.usecase package
        val exactUseCase = packages.firstOrNull { it.endsWith(".domain.usecase") }
        if (exactUseCase != null) return exactUseCase
        // Prefer exact .domain package and append .usecase
        val exactDomain = packages.firstOrNull { it.endsWith(".domain") }
        if (exactDomain != null) return exactDomain + ".usecase"
        // Prefer any that contains .domain, truncated to .domain and append .usecase
        val containingDomain = packages.firstOrNull { it.contains(".domain") }
        if (containingDomain != null) {
            val idx = containingDomain.indexOf(".domain")
            return containingDomain.substring(0, idx + ".domain".length) + ".usecase"
        }
        // Fallback: choose the shortest package and attempt to append .usecase
        val shortest = packages.minByOrNull { it.length }
        return shortest?.let { if (it.endsWith(".usecase")) it else it + ".usecase" }
    }
}
