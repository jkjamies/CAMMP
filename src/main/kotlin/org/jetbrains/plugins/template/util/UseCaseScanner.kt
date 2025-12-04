package org.jetbrains.plugins.template.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VfsUtil
import java.nio.file.Path
import java.nio.file.Paths

/**
 * Scans the project and returns a map of Gradle module path -> list of UseCase FQNs.
 */
fun refreshUseCases(project: Project): Map<String, List<String>>? {
    val basePath = project.basePath ?: return null
    val baseVf = LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath) ?: return null
    if (!baseVf.isDirectory) return null

    val itemsByModule = linkedMapOf<String, MutableList<String>>()
    val ignoreDirs = setOf(".git", ".gradle", ".idea", "build", "out", "gradle")

    fun findModuleDir(vf: VirtualFile): VirtualFile? {
        var cur: VirtualFile? = vf
        while (cur != null && cur != baseVf) {
            if (cur.findChild("build.gradle.kts") != null || cur.findChild("build.gradle") != null) return cur
            cur = cur.parent
        }
        return if (baseVf.findChild("build.gradle.kts") != null || baseVf.findChild("build.gradle") != null) baseVf else null
    }

    fun toGradlePath(moduleDir: VirtualFile): String {
        val base = Paths.get(baseVf.path).normalize()
        val mod = Paths.get(moduleDir.path).normalize()
        val rel: Path = try { base.relativize(mod) } catch (_: Throwable) { return ":" }
        val segs = rel.toString().replace('\\', '/').split('/').filter { it.isNotBlank() }
        return ":" + segs.joinToString(":")
    }

    fun findKotlinRoot(file: VirtualFile): VirtualFile? {
        var p = file.parent
        while (p != null && p != baseVf) {
            val path = p.path.replace('\\', '/')
            if (path.endsWith("/src/main/kotlin") || path.endsWith("/src/commonMain/kotlin")) return p
            p = p.parent
        }
        return null
    }

    fun deriveFqn(file: VirtualFile): String {
        val kotlinRoot = findKotlinRoot(file)
        val simple = file.name.removeSuffix(".kt")
        if (kotlinRoot != null) {
            val rootPath = kotlinRoot.path.trimEnd('/') + "/"
            val rel = file.path.substringAfter(rootPath)
            val pkgPath = rel.substringBeforeLast('/', "").removeSuffix("/")
            val pkg = pkgPath.replace('/', '.')
            return if (pkg.isBlank()) simple else "$pkg.$simple"
        }
        return simple
    }

    fun traverse(dir: VirtualFile) {
        if (!dir.isDirectory) return
        if (dir.name in ignoreDirs || dir.name.startsWith('.')) return
        dir.children.forEach { child ->
            if (child.isDirectory) {
                traverse(child)
            } else if (child.name.endsWith("UseCase.kt")) {
                val moduleDir = findModuleDir(child) ?: baseVf
                val gradlePath = toGradlePath(moduleDir)
                val fqn = deriveFqn(child)
                itemsByModule.computeIfAbsent(gradlePath) { mutableListOf() }.add(fqn)
            }
        }
    }

    traverse(baseVf)
    return itemsByModule
}

