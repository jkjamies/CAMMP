package com.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.step.RepositoryStep
import com.jkjamies.cammp.feature.repositorygenerator.domain.step.StepResult
import dev.zacsweers.metro.Inject

@Inject
class RepositoryGenerator(
    private val steps: Set<RepositoryStep>
) {
    suspend operator fun invoke(params: RepositoryParams): Result<String> = runCatching {
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

        val title = "Repository generation completed:"
        (sequenceOf(title) + results.asSequence()).joinToString("\n")
    }
}
