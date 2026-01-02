package com.jkjamies.cammp.feature.presentationgenerator.di

import com.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppScope::class)
interface PresentationGraph {
    val presentationViewModelFactory: PresentationViewModel.Factory
}