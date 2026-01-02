package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import java.nio.file.Path

sealed interface StepResult {
    data class Success(val message: String? = null) : StepResult
    data class Failure(val error: Throwable) : StepResult
}

interface RepositoryStep {
    suspend fun execute(params: RepositoryParams): StepResult
}
