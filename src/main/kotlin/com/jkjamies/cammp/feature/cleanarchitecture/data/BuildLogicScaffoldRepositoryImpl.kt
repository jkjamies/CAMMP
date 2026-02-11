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

import com.jkjamies.cammp.feature.cleanarchitecture.data.factory.BuildLogicSpecFactory
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AliasesRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.BuildLogicScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.ConventionPluginRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
internal class BuildLogicScaffoldRepositoryImpl(
    private val fs: FileSystemRepository,
    private val templateRepo: TemplateRepository,
    private val aliasesRepo: AliasesRepository,
    private val conventionPluginRepo: ConventionPluginRepository,
    private val buildLogicSpecFactory: BuildLogicSpecFactory,
) : BuildLogicScaffoldRepository {

    override fun ensureBuildLogic(params: CleanArchitectureParams, enabledModules: List<String>, diMode: DiMode): Boolean {
        val projectBase = params.projectBasePath
        val buildLogicDir = projectBase.resolve("build-logic")

        var changed = false

        if (!fs.exists(buildLogicDir)) {
            fs.createDirectories(buildLogicDir)
            changed = true
        }

        val settingsPath = buildLogicDir.resolve("settings.gradle.kts")
        if (!fs.exists(settingsPath)) {
            val raw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/settings.gradle.kts")
            fs.writeText(settingsPath, raw)
            changed = true
        }

        val buildPath = buildLogicDir.resolve("build.gradle.kts")
        if (!fs.exists(buildPath)) {
            val raw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/build.gradle.kts")
            fs.writeText(buildPath, buildLogicSpecFactory.applyPackageTokens(raw, params.orgCenter))
            changed = true
        }

        val safeOrg = sanitizeOrgCenter(params.orgCenter)
        val srcMainKotlin = buildLogicDir.resolve("src/main/kotlin").also {
            if (!fs.exists(it)) {
                fs.createDirectories(it)
                changed = true
            }
        }
        val basePkg = srcMainKotlin.resolve("com/${safeOrg.replace('.', '/')}/convention").also {
            if (!fs.exists(it)) {
                fs.createDirectories(it)
                changed = true
            }
        }

        val helpersDir = basePkg.resolve("helpers").also {
            if (!fs.exists(it)) {
                fs.createDirectories(it)
                changed = true
            }
        }
        val coreDir = basePkg.resolve("core").also {
            if (!fs.exists(it)) {
                fs.createDirectories(it)
                changed = true
            }
        }

        fun ensureHelper(fileName: String, templatePath: String) {
            val target = helpersDir.resolve(fileName)
            if (!fs.exists(target)) {
                val raw = templateRepo.getTemplateText(templatePath)
                fs.writeText(target, buildLogicSpecFactory.applyPackageTokens(raw, params.orgCenter))
                changed = true
            }
        }

        ensureHelper("AndroidLibraryDefaults.kt", "templates/cleanArchitecture/buildLogic/conventionPlugins/helpers/AndroidLibraryDefaults.kt")
        ensureHelper("StandardTestDependencies.kt", "templates/cleanArchitecture/buildLogic/conventionPlugins/helpers/StandardTestDependencies.kt")
        ensureHelper("TestOptions.kt", "templates/cleanArchitecture/buildLogic/conventionPlugins/helpers/TestOptions.kt")

        val conventionPackage = "com.$safeOrg.convention"

        // Idempotency:
        // We only generate files when their target file doesn't exist.
        // This is better than a marker file (which is opaque to users and can get stale).
        val aliasesTarget = coreDir.resolve("Aliases.kt")
        if (!fs.exists(aliasesTarget)) {
            val tomlPath = projectBase.resolve("gradle/libs.versions.toml")
            aliasesRepo.generateAliases(coreDir, "$conventionPackage.core", diMode, tomlPath)
            changed = true
        }

        val deps = coreDir.resolve("Dependencies.kt")
        if (!fs.exists(deps)) {
            val raw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/conventionPlugins/core/Dependencies.kt")
            fs.writeText(deps, buildLogicSpecFactory.applyPackageTokens(raw, params.orgCenter))
            changed = true
        }

        fun generatePlugin(type: PluginType, fileName: String) {
            val target = basePkg.resolve(fileName)
            if (!fs.exists(target)) {
                conventionPluginRepo.generate(basePkg, conventionPackage, diMode, type)
                changed = true
            }
        }

        if ("data" in enabledModules) generatePlugin(PluginType.DATA, "DataConventionPlugin.kt")
        if ("di" in enabledModules) generatePlugin(PluginType.DI, "DIConventionPlugin.kt")
        if ("domain" in enabledModules) generatePlugin(PluginType.DOMAIN, "DomainConventionPlugin.kt")
        if ("presentation" in enabledModules) generatePlugin(PluginType.PRESENTATION, "PresentationConventionPlugin.kt")
        if ("dataSource" in enabledModules) generatePlugin(PluginType.DATA_SOURCE, "DataSourceConventionPlugin.kt")
        if ("remoteDataSource" in enabledModules) generatePlugin(PluginType.REMOTE_DATA_SOURCE, "RemoteDataSourceConventionPlugin.kt")
        if ("localDataSource" in enabledModules) generatePlugin(PluginType.LOCAL_DATA_SOURCE, "LocalDataSourceConventionPlugin.kt")

        return changed
    }

    private fun sanitizeOrgCenter(input: String): String {
        val trimmed = input.trim()
        val unwrapped = if (trimmed.startsWith("\${") && trimmed.endsWith("}")) {
            trimmed.removePrefix("\${").removeSuffix("}")
        } else trimmed
        val withoutLeading = unwrapped.removePrefix("com.").removePrefix("org.")
        val cleaned = withoutLeading.replace(Regex("[^A-Za-z0-9_.]"), "").trim('.')
        return cleaned.ifBlank { "cammp" }.replaceFirstChar { it.lowercase() }
    }
}
