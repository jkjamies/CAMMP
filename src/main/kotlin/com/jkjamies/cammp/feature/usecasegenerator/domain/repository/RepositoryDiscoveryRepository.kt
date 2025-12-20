package com.jkjamies.cammp.feature.usecasegenerator.domain.repository

interface RepositoryDiscoveryRepository {
    /** Returns a sorted list of repository simple names available in the same feature space. */
    fun loadRepositories(domainModulePath: String): List<String>
}
