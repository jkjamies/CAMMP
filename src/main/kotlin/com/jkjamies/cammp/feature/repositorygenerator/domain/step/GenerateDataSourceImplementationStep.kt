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

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class GenerateDataSourceImplementationStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val scaffoldRepo: DatasourceScaffoldRepository
) : RepositoryStep {

    override suspend fun execute(params: RepositoryParams): StepResult {
        if (params.datasourceStrategy is DatasourceStrategy.None) return StepResult.Success(null)

        val results = mutableListOf<String>()
        val dataPkg = modulePkgRepo.findModulePackage(params.dataDir)
        val dataBasePkg = if (dataPkg.contains(".data")) dataPkg.substringBefore(".data") + ".data" else dataPkg

        val entries = when (params.datasourceStrategy) {
            DatasourceStrategy.None -> emptyList()
            DatasourceStrategy.Combined -> listOf("dataSource" to "DataSource")
            DatasourceStrategy.RemoteOnly -> listOf("remoteDataSource" to "RemoteDataSource")
            DatasourceStrategy.LocalOnly -> listOf("localDataSource" to "LocalDataSource")
            DatasourceStrategy.RemoteAndLocal -> listOf(
                "remoteDataSource" to "RemoteDataSource",
                "localDataSource" to "LocalDataSource",
            )
        }

        val useKoin = params.diStrategy is DiStrategy.Koin

        for ((moduleName, suffix) in entries) {
            val moduleDir = params.dataDir.parent?.resolve(moduleName)
                ?: return StepResult.Failure(IllegalStateException("Sibling module $moduleName not found"))

            if (!moduleDir.toFile().exists()) continue

            val modulePkg = modulePkgRepo.findModulePackage(moduleDir)
            val implDir = moduleDir.resolve("src/main/kotlin").resolve(modulePkg.replace('.', '/'))

            val ifaceName = params.className + suffix
            val ifacePkg = "$dataBasePkg.$moduleName" // Assuming subpackage matches module name
            val implName = "${ifaceName}Impl"

            try {
                val out = scaffoldRepo.generateImplementation(
                    directory = implDir,
                    packageName = modulePkg,
                    className = implName,
                    interfacePackage = ifacePkg,
                    interfaceName = ifaceName,
                    useKoin = useKoin
                )
                results += "- DataSource Impl: $out (generated)"
            } catch (e: Exception) {
                return StepResult.Failure(e)
            }
        }

        return StepResult.Success(results.joinToString("\n"))
    }
}