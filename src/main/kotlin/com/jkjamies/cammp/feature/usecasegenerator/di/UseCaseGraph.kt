package com.jkjamies.cammp.feature.usecasegenerator.di

import com.jkjamies.cammp.feature.usecasegenerator.domain.step.UseCaseStep
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface UseCaseGraph {
    val useCaseViewModelFactory: UseCaseViewModel.Factory

    @Multibinds(allowEmpty = true)
    val useCaseSteps: Set<UseCaseStep>
}