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

package com.jkjamies.cammp.feature.presentationgenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.NavigationSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [NavigationRepository] that generates Navigation components using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
class NavigationRepositoryImpl(
    private val specFactory: NavigationSpecFactory
) : NavigationRepository {

    override fun generateNavigationHost(
        targetDir: Path,
        packageName: String,
        navHostName: String
    ): FileGenerationResult {
        val fileName = "$navHostName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.createHost(packageName, navHostName)

        target.writeText(fileSpec.toString())
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }

    override fun generateDestination(
        targetDir: Path,
        packageName: String,
        params: PresentationParams,
        screenFolder: String
    ): FileGenerationResult {
        val screenName = params.screenName
        val fileName = "${screenName}Destination.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.createDestination(packageName, screenName, screenFolder)
        val comments = specFactory.getDestinationComments(
            screenName = screenName,
            screenNameLower = screenName.replaceFirstChar { it.lowercase() }
        )

        val content = buildString {
            append(fileSpec.toString())
            append(comments)
        }

        target.writeText(content)
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
