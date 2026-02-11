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

package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.domain.step.StepPhase
import com.jkjamies.cammp.domain.step.StepResult
import com.jkjamies.cammp.domain.step.runStepCatching
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@ContributesIntoSet(AppScope::class)
class GenerateNavigationDestinationStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val navigationRepo: NavigationRepository
) : PresentationStep {

    override val phase: StepPhase = StepPhase.GENERATE

    override suspend fun execute(params: PresentationParams): StepResult {
        if (!params.includeNavigation) {
            return StepResult.Success(null)
        }

        return runStepCatching {
            val sanitizedName = sanitizeScreenName(params.screenName)
            val pkg = inferPresentationPackage(modulePkgRepo, params.moduleDir)
            val kotlinDir = params.moduleDir.resolve("src/main/kotlin").also { if (!it.exists()) it.createDirectories() }
            val pkgDir = kotlinDir.resolve(pkg.replace('.', '/')).also { if (!it.exists()) it.createDirectories() }
            val folder = sanitizedName.replaceFirstChar { it.lowercase() }

            val navDir = pkgDir.resolve("navigation").also { if (!it.exists()) it.createDirectories() }
            val destDir = navDir.resolve("destinations").also { if (!it.exists()) it.createDirectories() }

            val result = navigationRepo.generateDestination(
                targetDir = destDir,
                packageName = pkg,
                params = params,
                screenFolder = folder
            )
            StepResult.Success("- navigation/destinations/${result.fileName}: ${result.path} (${result.status})")
        }
    }
}
