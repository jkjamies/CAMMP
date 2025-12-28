package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

import java.nio.file.Path

/**
 * A repository for generating the Aliases.kt file.
 */
interface AliasesRepository {
    /**
     * Generates the Aliases.kt file using KotlinPoet.
     * @param outputDirectory The directory to generate the file in.
     * @param packageName The package name for the file.
     * @param diMode The dependency injection mode selected.
     * @param tomlPath The path to the libs.versions.toml file.
     */
    fun generateAliases(outputDirectory: Path, packageName: String, diMode: DiMode, tomlPath: Path)
}
