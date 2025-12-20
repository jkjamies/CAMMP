package com.jkjamies.cammp.feature.usecasegenerator.domain.repository

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import java.nio.file.Path

interface UseCaseGenerationRepository {
    fun generateUseCase(params: UseCaseParams, packageName: String): Path
}
