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
import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class UpdateDataSourceDiStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val diRepo: DiModuleRepository
) : RepositoryStep {

    override val phase: StepPhase = StepPhase.DI

    override suspend fun execute(params: RepositoryParams): StepResult {
        if (params.datasourceStrategy is DatasourceStrategy.None) return StepResult.Success(null)
        if (params.diStrategy is DiStrategy.Koin && params.diStrategy.useAnnotations) return StepResult.Success(null)

        val diDir = params.dataDir.parent?.resolve("di") ?: return StepResult.Success(null)
        if (!diDir.toFile().exists()) return StepResult.Success(null)

        val diPackage = modulePkgRepo.findModulePackage(diDir)
        val dataPkg = modulePkgRepo.findModulePackage(params.dataDir)
        val dataBasePkg = if (dataPkg.contains(PackageSuffixes.DATA)) dataPkg.substringBefore(PackageSuffixes.DATA) + PackageSuffixes.DATA else dataPkg

        val entries = params.datasourceStrategy.toEntries()

        val useKoin = params.diStrategy is DiStrategy.Koin
        val bindings = mutableListOf<DataSourceBinding>()

        for ((moduleName, suffix) in entries) {
            val moduleDir = params.dataDir.parent?.resolve(moduleName) ?: continue
            if (!moduleDir.toFile().exists()) continue

            val modulePkg = modulePkgRepo.findModulePackage(moduleDir)
            val className = params.className + suffix
            val ifacePkg = "$dataBasePkg.$moduleName"

            val ifaceImport = "import $ifacePkg.$className"
            val implImport = "import $modulePkg.${className}Impl"

            if (!useKoin) {
                val signature = "abstract fun bind${className}(impl: ${className}Impl): ${className}"
                val block = buildString {
                    appendLine("    @Binds")
                    append("    ").append(signature)
                }
                bindings.add(DataSourceBinding(ifaceImport, implImport, signature, block))
            } else {
                val signature = "single<$className> { ${className}Impl(get()) }"
                val block = "    $signature"
                bindings.add(DataSourceBinding(ifaceImport, implImport, signature, block))
            }
        }

        if (bindings.isEmpty()) return StepResult.Success(null)

        return runStepCatching {
            val outcome = diRepo.mergeDataSourceModule(
                diDir = diDir,
                diPackage = diPackage,
                desiredBindings = bindings,
                useKoin = useKoin
            )
            StepResult.Success("- DI: ${outcome.outPath} (${outcome.status})")
        }
    }
}