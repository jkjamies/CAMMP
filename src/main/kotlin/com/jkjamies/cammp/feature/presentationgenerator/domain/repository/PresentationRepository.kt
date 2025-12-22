package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult

/**
 * Repository responsible for generating the presentation layer.
 */
interface PresentationRepository {
    /**
     * Generates the presentation layer files based on the provided parameters.
     *
     * @param params The configuration parameters for generation.
     * @return The result of the generation process.
     */
    fun generate(params: PresentationParams): PresentationResult
}
