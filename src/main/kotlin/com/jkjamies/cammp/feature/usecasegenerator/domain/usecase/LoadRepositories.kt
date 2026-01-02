package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository
import dev.zacsweers.metro.Inject

/**
 * Loads the available repository simple names from the specified domain module.
 *
 * @param repo The [RepositoryDiscoveryRepository] to use for loading repositories.
 */
@Inject
class LoadRepositories(
    private val repo: RepositoryDiscoveryRepository
) {
    /**
     * @param domainModulePath The absolute path to the domain module.
     * @return A list of repository simple names.
     */
    operator fun invoke(domainModulePath: String): List<String> = repo.loadRepositories(domainModulePath)
}
