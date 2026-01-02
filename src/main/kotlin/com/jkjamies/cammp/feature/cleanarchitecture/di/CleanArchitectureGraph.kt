package com.jkjamies.cammp.feature.cleanarchitecture.di

import com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface CleanArchitectureGraph {
    val generateModulesViewModelFactory: GenerateModulesViewModel.Factory
}