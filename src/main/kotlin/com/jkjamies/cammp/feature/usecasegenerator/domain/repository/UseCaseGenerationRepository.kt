package com.jkjamies.cammp.feature.usecasegenerator.domain.repository

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import java.nio.file.Path

data class UseCaseGenerationResult(val useCasePath: Path, val interfacePath: Path? = null)

interface UseCaseGenerationRepository {
    /** Generate the UseCase using the given [params] for the provided [packageName]. */
    fun generateUseCase(
        params: UseCaseParams,
        packageName: String,
        baseDomainPackage: String,
        apiDir: Path? = null
    ): UseCaseGenerationResult
}
