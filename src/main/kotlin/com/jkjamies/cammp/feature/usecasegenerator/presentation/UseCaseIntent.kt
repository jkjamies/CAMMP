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

package com.jkjamies.cammp.feature.usecasegenerator.presentation

sealed interface UseCaseIntent {
    data class SetName(val value: String) : UseCaseIntent
    data class SetDomainPackage(val value: String) : UseCaseIntent
    data class SetAsync(val selected: Boolean) : UseCaseIntent
    data class SetSync(val selected: Boolean) : UseCaseIntent
    data class ToggleRepository(val repositoryName: String, val selected: Boolean) : UseCaseIntent
    data class SetDiMetro(val selected: Boolean) : UseCaseIntent
    data class SetDiHilt(val selected: Boolean) : UseCaseIntent
    data class SetDiKoin(val selected: Boolean) : UseCaseIntent
    data class ToggleKoinAnnotations(val selected: Boolean) : UseCaseIntent
    data object Generate : UseCaseIntent
}
