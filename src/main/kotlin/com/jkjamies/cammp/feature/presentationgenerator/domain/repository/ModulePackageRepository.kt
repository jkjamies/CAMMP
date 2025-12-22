package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import java.nio.file.Path

/**
 * Repository for inferring the package name of a module.
 */
interface ModulePackageRepository {
    /**
     * Attempts to detect the base package of the module under [moduleDir].
     *
     * Returns a package that points to the presentation layer when possible
     * (…​.presentation), otherwise best effort based on discovered Kotlin files.
     *
     * @param moduleDir The root directory of the module.
     * @return The inferred package name, or null if it could not be determined.
     */
    fun findModulePackage(moduleDir: Path): String?
}
