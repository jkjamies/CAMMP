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
    suspend operator fun invoke(params: UseCaseParams): Result<String> {
        return runCatching {
            val className = if (params.className.endsWith("UseCase")) params.className else "${params.className}UseCase"
            val updatedParams = params.copy(className = className)

            val messages = mutableListOf<String>()
            
            // Execute all steps
            for (step in steps) {
                when (val result = step.execute(updatedParams)) {
                    is StepResult.Success -> {
                        result.message?.let { messages.add(it) }
                    }
                    is StepResult.Failure -> throw result.error
                }
            }
            
            if (messages.isEmpty()) throw IllegalStateException("No output from generation steps")
            messages.joinToString("\n\n")
        }
    }
}
