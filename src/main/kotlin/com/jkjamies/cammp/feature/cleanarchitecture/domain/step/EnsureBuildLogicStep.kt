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

import com.jkjamies.cammp.domain.step.StepPhase
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.BuildLogicScaffoldRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class EnsureBuildLogicStep(
    private val buildLogicRepo: BuildLogicScaffoldRepository,
) : CleanArchitectureStep {

    override val phase: StepPhase = StepPhase.CONFIGURE

    override suspend fun execute(params: CleanArchitectureParams): StepResult = runCleanArchStepCatching {
        val modules = resolveEnabledModules(params)
        val diMode = resolveDiMode(params)
        val updated = buildLogicRepo.ensureBuildLogic(params, modules, diMode)
        StepResult.BuildLogic(updated = updated, message = "- build-logic updated: $updated")
    }
}
