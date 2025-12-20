package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult

interface PresentationRepository {
    fun generate(params: PresentationParams): PresentationResult
}
