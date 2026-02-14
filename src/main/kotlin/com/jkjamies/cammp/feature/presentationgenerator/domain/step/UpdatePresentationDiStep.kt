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

import com.jkjamies.cammp.domain.codegen.PackageSuffixes
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.domain.step.StepPhase
import com.jkjamies.cammp.domain.step.StepResult
import com.jkjamies.cammp.domain.step.runStepCatching
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlin.io.path.exists

@ContributesIntoSet(AppScope::class)
class UpdatePresentationDiStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val diRepo: PresentationDiModuleRepository
) : PresentationStep {

    override val phase: StepPhase = StepPhase.DI

    override suspend fun execute(params: PresentationParams): StepResult {
        val diStrategy = params.diStrategy
        if (diStrategy !is DiStrategy.Koin || diStrategy.useAnnotations) {
            return StepResult.Success(null)
        }

        return runStepCatching {
            val diDir = params.moduleDir.parent?.resolve("di")
            if (diDir == null || !diDir.exists()) {
                return StepResult.Success(null)
            }

            val pkg = inferPresentationPackage(modulePkgRepo, params.moduleDir)
            val diPackage = if (pkg.endsWith(PackageSuffixes.PRESENTATION)) {
                pkg.replace(PackageSuffixes.PRESENTATION, PackageSuffixes.DI)
            } else {
                val found = modulePkgRepo.findModulePackage(diDir)
                found ?: "$pkg.di"
            }

            if (diPackage.isBlank()) {
                return StepResult.Success(null)
            }

            val sanitizedName = sanitizeScreenName(params.screenName)
            val folder = sanitizedName.replaceFirstChar { it.lowercase() }
            val screenPackage = "$pkg.$folder"
            val viewModelSimpleName = "${sanitizedName}ViewModel"
            val viewModelFqn = "$screenPackage.${sanitizedName}ViewModel"

            val useCaseFqns = params.selectedUseCases.distinct().sorted()

            val mergeOutcome = diRepo.mergeViewModelModule(
                diDir = diDir,
                diPackage = diPackage,
                viewModelSimpleName = viewModelSimpleName,
                viewModelFqn = viewModelFqn,
                dependencyCount = useCaseFqns.size,
            )
            StepResult.Success("- DI ViewModel: ${mergeOutcome.outPath} (${mergeOutcome.status})")
        }
    }
}
