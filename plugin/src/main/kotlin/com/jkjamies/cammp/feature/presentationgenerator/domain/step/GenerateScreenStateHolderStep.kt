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

package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.domain.step.StepPhase
import com.jkjamies.cammp.domain.step.StepResult
import com.jkjamies.cammp.domain.step.runStepCatching
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class GenerateScreenStateHolderStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val screenStateHolderRepo: ScreenStateHolderRepository
) : PresentationStep {

    override val phase: StepPhase = StepPhase.GENERATE

    override suspend fun execute(params: PresentationParams): StepResult {
        if (!params.useScreenStateHolder) {
            return StepResult.Success(null)
        }

        return runStepCatching {
            val setup = resolveScreenDir(modulePkgRepo, params)

            val result = screenStateHolderRepo.generateScreenStateHolder(
                targetDir = setup.targetDir,
                packageName = setup.screenPackage,
                params = params
            )
            StepResult.Success("- ${result.fileName}: ${result.path} (${result.status})")
        }
    }
}
