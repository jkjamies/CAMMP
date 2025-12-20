package com.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.jkjamies.cammp.feature.repositorygenerator.data.DataSourceDiscoveryRepositoryImpl
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceDiscoveryRepository

/**
 * Loads available data sources from the specified data module, grouped by type.
 *
 * @param repo The [DataSourceDiscoveryRepository] to use for loading data sources.
 */
class LoadDataSourcesByType(
    private val repo: DataSourceDiscoveryRepository = DataSourceDiscoveryRepositoryImpl()
) {
    /**
     * @param dataModulePath The absolute path to the data module.
     * @return A map of data source types to a list of their fully qualified names.
     */
    operator fun invoke(dataModulePath: String): Map<String, List<String>> =
        repo.loadDataSourcesByType(dataModulePath)
}
