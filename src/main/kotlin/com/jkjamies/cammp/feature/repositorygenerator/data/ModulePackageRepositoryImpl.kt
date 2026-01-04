package com.jkjamies.cammp.feature.repositorygenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.repositorygenerator.data.datasource.PackageMetadataDataSource
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

@ContributesBinding(AppScope::class)
@Inject
class ModulePackageRepositoryImpl(
    private val dataSource: PackageMetadataDataSource
) : ModulePackageRepository {
    override fun findModulePackage(moduleDir: Path): String {
        return dataSource.findModulePackage(moduleDir)
            ?: error("Could not determine module package for: $moduleDir")
    }
}
