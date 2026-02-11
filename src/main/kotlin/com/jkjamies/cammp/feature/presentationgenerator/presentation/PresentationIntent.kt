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

sealed interface PresentationIntent {
    data class SetDirectory(val value: String) : PresentationIntent
    data class SetScreenName(val value: String) : PresentationIntent
    data class SetPackage(val value: String) : PresentationIntent

    data class ToggleFlowStateHolder(val selected: Boolean) : PresentationIntent
    data class ToggleScreenStateHolder(val selected: Boolean) : PresentationIntent
    data class ToggleIncludeNavigation(val selected: Boolean) : PresentationIntent

    data class SetPatternMVI(val selected: Boolean) : PresentationIntent
    data class SetPatternMVVM(val selected: Boolean) : PresentationIntent
    data class SetPatternCircuit(val selected: Boolean) : PresentationIntent

    data class SetDiMetro(val selected: Boolean) : PresentationIntent
    data class SetDiHilt(val selected: Boolean) : PresentationIntent
    data class SetDiKoin(val selected: Boolean) : PresentationIntent
    data class ToggleKoinAnnotations(val selected: Boolean) : PresentationIntent

    data class SetUseCasesByModule(val value: Map<String, List<String>>) : PresentationIntent
    data class ToggleUseCaseSelection(val fqn: String, val selected: Boolean) : PresentationIntent
    data object Generate : PresentationIntent
}
