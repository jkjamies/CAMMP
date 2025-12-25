package com.jkjamies.cammp.feature.repositorygenerator.domain.repository

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import java.nio.file.Path

interface RepositoryGenerationRepository {
    /** Generate the domain layer from the given [params], [packageName], and [domainDir]. */
    fun generateDomainLayer(params: RepositoryParams, packageName: String, domainDir: Path): Path

    /** Generate the data layer from the given [params], [dataPackage], and [domainPackage]. */
    fun generateDataLayer(params: RepositoryParams, dataPackage: String, domainPackage: String): Path
}
