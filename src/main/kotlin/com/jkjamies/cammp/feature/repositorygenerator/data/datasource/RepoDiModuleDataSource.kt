package com.jkjamies.cammp.feature.repositorygenerator.data.datasource

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding

interface RepoDiModuleDataSource {
    fun generateKoinModule(
        packageName: String,
        existingContent: String?,
        domainFqn: String,
        dataFqn: String,
        className: String
    ): String

    fun generateHiltModule(
        packageName: String,
        existingContent: String?,
        domainFqn: String,
        dataFqn: String,
        className: String
    ): String

    fun generateKoinDataSourceModule(
        packageName: String,
        existingContent: String?,
        bindings: List<DataSourceBinding>
    ): String

    fun generateHiltDataSourceModule(
        packageName: String,
        existingContent: String?,
        bindings: List<DataSourceBinding>
    ): String
}
