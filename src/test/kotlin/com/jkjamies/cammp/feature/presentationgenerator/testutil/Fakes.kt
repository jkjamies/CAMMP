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

package com.jkjamies.cammp.feature.presentationgenerator.testutil

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.IntentRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationMergeOutcome
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.UiStateRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ViewModelRepository
import java.nio.file.Path

internal class ModulePackageRepositoryFake(
    private val pkg: String? = null,
) : ModulePackageRepository {
    override fun findModulePackage(moduleDir: Path): String? = pkg
}

internal class ScreenRepositoryFake(private val status: GenerationStatus = GenerationStatus.CREATED) : ScreenRepository {
    data class Call(val targetDir: Path, val packageName: String, val params: PresentationParams)
    val calls = mutableListOf<Call>()

    override fun generateScreen(targetDir: Path, packageName: String, params: PresentationParams): FileGenerationResult {
        calls += Call(targetDir, packageName, params)
        val target = targetDir.resolve("${params.screenName}.kt")
        return FileGenerationResult(target, status, target.fileName.toString())
    }
}

internal class UiStateRepositoryFake(private val status: GenerationStatus = GenerationStatus.CREATED) : UiStateRepository {
    data class Call(val targetDir: Path, val packageName: String, val params: PresentationParams)
    val calls = mutableListOf<Call>()

    override fun generateUiState(targetDir: Path, packageName: String, params: PresentationParams): FileGenerationResult {
        calls += Call(targetDir, packageName, params)
        val target = targetDir.resolve("${params.screenName}UiState.kt")
        return FileGenerationResult(target, status, target.fileName.toString())
    }
}

internal class ViewModelRepositoryFake(private val status: GenerationStatus = GenerationStatus.CREATED) : ViewModelRepository {
    data class Call(val targetDir: Path, val packageName: String, val params: PresentationParams)
    val calls = mutableListOf<Call>()

    override fun generateViewModel(targetDir: Path, packageName: String, params: PresentationParams): FileGenerationResult {
        calls += Call(targetDir, packageName, params)
        val target = targetDir.resolve("${params.screenName}ViewModel.kt")
        return FileGenerationResult(target, status, target.fileName.toString())
    }
}

internal class IntentRepositoryFake(private val status: GenerationStatus = GenerationStatus.CREATED) : IntentRepository {
    data class Call(val targetDir: Path, val packageName: String, val params: PresentationParams)
    val calls = mutableListOf<Call>()

    override fun generateIntent(targetDir: Path, packageName: String, params: PresentationParams): FileGenerationResult {
        calls += Call(targetDir, packageName, params)
        val target = targetDir.resolve("${params.screenName}Intent.kt")
        return FileGenerationResult(target, status, target.fileName.toString())
    }
}

internal class ScreenStateHolderRepositoryFake(private val status: GenerationStatus = GenerationStatus.CREATED) : ScreenStateHolderRepository {
    data class Call(val targetDir: Path, val packageName: String, val params: PresentationParams)
    val calls = mutableListOf<Call>()

    override fun generateScreenStateHolder(targetDir: Path, packageName: String, params: PresentationParams): FileGenerationResult {
        calls += Call(targetDir, packageName, params)
        val target = targetDir.resolve("${params.screenName}StateHolder.kt")
        return FileGenerationResult(target, status, target.fileName.toString())
    }
}

internal class FlowStateHolderRepositoryFake(private val status: GenerationStatus = GenerationStatus.CREATED) : FlowStateHolderRepository {
    data class Call(val targetDir: Path, val packageName: String, val flowName: String, val params: PresentationParams)
    val calls = mutableListOf<Call>()

    override fun generateFlowStateHolder(
        targetDir: Path,
        packageName: String,
        flowName: String,
        params: PresentationParams
    ): FileGenerationResult {
        calls += Call(targetDir, packageName, flowName, params)
        val target = targetDir.resolve("$flowName.kt")
        return FileGenerationResult(target, status, target.fileName.toString())
    }
}

internal class NavigationRepositoryFake(private val status: GenerationStatus = GenerationStatus.CREATED) : NavigationRepository {
    data class HostCall(val targetDir: Path, val packageName: String, val navHostName: String)
    data class DestinationCall(val targetDir: Path, val packageName: String, val params: PresentationParams, val screenFolder: String)

    val hostCalls = mutableListOf<HostCall>()
    val destinationCalls = mutableListOf<DestinationCall>()

    override fun generateNavigationHost(targetDir: Path, packageName: String, navHostName: String): FileGenerationResult {
        hostCalls += HostCall(targetDir, packageName, navHostName)
        val target = targetDir.resolve("$navHostName.kt")
        return FileGenerationResult(target, status, target.fileName.toString())
    }

    override fun generateDestination(
        targetDir: Path,
        packageName: String,
        params: PresentationParams,
        screenFolder: String
    ): FileGenerationResult {
        destinationCalls += DestinationCall(targetDir, packageName, params, screenFolder)
        val target = targetDir.resolve("${params.screenName}Destination.kt")
        return FileGenerationResult(target, status, target.fileName.toString())
    }
}

internal class PresentationDiModuleRepositoryFake(
    private val status: String = "created",
) : PresentationDiModuleRepository {
    data class Call(
        val diDir: Path,
        val diPackage: String,
        val viewModelSimpleName: String,
        val viewModelFqn: String,
        val dependencyCount: Int,
    )

    val calls = mutableListOf<Call>()

    override fun mergeViewModelModule(
        diDir: Path,
        diPackage: String,
        viewModelSimpleName: String,
        viewModelFqn: String,
        dependencyCount: Int
    ): PresentationMergeOutcome {
        calls += Call(diDir, diPackage, viewModelSimpleName, viewModelFqn, dependencyCount)
        return PresentationMergeOutcome(diDir.resolve("ViewModelModule.kt"), status)
    }
}
