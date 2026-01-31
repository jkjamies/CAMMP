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

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class GenerateRepositoryImplementationStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val generationRepo: RepositoryGenerationRepository
) : RepositoryStep {

    override suspend fun execute(params: RepositoryParams): StepResult {
        return try {
            val dataBase = modulePkgRepo.findModulePackage(params.dataDir)

            val domainDir = params.dataDir.parent?.resolve("domain")
                ?: error("Could not locate sibling domain module for ${params.dataDir}")
            val domainBase = modulePkgRepo.findModulePackage(domainDir)

            val domainFull = "$domainBase.repository"
            val dataFull = "$dataBase.repository"

            val dataOut = generationRepo.generateDataLayer(params, dataFull, domainFull)
            StepResult.Success("- Data Implementation: $dataOut (generated)")
        } catch (e: Exception) {
            StepResult.Failure(e)
        }
    }
}
