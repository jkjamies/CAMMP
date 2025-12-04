package com.github.jkjamies.cammp.feature.cleanarchitecture.domain.repository

import java.nio.file.Path

interface GradleSettingsRepository {
    /** Ensures include(":root:feature:module") lines exist. Returns true if file was modified. */
    fun ensureIncludes(projectBase: Path, root: String, feature: String, modules: List<String>): Boolean

    /** Ensures includeBuild("build-logic") exists. Returns true if file was modified. */
    fun ensureIncludeBuild(projectBase: Path, buildLogicName: String): Boolean

    /** Ensures version catalog plugin aliases for enabled modules exist. Returns true if modified. */
    fun ensureVersionCatalogPluginAliases(projectBase: Path, orgSegment: String, enabledModules: List<String>): Boolean
}
