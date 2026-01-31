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

package com.jkjamies.cammp.feature.presentationgenerator.domain.model

import java.nio.file.Path

sealed interface DiStrategy {
    data object Metro : DiStrategy
    data object Hilt : DiStrategy
    data class Koin(val useAnnotations: Boolean) : DiStrategy
}

sealed interface PresentationPatternStrategy {
    data object MVI : PresentationPatternStrategy
    data object MVVM : PresentationPatternStrategy
    data object Circuit : PresentationPatternStrategy
}

data class PresentationParams(
    val moduleDir: Path,
    val screenName: String,
    val patternStrategy: PresentationPatternStrategy,
    val diStrategy: DiStrategy,
    val includeNavigation: Boolean = false,
    val useFlowStateHolder: Boolean = false,
    val useScreenStateHolder: Boolean = false,
    val selectedUseCases: List<String> = emptyList(),
)
