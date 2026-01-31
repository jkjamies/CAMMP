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

package com.jkjamies.cammp.feature.cleanarchitecture.testutil

import com.jkjamies.cammp.feature.cleanarchitecture.data.datasource.GradleSettingsDataSource
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AliasesRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.BuildLogicScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.CleanArchitectureScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.ConventionPluginRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import java.nio.file.Path

/**
 * Keep test doubles for cleanarchitecture in one place.
 */
class GradleSettingsDataSourceFake(
    var includesChanged: Boolean = false,
    var includeBuildChanged: Boolean = false,
    var aliasesChanged: Boolean = false,
    var appDepChanged: Boolean = false,
) : GradleSettingsDataSource {

    data class EnsureIncludesCall(
        val projectBase: Path,
        val root: String,
        val feature: String,
        val modules: List<String>,
    )

    val ensureIncludesCalls = mutableListOf<EnsureIncludesCall>()

    data class EnsureIncludeBuildCall(
        val projectBase: Path,
        val buildLogicName: String,
    )

    val ensureIncludeBuildCalls = mutableListOf<EnsureIncludeBuildCall>()

    data class EnsureVersionCatalogPluginAliasesCall(
        val projectBase: Path,
        val orgSegment: String,
        val enabledModules: List<String>,
    )

    val ensureVersionCatalogPluginAliasesCalls = mutableListOf<EnsureVersionCatalogPluginAliasesCall>()

    data class EnsureAppDependencyCall(
        val projectBase: Path,
        val root: String,
        val feature: String,
        val diMode: DiMode,
    )

    val ensureAppDependencyCalls = mutableListOf<EnsureAppDependencyCall>()

    override fun ensureIncludes(projectBase: Path, root: String, feature: String, modules: List<String>): Boolean {
        ensureIncludesCalls += EnsureIncludesCall(projectBase, root, feature, modules)
        return includesChanged
    }

    override fun ensureIncludeBuild(projectBase: Path, buildLogicName: String): Boolean {
        ensureIncludeBuildCalls += EnsureIncludeBuildCall(projectBase, buildLogicName)
        return includeBuildChanged
    }

    override fun ensureVersionCatalogPluginAliases(projectBase: Path, orgSegment: String, enabledModules: List<String>): Boolean {
        ensureVersionCatalogPluginAliasesCalls += EnsureVersionCatalogPluginAliasesCall(projectBase, orgSegment, enabledModules)
        return aliasesChanged
    }

    override fun ensureAppDependency(projectBase: Path, root: String, feature: String, diMode: DiMode): Boolean {
        ensureAppDependencyCalls += EnsureAppDependencyCall(projectBase, root, feature, diMode)
        return appDepChanged
    }
}

class BuildLogicScaffoldRepositoryFake(
    private val updated: Boolean,
) : BuildLogicScaffoldRepository {

    data class Call(
        val params: CleanArchitectureParams,
        val enabledModules: List<String>,
        val diMode: DiMode,
    )

    val calls = mutableListOf<Call>()

    override fun ensureBuildLogic(params: CleanArchitectureParams, enabledModules: List<String>, diMode: DiMode): Boolean {
        calls += Call(params, enabledModules, diMode)
        return updated
    }
}

class GradleSettingsScaffoldRepositoryFake(
    private val updated: Boolean,
) : GradleSettingsScaffoldRepository {

    data class Call(
        val params: CleanArchitectureParams,
        val enabledModules: List<String>,
        val diMode: DiMode,
    )

    val calls = mutableListOf<Call>()

    override fun ensureSettings(params: CleanArchitectureParams, enabledModules: List<String>, diMode: DiMode): Boolean {
        calls += Call(params, enabledModules, diMode)
        return updated
    }
}

class CleanArchitectureScaffoldRepositoryFake(
    private val onGenerate: (CleanArchitectureParams) -> CleanArchitectureResult,
) : CleanArchitectureScaffoldRepository {

    val calls = mutableListOf<CleanArchitectureParams>()

    override suspend fun generateModules(params: CleanArchitectureParams): CleanArchitectureResult {
        calls += params
        return onGenerate(params)
    }
}

/**
 * A fake [AliasesRepository] that writes the generated output to a real [com.jkjamies.cammp.feature.cleanarchitecture.data.FileSystemRepositoryImpl]
 * so higher-level repository integration tests can assert on the presence of files.
 */
class AliasesRepositoryWritingFake(
    private val fs: com.jkjamies.cammp.feature.cleanarchitecture.data.FileSystemRepositoryImpl,
) : AliasesRepository {

    override fun generateAliases(outputDirectory: Path, packageName: String, diMode: DiMode, tomlPath: Path) {
        val out = outputDirectory.resolve("Aliases.kt")
        fs.createDirectories(outputDirectory)
        fs.writeText(
            path = out,
            content = "package $packageName\n\n// fake Aliases for tests\nval DI_MODE = \"$diMode\"\nval TOML_PATH = \"$tomlPath\"\n",
            overwriteIfExists = true,
        )
    }
}

/**
 * A fake [ConventionPluginRepository] that writes a placeholder plugin file so callers can assert on filesystem effects.
 */
class ConventionPluginRepositoryWritingFake(
    private val fs: com.jkjamies.cammp.feature.cleanarchitecture.data.FileSystemRepositoryImpl,
) : ConventionPluginRepository {

    override fun generate(outputDirectory: Path, packageName: String, diMode: DiMode, type: PluginType) {
        fs.createDirectories(outputDirectory)
        val fileName = when (type) {
            PluginType.DATA -> "DataConventionPlugin.kt"
            PluginType.DI -> "DiConventionPlugin.kt"
            PluginType.DOMAIN -> "DomainConventionPlugin.kt"
            PluginType.PRESENTATION -> "PresentationConventionPlugin.kt"
            PluginType.DATA_SOURCE -> "DataSourceConventionPlugin.kt"
            PluginType.REMOTE_DATA_SOURCE -> "RemoteDataSourceConventionPlugin.kt"
            PluginType.LOCAL_DATA_SOURCE -> "LocalDataSourceConventionPlugin.kt"
        }

        fs.writeText(
            path = outputDirectory.resolve(fileName),
            content = "package $packageName\n\n// fake convention plugin for tests\n",
            overwriteIfExists = true,
        )
    }
}
