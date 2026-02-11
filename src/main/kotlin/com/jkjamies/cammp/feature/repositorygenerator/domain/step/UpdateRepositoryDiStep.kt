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

package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.domain.codegen.PackageSuffixes
import com.jkjamies.cammp.domain.step.StepPhase
import com.jkjamies.cammp.domain.step.StepResult
import com.jkjamies.cammp.domain.step.runStepCatching
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class UpdateRepositoryDiStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val diRepo: DiModuleRepository
) : RepositoryStep {

    override val phase: StepPhase = StepPhase.DI

    override suspend fun execute(params: RepositoryParams): StepResult {
        // Skip logic: If no DI strategy is chosen, this does nothing.
        if (params.diStrategy !is DiStrategy.Hilt && params.diStrategy !is DiStrategy.Metro && params.diStrategy !is DiStrategy.Koin) {
            return StepResult.Success(null)
        }

        return runStepCatching {
            val diDir = params.dataDir.parent?.resolve("di")
                ?: return StepResult.Success("- DI: Skipped (no di module found)")

            val diPackageRaw = modulePkgRepo.findModulePackage(diDir)
            val diPackage = diPackageRaw.removeSuffix(PackageSuffixes.REPOSITORY).removeSuffix(PackageSuffixes.USE_CASE)

            val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
            if (!diTargetDir.toFile().exists()) {
                diTargetDir.toFile().mkdirs()
            }

            val dataBase = modulePkgRepo.findModulePackage(params.dataDir)

            val domainDir = params.dataDir.parent?.resolve("domain")
                ?: error("Could not locate sibling domain module for ${params.dataDir}")
            val domainBase = modulePkgRepo.findModulePackage(domainDir)

            val domainFqn = "$domainBase.repository"
            val dataFqn = "$dataBase.repository"

            val outcome = diRepo.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = params.className,
                domainFqn = domainFqn,
                dataFqn = dataFqn,
                useKoin = params.diStrategy is DiStrategy.Koin
            )

            StepResult.Success("- DI: ${outcome.outPath} (${outcome.status})")
        }
    }
}