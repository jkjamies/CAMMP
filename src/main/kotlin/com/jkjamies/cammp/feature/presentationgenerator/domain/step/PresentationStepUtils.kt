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

package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

data class ScreenDirSetup(
    val sanitizedName: String,
    val basePkg: String,
    val targetDir: Path,
    val screenPackage: String,
)

fun sanitizeScreenName(raw: String): String {
    val base = raw.trim().replace(Regex("[^A-Za-z0-9_]"), "")
    if (base.isEmpty()) return "Screen"
    return base.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}

fun inferPresentationPackage(modulePkgRepo: ModulePackageRepository, moduleDir: Path): String {
    val existing = modulePkgRepo.findModulePackage(moduleDir)
    return existing ?: "com.example.presentation"
}

fun deriveModuleName(pkg: String): String {
    val parts = pkg.split('.')
    val idx = parts.indexOf("presentation")
    if (idx > 0) {
        return parts.getOrNull(idx - 1)?.replaceFirstChar { it.uppercase() } ?: "App"
    }
    return parts.lastOrNull()?.replaceFirstChar { it.uppercase() } ?: "App"
}

fun resolveScreenDir(modulePkgRepo: ModulePackageRepository, params: PresentationParams): ScreenDirSetup {
    val sanitizedName = sanitizeScreenName(params.screenName)
    val basePkg = inferPresentationPackage(modulePkgRepo, params.moduleDir)
    val kotlinDir = params.moduleDir.resolve("src/main/kotlin").also { if (!it.exists()) it.createDirectories() }
    val pkgDir = kotlinDir.resolve(basePkg.replace('.', '/')).also { if (!it.exists()) it.createDirectories() }
    val folder = sanitizedName.replaceFirstChar { it.lowercase() }
    val targetDir = pkgDir.resolve(folder).also { if (!it.exists()) it.createDirectories() }
    val screenPackage = "$basePkg.$folder"
    return ScreenDirSetup(sanitizedName, basePkg, targetDir, screenPackage)
}
