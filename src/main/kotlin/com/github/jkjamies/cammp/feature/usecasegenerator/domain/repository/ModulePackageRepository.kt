package com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository

import java.nio.file.Path

interface ModulePackageRepository {
    /**
     * Returns the package name found under the module's src/main/kotlin tree, or null if none.
     */
    fun findModulePackage(moduleDir: Path): String?
}

