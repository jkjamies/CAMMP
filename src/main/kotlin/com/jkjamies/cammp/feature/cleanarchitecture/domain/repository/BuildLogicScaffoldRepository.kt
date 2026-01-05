package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams

/**
 * Scaffolds the `build-logic/` project used by a clean-architecture feature setup.
 */
interface BuildLogicScaffoldRepository {

    /**
     * Ensures the `build-logic` project exists and contains the required convention plugin sources.
     *
     * @return true if any file or directory was created/updated.
     */
    fun ensureBuildLogic(
        params: CleanArchitectureParams,
        enabledModules: List<String>,
        diMode: DiMode,
    ): Boolean
}
