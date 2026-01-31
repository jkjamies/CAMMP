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

package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.data.datasource.GradleSettingsDataSource
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
class GradleSettingsScaffoldRepositoryImpl(
    private val dataSource: GradleSettingsDataSource,
) : GradleSettingsScaffoldRepository {

    override fun ensureSettings(params: CleanArchitectureParams, enabledModules: List<String>, diMode: DiMode): Boolean {
        val includesChanged = dataSource.ensureIncludes(params.projectBasePath, params.root, params.feature, enabledModules)
        val includeBuildChanged = dataSource.ensureIncludeBuild(params.projectBasePath, "build-logic")
        val aliasesChanged = dataSource.ensureVersionCatalogPluginAliases(params.projectBasePath, params.orgCenter, enabledModules)
        val appDepChanged = dataSource.ensureAppDependency(params.projectBasePath, params.root, params.feature, diMode)
        return includesChanged || includeBuildChanged || aliasesChanged || appDepChanged
    }
}
