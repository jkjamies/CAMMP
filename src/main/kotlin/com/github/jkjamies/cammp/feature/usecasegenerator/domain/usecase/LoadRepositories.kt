package com.github.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.github.jkjamies.cammp.feature.usecasegenerator.data.RepositoryDiscoveryRepositoryImpl
import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository

/**
 * Loads the available repository simple names from the specified domain module.
 *
 * @param repo The [RepositoryDiscoveryRepository] to use for loading repositories.
 */
class LoadRepositories(
    private val repo: RepositoryDiscoveryRepository = RepositoryDiscoveryRepositoryImpl()
) {
    /**
     * @param domainModulePath The absolute path to the domain module.
     * @return A list of repository simple names.
     */
    operator fun invoke(domainModulePath: String): List<String> = repo.loadRepositories(domainModulePath)
}
