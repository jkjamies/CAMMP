package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams

sealed interface StepResult {
    data class Success(val message: String? = null) : StepResult
    data class Failure(val error: Throwable) : StepResult
}

interface PresentationStep {
    suspend fun execute(params: PresentationParams): StepResult
}
