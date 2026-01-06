package com.jkjamies.cammp.feature.cleanarchitecture.domain.step

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult

sealed interface StepResult {
    data class Success(val message: String = "") : StepResult
    data class Settings(val updated: Boolean, val message: String = "") : StepResult
    data class BuildLogic(val updated: Boolean, val message: String = "") : StepResult
    data class Scaffold(val result: CleanArchitectureResult) : StepResult
    data class Failure(val error: Throwable) : StepResult
}

interface CleanArchitectureStep {
    suspend fun execute(params: CleanArchitectureParams): StepResult
}
