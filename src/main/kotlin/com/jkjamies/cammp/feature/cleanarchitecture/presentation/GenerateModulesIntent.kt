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

sealed interface GenerateModulesIntent {
    data class SetRoot(val value: String) : GenerateModulesIntent
    data class SetFeature(val value: String) : GenerateModulesIntent
    data class SetOrgCenter(val value: String) : GenerateModulesIntent
    data class SetPlatformAndroid(val selected: Boolean) : GenerateModulesIntent
    data class SetPlatformKmp(val selected: Boolean) : GenerateModulesIntent
    data class SetIncludePresentation(val selected: Boolean) : GenerateModulesIntent
    data class SetIncludeApiModule(val selected: Boolean) : GenerateModulesIntent
    data class SetIncludeDatasource(val selected: Boolean) : GenerateModulesIntent
    data class SetDatasourceCombined(val selected: Boolean) : GenerateModulesIntent
    data class SetDatasourceRemote(val selected: Boolean) : GenerateModulesIntent
    data class SetDatasourceLocal(val selected: Boolean) : GenerateModulesIntent
    data class SelectDiMetro(val selected: Boolean) : GenerateModulesIntent
    data class SelectDiHilt(val selected: Boolean) : GenerateModulesIntent
    data class SelectDiKoin(val selected: Boolean) : GenerateModulesIntent
    data class SetKoinAnnotations(val selected: Boolean) : GenerateModulesIntent
    data class SetIncludeDiModule(val selected: Boolean) : GenerateModulesIntent
    object Generate : GenerateModulesIntent
}
