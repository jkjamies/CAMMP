package com.jkjamies.cammp.feature.cleanarchitecture.domain.datasource

import java.nio.file.Path

/**
 * Data source for discovering the Kotlin package of a module.
 */
interface ModulePackageMetadataDataSource {
    fun findModulePackage(moduleDir: Path): String?
}

