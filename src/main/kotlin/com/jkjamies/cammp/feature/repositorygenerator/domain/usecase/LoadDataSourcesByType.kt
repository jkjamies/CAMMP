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

package com.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceDiscoveryRepository
import dev.zacsweers.metro.Inject

/**
 * Loads available data sources from the specified data module, grouped by type.
 *
 * @param repo The [DataSourceDiscoveryRepository] to use for loading data sources.
 */
@Inject
class LoadDataSourcesByType(
    private val repo: DataSourceDiscoveryRepository
) {
    /**
     * @param dataModulePath The absolute path to the data module.
     * @return A map of data source types to a list of their fully qualified names.
     */
    operator fun invoke(dataModulePath: String): Map<String, List<String>> =
        repo.loadDataSourcesByType(dataModulePath)
}
