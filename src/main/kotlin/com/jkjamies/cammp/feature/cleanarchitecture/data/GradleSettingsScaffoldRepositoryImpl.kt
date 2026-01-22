package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.data.datasource.GradleSettingsDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
class GradleSettingsScaffoldRepositoryImpl(
    private val dataSource: GradleSettingsDataSource,
) : GradleSettingsScaffoldRepository {

    override fun ensureSettings(params: CleanArchitectureParams, enabledModules: List<String>, diMode: DiMode): Boolean {
        val includesChanged = dataSource.ensureIncludes(params.projectBasePath, params.root, params.feature, enabledModules)
        val includeBuildChanged = dataSource.ensureIncludeBuild(params.projectBasePath, "build-logic")
        val aliasesChanged = dataSource.ensureVersionCatalogPluginAliases(params.projectBasePath, params.orgCenter, enabledModules)
        val appDepChanged = dataSource.ensureAppDependency(params.projectBasePath, params.root, params.feature, diMode)
        return includesChanged || includeBuildChanged || aliasesChanged || appDepChanged
    }
}
