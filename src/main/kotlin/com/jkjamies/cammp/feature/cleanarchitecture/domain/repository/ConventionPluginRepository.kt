package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

import java.nio.file.Path

/**
 * A repository for generating Gradle convention plugin files.
 */
interface ConventionPluginRepository {
    /**
     * Generates a convention plugin file based on the specified parameters.
     *
     * @param outputDirectory The directory where the plugin file will be created.
     * @param packageName The package name for the generated plugin class.
     * @param diMode The dependency injection framework to configure.
     * @param type The type of convention plugin to generate (e.g., Data, Presentation).
     */
    fun generate(
        outputDirectory: Path,
        packageName: String,
        diMode: DiMode,
        type: PluginType
    )
}

/**
 * Specifies the dependency injection framework to be used.
 */
enum class DiMode {
    HILT, KOIN, KOIN_ANNOTATIONS, METRO
}

/**
 * Specifies the type of convention plugin to generate.
 */
enum class PluginType {
    DATA,
    DI,
    DOMAIN,
    PRESENTATION,
    DATA_SOURCE,
    REMOTE_DATA_SOURCE,
    LOCAL_DATA_SOURCE
}
