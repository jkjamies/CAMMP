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

package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.domain.codegen.PackageSuffixes
import com.jkjamies.cammp.domain.step.StepPhase
import com.jkjamies.cammp.domain.step.StepResult
import com.jkjamies.cammp.domain.step.runStepCatching
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class UpdateDiStep(
    private val diRepository: UseCaseDiModuleRepository,
    private val modulePackageRepository: ModulePackageRepository
) : UseCaseStep {

    override val phase: StepPhase = StepPhase.DI

    override suspend fun execute(params: UseCaseParams): StepResult {
        val diDir = params.domainDir.parent?.resolve("di")
            ?: return StepResult.Success(null)

        if (!diDir.toFile().exists()) {
            return StepResult.Success(null)
        }

        val diPackageRaw = modulePackageRepository.findModulePackage(diDir)
            ?: return StepResult.Success(null)

        return runStepCatching {
            val diPackage = diPackageRaw.removeSuffix(PackageSuffixes.USE_CASE).removeSuffix(PackageSuffixes.REPOSITORY)

            val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
            if (!diTargetDir.toFile().exists()) {
                diTargetDir.toFile().mkdirs()
            }

            val packageFound = modulePackageRepository.findModulePackage(params.domainDir) ?: ""
            // The package repo returns the usecase package (e.g. ...domain.usecase). Strip it to find the sibling.
            val domainBase = if (packageFound.endsWith(PackageSuffixes.USE_CASE)) packageFound.removeSuffix(PackageSuffixes.USE_CASE) else packageFound
            val useCasePackage = "$domainBase${PackageSuffixes.USE_CASE}"
            val useCaseFqn = "$useCasePackage.${params.className}"

            val repoFqns = params.repositories.map { simpleName ->
                "$domainBase.repository.$simpleName"
            }

            val apiDir = params.domainDir.parent?.resolve("api")
            val useCaseInterfaceFqn = if (apiDir?.toFile()?.exists() == true) {
                val apiBase = diPackage.removeSuffix(PackageSuffixes.DI)
                "$apiBase.usecase.${params.className}"
            } else null

            val outcome = diRepository.mergeUseCaseModule(
                diDir = diDir,
                diPackage = diPackage,
                useCaseSimpleName = params.className,
                useCaseFqn = useCaseFqn,
                repositoryFqns = repoFqns,
                diStrategy = params.diStrategy,
                useCaseInterfaceFqn = useCaseInterfaceFqn
            )

            StepResult.Success(message = "DI: ${outcome.outPath} (${outcome.status})")
        }
    }
}