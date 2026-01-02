package com.jkjamies.cammp.feature.repositorygenerator.di

import com.jkjamies.cammp.feature.repositorygenerator.domain.step.RepositoryStep
import com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Multibinds

@ContributesTo(AppScope::class)
interface RepositoryGraph {
    val repositoryViewModelFactory: RepositoryViewModel.Factory

    @Multibinds(allowEmpty = true)
    val repositorySteps: Set<RepositoryStep>
}
