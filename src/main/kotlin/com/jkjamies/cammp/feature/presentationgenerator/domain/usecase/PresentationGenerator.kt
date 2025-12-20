package com.jkjamies.cammp.feature.presentationgenerator.domain.usecase

import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult

/**
 * Generates presentation layer files for a screen.
 *
 * @param repository The [PresentationRepository] to use for generation.
 */
class PresentationGenerator(
    private val repository: PresentationRepository,
) {
    /**
     * @param p The [PresentationParams] for generating the screen.
     * @return A [Result] containing the [PresentationResult], or an exception.
     */
    operator fun invoke(p: PresentationParams): Result<PresentationResult> = runCatching {
        require(p.screenName.isNotBlank()) { "Screen name is required" }
        repository.generate(p)
    }
}
