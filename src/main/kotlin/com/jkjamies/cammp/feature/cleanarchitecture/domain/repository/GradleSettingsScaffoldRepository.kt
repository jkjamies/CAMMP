package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams

/**
 * Applies required Gradle settings changes for a clean architecture feature.
 */
interface GradleSettingsScaffoldRepository {

    /**
     * Ensure all required `include(...)`s and auxiliary Gradle settings are present.
     *
     * @return true if any file was modified.
     */
    fun ensureSettings(
        params: CleanArchitectureParams,
        enabledModules: List<String>,
        diMode: DiMode,
    ): Boolean
}
