package com.jkjamies.cammp.feature.repositorygenerator.domain.repository

import java.nio.file.Path

interface DatasourceScaffoldRepository {
    /** Generates the DataSource interface in the data module. */
    fun generateInterface(
        directory: Path,
        packageName: String,
        className: String
    ): Path

    /**
     * Generates the DataSource implementation in the target module.
     */
    fun generateImplementation(
        directory: Path,
        packageName: String,
        className: String,
        interfacePackage: String,
        interfaceName: String,
        useKoin: Boolean
    ): Path
}
