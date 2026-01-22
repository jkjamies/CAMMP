package com.jkjamies.cammp.feature.cleanarchitecture.datasource

import com.jkjamies.cammp.feature.cleanarchitecture.domain.datasource.ModulePackageMetadataDataSource
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

/**
 * Infers the base package for a module by inspecting Kotlin source files.
 *
 * Note: this is intentionally lightweight and filesystem-based (no IntelliJ PSI/VFS)
 * to keep cleanarchitecture generator usable from tests/CI.
 */
@ContributesBinding(AppScope::class)
class ModulePackageMetadataDataSourceImpl(
    private val fs: FileSystemRepository,
) : ModulePackageMetadataDataSource {

    override fun findModulePackage(moduleDir: Path): String? {
        // Prefer src/main/kotlin
        val kotlinRoot = moduleDir.resolve("src/main/kotlin")
        if (!fs.exists(kotlinRoot)) return null

        // Very small heuristic: look for the first "package ..." line in Placeholder.kt if present.
        val placeholder = kotlinRoot
            .resolve("Placeholder.kt")
        fs.readText(placeholder)?.let { text ->
            Regex("(?m)^\\s*package\\s+([A-Za-z0-9_.]+)\\s*$").find(text)?.groups?.get(1)?.value?.let { return it }
        }

        return null
    }
}
