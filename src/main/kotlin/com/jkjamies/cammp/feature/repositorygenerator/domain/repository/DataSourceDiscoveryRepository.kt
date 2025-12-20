package com.jkjamies.cammp.feature.repositorygenerator.domain.repository

interface DataSourceDiscoveryRepository {
    /**
     * Discover datasource interfaces within the given data module path and group them by type.
     * Keys: "Combined", "Remote", "Local". Values: list of FQNs.
     */
    fun loadDataSourcesByType(dataModulePath: String): Map<String, List<String>>
}
