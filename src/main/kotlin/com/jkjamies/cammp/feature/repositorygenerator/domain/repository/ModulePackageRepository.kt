package com.jkjamies.cammp.feature.repositorygenerator.domain.repository

import java.nio.file.Path

interface ModulePackageRepository {
    fun findModulePackage(moduleDir: Path): String
}

