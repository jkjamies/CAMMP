package com.jkjamies.cammp.feature.cleanarchitecture.domain.step

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.CleanArchitectureScaffoldRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

/**
 * Main scaffold step that creates module directories/build files/source skeletons.
 */
@ContributesIntoSet(AppScope::class)
class GenerateModulesStep(
    private val scaffoldRepo: CleanArchitectureScaffoldRepository,
) : CleanArchitectureStep {

    override suspend fun execute(params: CleanArchitectureParams): StepResult = runCatching {
        StepResult.Scaffold(scaffoldRepo.generateModules(params))
    }.getOrElse { StepResult.Failure(it) }
}
