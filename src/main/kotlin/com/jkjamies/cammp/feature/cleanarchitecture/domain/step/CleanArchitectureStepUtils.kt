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

package com.jkjamies.cammp.feature.cleanarchitecture.domain.step

import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode

fun resolveEnabledModules(p: CleanArchitectureParams): List<String> = buildList {
    add("domain")
    add("data")
    if (p.includeApiModule) add("api")
    if (p.includeDiModule) add("di")
    if (p.includePresentation) add("presentation")

    when (p.datasourceStrategy) {
        DatasourceStrategy.None -> Unit
        DatasourceStrategy.Combined -> add("dataSource")
        DatasourceStrategy.RemoteOnly -> add("remoteDataSource")
        DatasourceStrategy.LocalOnly -> add("localDataSource")
        DatasourceStrategy.RemoteAndLocal -> {
            add("remoteDataSource")
            add("localDataSource")
        }
    }
}

fun resolveDiMode(p: CleanArchitectureParams): DiMode = when (val s = p.diStrategy) {
    DiStrategy.Hilt -> DiMode.HILT
    DiStrategy.Metro -> DiMode.METRO
    is DiStrategy.Koin -> if (s.useAnnotations) DiMode.KOIN_ANNOTATIONS else DiMode.KOIN
}
