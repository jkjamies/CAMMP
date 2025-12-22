package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import java.nio.file.Path

/**
 * Result of merging a ViewModel into a DI module.
 *
 * @property outPath The path to the module file.
 * @property status The status of the operation ("created", "updated", "exists").
 */
data class PresentationMergeOutcome(val outPath: Path, val status: String)

/**
 * Repository for managing DI modules for the presentation layer.
 */
interface PresentationDiModuleRepository {
    /**
     * Create or merge the ViewModel DI module (Koin only) and write it.
     *
     * @param diDir The root directory of the DI module.
     * @param diPackage The package name of the DI module.
     * @param viewModelSimpleName The simple name of the ViewModel.
     * @param viewModelFqn The fully qualified name of the ViewModel.
     * @param dependencyCount The number of dependencies.
     * @return The outcome of the merge operation.
     */
    fun mergeViewModelModule(
        diDir: Path,
        diPackage: String,
        viewModelSimpleName: String,
        viewModelFqn: String,
        dependencyCount: Int,
    ): PresentationMergeOutcome
}
