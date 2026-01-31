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

package com.jkjamies.cammp.feature.cleanarchitecture.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class GenerateModulesUiState(
    val projectBasePath: String? = null,
    val root: String = "",
    val feature: String = "",
    val orgCenter: String = "cammp",

    val platformAndroid: Boolean = true,
    val platformKmp: Boolean = false,

    val includePresentation: Boolean = true,
    val includeApiModule: Boolean = false,
    val includeDiModule: Boolean = true,

    val includeDatasource: Boolean = false,
    val datasourceCombined: Boolean = false,
    val datasourceRemote: Boolean = true,
    val datasourceLocal: Boolean = true,
    val diMetro: Boolean = true,
    val diHilt: Boolean = false,
    val diKoin: Boolean = false,
    val diKoinAnnotations: Boolean = false,

    val orgRightPreview: String = "",
    val isGenerating: Boolean = false,
    val lastCreated: List<String> = emptyList(),
    val lastSkipped: List<String> = emptyList(),
    val settingsUpdated: Boolean? = null,
    val buildLogicCreated: Boolean? = null,
    val lastMessage: String? = null,
    val errorMessage: String? = null,
)
