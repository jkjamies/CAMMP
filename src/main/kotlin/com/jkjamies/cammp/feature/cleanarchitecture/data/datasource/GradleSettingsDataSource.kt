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

package com.jkjamies.cammp.feature.cleanarchitecture.data.datasource

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import java.nio.file.Path

/**
 * Data source responsible for reading and updating Gradle settings/build files.
 *
 * This is kept as a datasource (not a repository) so repositories don't depend on repositories.
 */
interface GradleSettingsDataSource {
    fun ensureIncludes(projectBase: Path, root: String, feature: String, modules: List<String>): Boolean
    fun ensureIncludeBuild(projectBase: Path, buildLogicName: String): Boolean
    fun ensureVersionCatalogPluginAliases(projectBase: Path, orgSegment: String, enabledModules: List<String>): Boolean
    fun ensureAppDependency(projectBase: Path, root: String, feature: String, diMode: DiMode): Boolean
}

