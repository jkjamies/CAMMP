/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.plugins.template.util

import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
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
                
                // If this is an implementation in a 'domain' module, and we already have an entry
                // for this feature's 'api' module, we might want to skip it if it's the same use case.
                // However, the current structure groups by gradlePath.
                
                itemsByModule.computeIfAbsent(gradlePath) { mutableListOf() }.add(fqn)
            }
        }
    }

    traverse(baseVf)

    // Post-process to handle api vs domain preference within the same feature
    val result = linkedMapOf<String, List<String>>()
    val allModules = itemsByModule.keys.toList()
    
    for (modulePath in allModules) {
        if (modulePath.endsWith(":domain")) {
            val featurePath = modulePath.removeSuffix(":domain")
            val apiPath = "$featurePath:api"
            if (itemsByModule.containsKey(apiPath)) {
                // If api module exists, we only include use cases from api, 
                // unless it's empty (unlikely if domain has some, but safety first)
                val apiUseCases = itemsByModule[apiPath] ?: emptyList()
                if (apiUseCases.isNotEmpty()) {
                    result[apiPath] = apiUseCases
                    // Skip adding domain module for this feature
                    continue
                }
            }
        } else if (modulePath.endsWith(":api")) {
            // Handled by the :domain check above or just added directly if no :domain exists
            result[modulePath] = itemsByModule[modulePath] ?: emptyList()
            continue
        }
        
        result[modulePath] = itemsByModule[modulePath] ?: emptyList()
    }

    return result
}
