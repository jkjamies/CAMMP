package com.jkjamies.cammp.feature.presentationgenerator.domain.repository

import java.nio.file.Path

interface ModulePackageRepository {
    /**
     * Attempts to detect the base package of the module under [moduleDir].
     * Returns a package that points to the presentation layer when possible
     * (…​.presentation), otherwise best effort based on discovered Kotlin files.
     */
    fun findModulePackage(moduleDir: Path): String?
}
