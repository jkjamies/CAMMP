package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import java.nio.file.Path

sealed interface StepResult {
    data class Success(val path: Path? = null, val message: String? = null) : StepResult
    data class Failure(val error: Throwable) : StepResult
}

interface UseCaseStep {
    suspend fun execute(params: UseCaseParams): StepResult
}
