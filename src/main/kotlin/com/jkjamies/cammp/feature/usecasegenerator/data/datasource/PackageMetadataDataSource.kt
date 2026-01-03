package com.jkjamies.cammp.feature.usecasegenerator.data.datasource

import java.nio.file.Path

interface PackageMetadataDataSource {
    fun findModulePackage(moduleDir: Path): String?
}