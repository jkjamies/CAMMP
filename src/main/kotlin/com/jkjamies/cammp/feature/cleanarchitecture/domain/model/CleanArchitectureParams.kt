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

package com.jkjamies.cammp.feature.cleanarchitecture.domain.model

import java.nio.file.Path

sealed interface DiStrategy {
    data object Hilt : DiStrategy
    data object Metro : DiStrategy
    data class Koin(val useAnnotations: Boolean) : DiStrategy
}

sealed interface DatasourceStrategy {
    data object None : DatasourceStrategy
    data object Combined : DatasourceStrategy
    data object LocalOnly : DatasourceStrategy
    data object RemoteOnly : DatasourceStrategy
    data object RemoteAndLocal : DatasourceStrategy
}

data class CleanArchitectureParams(
    val projectBasePath: Path,
    val root: String,
    val feature: String,
    val orgCenter: String,
    val includePresentation: Boolean,
    val includeApiModule: Boolean = false,
    val includeDiModule: Boolean = true,
    val datasourceStrategy: DatasourceStrategy = DatasourceStrategy.None,
    val diStrategy: DiStrategy = DiStrategy.Hilt,
)
