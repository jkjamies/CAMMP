package org.jetbrains.plugins.template.util

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile

/**
 * Scans a selected domain module directory for repositories grouped by package.
 * Returns Map<pkgFqn, List<Pair<simpleName, fqn>>>.
 */
fun findRepositoriesGroupedByPackage(domainDirPath: String): Map<String, List<Pair<String, String>>>? {
    if (domainDirPath.isBlank()) return null
    val domainVf = LocalFileSystem.getInstance().refreshAndFindFileByPath(domainDirPath) ?: return null
    if (!domainVf.isDirectory || !domainVf.name.equals("domain", ignoreCase = true)) return emptyMap()
    val kotlinRoot = VfsUtil.findRelativeFile("src/main/kotlin", domainVf) ?: return emptyMap()

    val map = linkedMapOf<String, MutableList<Pair<String, String>>>()
    val rootPath = kotlinRoot.path.trimEnd('/')
    fun visit(dir: VirtualFile) {
        if (!dir.isDirectory) return
        if (dir.name.equals("repository", ignoreCase = true)) {
            dir.children.filter { !it.isDirectory && it.name.endsWith(".kt") }.forEach { file ->
                val simple = file.name.removeSuffix(".kt")
                if (!simple.endsWith("Repository")) return@forEach
                val rel = file.path.substringAfter("$rootPath/")
                val pkgPath = rel.substringBeforeLast('/')
                val pkg = pkgPath.replace('/', '.')
                val fqn = "$pkg.$simple"
                map.computeIfAbsent(pkg) { mutableListOf() }.add(simple to fqn)
            }
        }
        dir.children.filter { it.isDirectory }.forEach { visit(it) }
    }
    visit(kotlinRoot)
    return map
}

