package com.jkjamies.cammp.feature.presentationgenerator.domain.usecase

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.step.PresentationStep
import com.jkjamies.cammp.feature.presentationgenerator.domain.step.StepResult
import dev.zacsweers.metro.Inject

@Inject
class PresentationGenerator(
    private val steps: Set<PresentationStep>
) {
    suspend operator fun invoke(params: PresentationParams): Result<String> = runCatching {
        val results = mutableListOf<String>()
        
        for (step in steps) {
            when (val result = step.execute(params)) {
                is StepResult.Success -> {
                    if (result.message != null) {
                        results.add(result.message)
                    }
                }
                is StepResult.Failure -> throw result.error
            }
        }

        val title = "Presentation generation completed:"
        (sequenceOf(title) + results.asSequence()).joinToString("\n")
    }
}
