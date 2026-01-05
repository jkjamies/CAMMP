package com.jkjamies.cammp.feature.presentationgenerator.data.datasource

import java.nio.file.Path

/**
 * Reads package metadata from an existing module (for example by inspecting source folders).
 */
interface PackageMetadataDataSource {
    fun findModulePackage(moduleDir: Path): String?
}