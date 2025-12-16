package com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository

import com.github.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import java.nio.file.Path

interface RepositoryGenerationRepository {
    fun generateDomainLayer(params: RepositoryParams, packageName: String, domainDir: Path): Path
    fun generateDataLayer(params: RepositoryParams, dataPackage: String, domainPackage: String): Path
}
