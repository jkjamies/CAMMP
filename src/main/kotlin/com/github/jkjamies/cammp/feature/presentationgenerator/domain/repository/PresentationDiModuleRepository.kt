package com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository

import java.nio.file.Path

data class PresentationMergeOutcome(val outPath: Path, val status: String)

interface PresentationDiModuleRepository {
    /** Create or merge the ViewModel DI module (Koin only) and write it. */
    fun mergeViewModelModule(
        diDir: Path,
        diPackage: String,
        viewModelSimpleName: String,
        viewModelFqn: String,
        dependencyCount: Int,
        useKoin: Boolean,
    ): PresentationMergeOutcome
}
