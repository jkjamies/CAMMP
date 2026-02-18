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
import com.jkjamies.cammp.feature.presentationgenerator.data.factory.IntentSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.IntentRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.writeText

/**
 * Implementation of [IntentRepository] that generates MVI Intent interfaces using KotlinPoet.
 */
@ContributesBinding(AppScope::class)
internal class IntentRepositoryImpl(
    private val specFactory: IntentSpecFactory
) : IntentRepository {

    override fun generateIntent(
        targetDir: Path,
        packageName: String,
        params: PresentationParams
    ): FileGenerationResult {
        val intentName = "${params.screenName}Intent"
        val fileName = "$intentName.kt"
        val target = targetDir.resolve(fileName)

        if (target.exists()) {
            return FileGenerationResult(target, GenerationStatus.SKIPPED, fileName)
        }

        val fileSpec = specFactory.create(packageName, params)

        target.writeText(fileSpec.toString())
        return FileGenerationResult(target, GenerationStatus.CREATED, fileName)
    }
}
