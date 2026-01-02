package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.step.StepResult
import com.jkjamies.cammp.feature.usecasegenerator.domain.step.UseCaseStep
import dev.zacsweers.metro.Inject
import java.nio.file.Path

@Inject
class UseCaseGenerator(
    private val steps: Set<UseCaseStep>
) {
    suspend operator fun invoke(params: UseCaseParams): Result<Path> {
        return runCatching {
            val className = if (params.className.endsWith("UseCase")) params.className else "${params.className}UseCase"
            val updatedParams = params.copy(className = className)

            var lastPath: Path? = null
            
            // Execute all steps
            for (step in steps) {
                when (val result = step.execute(updatedParams)) {
                    is StepResult.Success -> {
                        if (result.path != null) {
                            lastPath = result.path
                        }
                    }
                    is StepResult.Failure -> throw result.error
                }
            }
            
            lastPath ?: throw IllegalStateException("No path returned from generation steps")
        }
    }
}
