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

package com.jkjamies.cammp.feature.repositorygenerator.datasource

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.jkjamies.cammp.domain.codegen.PackageSuffixes
import com.jkjamies.cammp.feature.repositorygenerator.data.datasource.PackageMetadataDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

/**
 * Infers the best base module package for repository generation given discovered Kotlin packages.
 *
 * This function is intentionally pure so it can be unit tested without requiring IntelliJ VFS fixtures.
 */
internal fun inferRepositoryModulePackageFrom(packages: Set<String>, moduleName: String?): String? {
    if (packages.isEmpty()) return null

    val name = moduleName?.lowercase().orEmpty()
    val preferredSuffix = when (name) {
        "data" -> PackageSuffixes.DATA
        "domain" -> PackageSuffixes.DOMAIN
        "di" -> PackageSuffixes.DI
        else -> null
    }

    if (preferredSuffix != null) {
        val exact = packages.firstOrNull { it.endsWith(preferredSuffix) }
        if (exact != null) return exact

        val containing = packages.firstOrNull { it.contains(preferredSuffix) }
        if (containing != null) {
            val idx = containing.indexOf(preferredSuffix)
            if (idx >= 0) return containing.substring(0, idx + preferredSuffix.length)
        }
    }

    return packages.minByOrNull { it.length } ?: packages.first()
}

@ContributesBinding(AppScope::class)
internal class PackageMetadataDataSourceImpl : PackageMetadataDataSource {
    override fun findModulePackage(moduleDir: Path): String? {
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(moduleDir.toString())
            ?: return null
        val kotlinRoot = VfsUtil.findRelativeFile("src/main/kotlin", vf)
            ?: return null

        val packages = mutableSetOf<String>()

        fun visit(dir: com.intellij.openapi.vfs.VirtualFile) {
            if (!dir.isDirectory) return
            dir.children.forEach { child ->
                if (child.isDirectory) visit(child)
                else if (child.name.endsWith(".kt")) {
                    val text = child.contentsToByteArray(false).toString(Charsets.UTF_8)
                    val line = text.lineSequence().firstOrNull { it.trimStart().startsWith("package ") }
                    if (line != null) packages += line.removePrefix("package ").trim()
                }
            }
        }

        visit(kotlinRoot)
        return inferRepositoryModulePackageFrom(packages, moduleDir.fileName?.toString())
    }
}