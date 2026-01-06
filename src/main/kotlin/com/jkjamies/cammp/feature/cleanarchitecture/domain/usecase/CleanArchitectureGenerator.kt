package com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.feature.cleanarchitecture.domain.step.CleanArchitectureStep
import com.jkjamies.cammp.feature.cleanarchitecture.domain.step.StepResult
import dev.zacsweers.metro.Inject

/**
 * Generates a Clean Architecture module structure for a feature.
 */
@Inject
class CleanArchitectureGenerator(
    private val steps: Set<CleanArchitectureStep>,
) {
    /**
     * @param p The [CleanArchitectureParams] for generating the modules.
     * @return A [Result] containing the [CleanArchitectureResult], or an exception.
     */
    suspend operator fun invoke(p: CleanArchitectureParams): Result<CleanArchitectureResult> = runCatching {
        val orderedSteps = steps.sortedBy { it::class.qualifiedName ?: it::class.simpleName ?: "" }

        val messages = mutableListOf<String>()
        var scaffoldResult: CleanArchitectureResult? = null
        var settingsUpdated = false
        var buildLogicUpdated = false

        for (step in orderedSteps) {
            when (val r = step.execute(p)) {
                is StepResult.Scaffold -> scaffoldResult = r.result
                is StepResult.Settings -> {
                    settingsUpdated = settingsUpdated || r.updated
                    if (r.message.isNotBlank()) messages.add(r.message)
                }
                is StepResult.BuildLogic -> {
                    buildLogicUpdated = buildLogicUpdated || r.updated
                    if (r.message.isNotBlank()) messages.add(r.message)
                }
                is StepResult.Success -> if (r.message.isNotBlank()) messages.add(r.message)
                is StepResult.Failure -> throw r.error
            }
        }

        val result = requireNotNull(scaffoldResult) {
            "No GenerateModulesStep found or it did not produce a result. Ensure it is contributed into the CleanArchitecture steps set."
        }

        val message = if (result.message.isNotBlank()) result.message else messages.joinToString("\n")

        result.copy(
            settingsUpdated = settingsUpdated,
            buildLogicCreated = buildLogicUpdated,
            message = message,
        )
    }
}
