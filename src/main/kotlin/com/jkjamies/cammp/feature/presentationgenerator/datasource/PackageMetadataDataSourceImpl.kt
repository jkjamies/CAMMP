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

package com.jkjamies.cammp.feature.presentationgenerator.datasource

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.jkjamies.cammp.domain.codegen.PackageSuffixes
import com.jkjamies.cammp.feature.presentationgenerator.data.datasource.PackageMetadataDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

/**
 * Infers the best "presentation" package to use given a set of discovered Kotlin packages.
 *
 * This function is intentionally pure so it can be unit tested without requiring IntelliJ VFS fixtures.
 */
internal fun inferPresentationPackageFrom(packages: Set<String>): String? {
    if (packages.isEmpty()) return null

    val exactPresentation = packages.firstOrNull { it.endsWith(PackageSuffixes.PRESENTATION) }
    if (exactPresentation != null) return exactPresentation

    val apiPkg = packages.firstOrNull { it.endsWith(".api") || it.contains(".api.") }
    if (apiPkg != null) {
        val idx = apiPkg.indexOf(".api")
        val base = if (idx >= 0) apiPkg.substring(0, idx) else apiPkg
        return base + PackageSuffixes.PRESENTATION
    }

    val domainPkg = packages.firstOrNull { it.endsWith(PackageSuffixes.DOMAIN) || it.contains("${PackageSuffixes.DOMAIN}.") }
    if (domainPkg != null) {
        val idx = domainPkg.indexOf(PackageSuffixes.DOMAIN)
        val base = if (idx >= 0) domainPkg.substring(0, idx) else domainPkg
        return base + PackageSuffixes.PRESENTATION
    }

    val dataPkg = packages.firstOrNull { it.endsWith(PackageSuffixes.DATA) || it.contains("${PackageSuffixes.DATA}.") }
    if (dataPkg != null) {
        val idx = dataPkg.indexOf(PackageSuffixes.DATA)
        val base = if (idx >= 0) dataPkg.substring(0, idx) else dataPkg
        return base + PackageSuffixes.PRESENTATION
    }

    val shortest = packages.minByOrNull { it.length } ?: return null
    return if (shortest.endsWith(PackageSuffixes.PRESENTATION)) shortest else shortest + PackageSuffixes.PRESENTATION
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
        return inferPresentationPackageFrom(packages)
    }
}