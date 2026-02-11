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

package com.jkjamies.cammp.feature.usecasegenerator.datasource

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.jkjamies.cammp.domain.codegen.PackageSuffixes
import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.PackageMetadataDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

/**
 * Infers the best "domain.usecase" package to use given a set of discovered Kotlin packages.
 *
 * This function is intentionally pure so it can be unit tested without requiring IntelliJ VFS fixtures.
 */
internal fun inferUseCasePackageFrom(packages: Set<String>): String? {
    if (packages.isEmpty()) return null

    // Prefer exact .domain.usecase package
    val exactUseCase = packages.firstOrNull { it.endsWith("${PackageSuffixes.DOMAIN}${PackageSuffixes.USE_CASE}") }
    if (exactUseCase != null) return exactUseCase

    // Prefer exact .domain package and append .usecase
    val exactDomain = packages.firstOrNull { it.endsWith(PackageSuffixes.DOMAIN) }
    if (exactDomain != null) return exactDomain + PackageSuffixes.USE_CASE

    // Prefer any that contains .domain, truncated to .domain and append .usecase
    val containingDomain = packages.firstOrNull { it.contains(PackageSuffixes.DOMAIN) }
    if (containingDomain != null) {
        val idx = containingDomain.indexOf(PackageSuffixes.DOMAIN)
        return containingDomain.substring(0, idx + PackageSuffixes.DOMAIN.length) + PackageSuffixes.USE_CASE
    }

    // Fallback: choose the shortest package and attempt to append .usecase
    val shortest = packages.minByOrNull { it.length } ?: return null
    return if (shortest.endsWith(PackageSuffixes.USE_CASE)) shortest else shortest + PackageSuffixes.USE_CASE
}

@ContributesBinding(AppScope::class)
internal class PackageMetadataDataSourceImpl : PackageMetadataDataSource {
    override fun findModulePackage(moduleDir: Path): String? {
        val vf = LocalFileSystem.getInstance().refreshAndFindFileByPath(moduleDir.toString())
            ?: return null
        val kotlinRoot = VfsUtil.findRelativeFile("src/main/kotlin", vf) ?: return null

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
        return inferUseCasePackageFrom(packages)
    }
}