package com.jkjamies.cammp.feature.repositorygenerator.domain.repository

import java.nio.file.Path

interface ModulePackageRepository {
    /** Find the package name of the [moduleDir]. */
    fun findModulePackage(moduleDir: Path): String
}

