package com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.github.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.github.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult

interface PresentationRepository {
    fun generate(params: PresentationParams): PresentationResult
}
