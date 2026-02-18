/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
