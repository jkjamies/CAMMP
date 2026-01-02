package com.jkjamies.cammp.feature.usecasegenerator.domain.repository

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import java.nio.file.Path

interface UseCaseGenerationRepository {
    /** Generate the UseCase using the given [params] for the provided [packageName]. */
    fun generateUseCase(params: UseCaseParams, packageName: String, baseDomainPackage: String): Path
}
