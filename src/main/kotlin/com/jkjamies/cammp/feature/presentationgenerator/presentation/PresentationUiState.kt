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

package com.jkjamies.cammp.feature.presentationgenerator.presentation

import androidx.compose.runtime.Immutable

@Immutable
data class PresentationUiState(
    val directory: String = "",
    val screenName: String = "",
    val pkg: String = "",

    val useFlowStateHolder: Boolean = false,
    val useScreenStateHolder: Boolean = false,
    val includeNavigation: Boolean = false,

    val patternMVI: Boolean = true,
    val patternMVVM: Boolean = false,
    val patternCircuit: Boolean = false,
    val diMetro: Boolean = true,
    val diHilt: Boolean = false,
    val diKoin: Boolean = false,
    val diKoinAnnotations: Boolean = false,

    val useCasesByModule: Map<String, List<String>> = emptyMap(),
    val selectedUseCases: Set<String> = emptySet(),
    val isGenerating: Boolean = false,
    val lastCreated: List<String> = emptyList(),
    val lastSkipped: List<String> = emptyList(),
    val lastMessage: String? = null,
    val errorMessage: String? = null,
)
