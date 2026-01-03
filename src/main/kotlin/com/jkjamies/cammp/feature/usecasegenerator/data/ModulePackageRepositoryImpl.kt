package com.jkjamies.cammp.feature.usecasegenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.PackageMetadataDataSource
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

@ContributesBinding(AppScope::class)
@Inject
class ModulePackageRepositoryImpl(
    private val dataSource: PackageMetadataDataSource
) : ModulePackageRepository {
    override fun findModulePackage(moduleDir: Path): String? {
        return dataSource.findModulePackage(moduleDir)
    }
}
