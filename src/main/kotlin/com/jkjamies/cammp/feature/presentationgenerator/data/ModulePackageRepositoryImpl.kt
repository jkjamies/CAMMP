package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.data.datasource.PackageMetadataDataSource
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

/**
 * Implementation of [ModulePackageRepository] that uses IntelliJ's Virtual File System (VFS)
 * to find the most appropriate package name within a module.
 */
@ContributesBinding(AppScope::class)
class ModulePackageRepositoryImpl(
    private val dataSource: PackageMetadataDataSource
) : ModulePackageRepository {
    override fun findModulePackage(moduleDir: Path): String? {
        return dataSource.findModulePackage(moduleDir)
    }
}
