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

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.BuildLogicScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class EnsureBuildLogicStep(
    private val buildLogicRepo: BuildLogicScaffoldRepository,
) : CleanArchitectureStep {

    override suspend fun execute(params: CleanArchitectureParams): StepResult = runCatching {
        val modules = resolveEnabledModules(params)
        val diMode = resolveDiMode(params)
        val updated = buildLogicRepo.ensureBuildLogic(params, modules, diMode)
        StepResult.BuildLogic(updated = updated, message = "- build-logic updated: $updated")
    }.getOrElse { StepResult.Failure(it) }

    private fun resolveEnabledModules(p: CleanArchitectureParams): List<String> = buildList {
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

    private fun resolveDiMode(p: CleanArchitectureParams): DiMode = when (val s = p.diStrategy) {
        DiStrategy.Hilt -> DiMode.HILT
        DiStrategy.Metro -> DiMode.METRO
        is DiStrategy.Koin -> if (s.useAnnotations) DiMode.KOIN_ANNOTATIONS else DiMode.KOIN
    }
}
