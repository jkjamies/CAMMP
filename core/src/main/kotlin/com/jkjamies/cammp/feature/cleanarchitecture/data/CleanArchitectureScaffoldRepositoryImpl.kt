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

package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.domain.codegen.PackageSuffixes
import com.jkjamies.cammp.feature.cleanarchitecture.data.factory.ModuleBuildGradleSpecFactory
import com.jkjamies.cammp.feature.cleanarchitecture.data.factory.ModuleSourceSpecFactory
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.domain.model.DatasourceStrategy
import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.CleanArchitectureScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path

/**
 * Repository implementation that performs IO: creating module dirs, writing templates, and applying merges.
 *
 * Reference: legacy implementation previously in `domain/usecase/CleanArchitectureGenerator.kt`.
 */
@ContributesBinding(AppScope::class)
class CleanArchitectureScaffoldRepositoryImpl(
    private val fs: FileSystemRepository,
    private val templateRepo: TemplateRepository,
    private val annotationModuleRepo: AnnotationModuleRepository,
    private val buildGradleSpecFactory: ModuleBuildGradleSpecFactory,
    private val sourceSpecFactory: ModuleSourceSpecFactory,
) : CleanArchitectureScaffoldRepository {

    override suspend fun generateModules(params: CleanArchitectureParams): CleanArchitectureResult {
        return generate(params)
    }

    private fun generate(p: CleanArchitectureParams): CleanArchitectureResult {
        require(fs.isDirectory(p.projectBasePath)) {
            "Project base path does not exist or is not a directory: ${p.projectBasePath}"
        }

        val featureName = p.feature.split('-')
            .mapIndexed { index, s -> if (index > 0) s.replaceFirstChar { it.titlecase() } else s }
            .joinToString("")
            .replaceFirstChar { it.lowercase() }
        val featureDirName = p.feature

        val rootDir = p.projectBasePath.resolve(p.root).also { if (!fs.exists(it)) fs.createDirectories(it) }
        val featureDir = rootDir.resolve(featureDirName).also { if (!fs.exists(it)) fs.createDirectories(it) }

        val modules = buildList {
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

        val created = mutableListOf<String>()
        val skipped = mutableListOf<String>()

        for (m in modules) {
            val moduleDir = featureDir.resolve(m)
            if (fs.exists(moduleDir)) {
                skipped += m
            } else {
                fs.createDirectories(moduleDir)
                scaffoldModuleBuildFile(moduleDir, p, m, featureName, modules)
                scaffoldModuleSourceSkeleton(moduleDir, p, m, featureName)
                created += m
            }
        }

        val msg = if (created.isEmpty()) {
            "No modules created (all existed)."
        } else {
            "Created modules: ${created.joinToString()}."
        }

        return CleanArchitectureResult(
            created = created,
            skipped = skipped,
            settingsUpdated = false,
            buildLogicCreated = false,
            message = msg,
        )
    }

    private fun scaffoldModuleBuildFile(
        moduleDir: Path,
        p: CleanArchitectureParams,
        moduleName: String,
        featureName: String,
        enabledModules: List<String>
    ) {
        val templatePath = when (moduleName) {
            "api" -> "templates/cleanArchitecture/module/api.gradle.kts"
            "domain" -> "templates/cleanArchitecture/module/domain.gradle.kts"
            "data" -> "templates/cleanArchitecture/module/data.gradle.kts"
            "di" -> "templates/cleanArchitecture/module/di.gradle.kts"
            "presentation" -> "templates/cleanArchitecture/module/presentation.gradle.kts"
            "dataSource" -> "templates/cleanArchitecture/module/dataSource.gradle.kts"
            "remoteDataSource" -> "templates/cleanArchitecture/module/remoteDataSource.gradle.kts"
            "localDataSource" -> "templates/cleanArchitecture/module/localDataSource.gradle.kts"
            else -> "templates/cleanArchitecture/module/domain.gradle.kts"
        }

        val raw = templateRepo.getTemplateText(templatePath)
        val content = buildGradleSpecFactory.create(
            params = p,
            moduleName = moduleName,
            featureName = featureName,
            enabledModules = enabledModules,
            rawTemplate = raw,
        )

        fs.writeText(moduleDir.resolve("build.gradle.kts"), content)
    }

    private fun scaffoldModuleSourceSkeleton(
        moduleDir: Path,
        p: CleanArchitectureParams,
        moduleName: String,
        featureName: String
    ) {
        val pkg = sourceSpecFactory.packageName(p, moduleName, featureName)
        val srcMainKotlin = moduleDir.resolve("src/main/kotlin").also { if (!fs.exists(it)) fs.createDirectories(it) }
        moduleDir.resolve("src/test/kotlin").also { if (!fs.exists(it)) fs.createDirectories(it) }
        val pkgDir = srcMainKotlin.resolve(pkg.replace('.', '/')).also { if (!fs.exists(it)) fs.createDirectories(it) }

        val placeholderFile = pkgDir.resolve("Placeholder.kt")
        if (!fs.exists(placeholderFile)) {
            fs.writeText(placeholderFile, sourceSpecFactory.placeholderKotlinFile(p, moduleName, featureName))
        }

        // Create standard subpackages
        when (moduleName) {
            "api" -> {
                listOf("model", "usecase").forEach { sub ->
                    val d = pkgDir.resolve(sub)
                    if (!fs.exists(d)) fs.createDirectories(d)
                }
            }

            "domain" -> {
                listOf("repository", "model", "usecase").forEach { sub ->
                    val d = pkgDir.resolve(sub)
                    if (!fs.exists(d)) fs.createDirectories(d)
                }
            }

            "data" -> {
                val repoDir = pkgDir.resolve("repository")
                if (!fs.exists(repoDir)) fs.createDirectories(repoDir)

                when (p.datasourceStrategy) {
                    DatasourceStrategy.None -> Unit
                    DatasourceStrategy.Combined -> {
                        val d = pkgDir.resolve("dataSource")
                        if (!fs.exists(d)) fs.createDirectories(d)
                    }
                    DatasourceStrategy.RemoteOnly -> {
                        val d = pkgDir.resolve("remoteDataSource")
                        if (!fs.exists(d)) fs.createDirectories(d)
                    }
                    DatasourceStrategy.LocalOnly -> {
                        val d = pkgDir.resolve("localDataSource")
                        if (!fs.exists(d)) fs.createDirectories(d)
                    }
                    DatasourceStrategy.RemoteAndLocal -> {
                        val rd = pkgDir.resolve("remoteDataSource")
                        if (!fs.exists(rd)) fs.createDirectories(rd)
                        val ld = pkgDir.resolve("localDataSource")
                        if (!fs.exists(ld)) fs.createDirectories(ld)
                    }
                }
            }

            "di" -> {
                val useKoinAnnotations = (p.diStrategy as? DiStrategy.Koin)
                    ?.useAnnotations == true
                if (useKoinAnnotations && p.includeDiModule) {
                    val scanBase = sourceSpecFactory.packageName(p, moduleName = "di", featureName = featureName)
                        .removeSuffix(PackageSuffixes.DI)
                    annotationModuleRepo.generate(
                        outputDirectory = pkgDir,
                        packageName = pkg,
                        scanPackage = scanBase,
                        featureName = featureName
                    )
                }
                
                // Ensure subpackages for DI even if empty initially, helps subsequent generators
                listOf("usecase", "repository").forEach { sub ->
                    val d = pkgDir.resolve(sub)
                    if (!fs.exists(d)) fs.createDirectories(d)
                }
            }
        }
    }
}
