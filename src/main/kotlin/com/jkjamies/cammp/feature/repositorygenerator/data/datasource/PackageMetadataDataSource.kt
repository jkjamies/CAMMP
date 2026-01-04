package com.jkjamies.cammp.feature.repositorygenerator.data.datasource

import java.nio.file.Path

interface PackageMetadataDataSource {
    fun findModulePackage(moduleDir: Path): String?
}