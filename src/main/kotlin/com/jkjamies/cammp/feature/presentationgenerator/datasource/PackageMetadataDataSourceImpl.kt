package com.jkjamies.cammp.feature.presentationgenerator.datasource

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.jkjamies.cammp.feature.presentationgenerator.data.datasource.PackageMetadataDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

@ContributesBinding(AppScope::class)
@Inject
class PackageMetadataDataSourceImpl : PackageMetadataDataSource {
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

        val exactPresentation = packages.firstOrNull { it.endsWith(".presentation") }
        if (exactPresentation != null) return exactPresentation

        val domainPkg = packages.firstOrNull { it.endsWith(".domain") || it.contains(".domain.") }
        if (domainPkg != null) {
            val idx = domainPkg.indexOf(".domain")
            val base = if (idx >= 0) domainPkg.substring(0, idx) else domainPkg
            return base + ".presentation"
        }
        val dataPkg = packages.firstOrNull { it.endsWith(".data") || it.contains(".data.") }
        if (dataPkg != null) {
            val idx = dataPkg.indexOf(".data")
            val base = if (idx >= 0) dataPkg.substring(0, idx) else dataPkg
            return base + ".presentation"
        }

        val shortest = packages.minByOrNull { it.length } ?: return null
        return if (shortest.endsWith(".presentation")) shortest else shortest + ".presentation"
    }
}