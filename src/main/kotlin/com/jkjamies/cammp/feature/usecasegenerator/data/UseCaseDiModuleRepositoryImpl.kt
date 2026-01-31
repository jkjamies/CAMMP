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

package com.jkjamies.cammp.feature.usecasegenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.DiModuleDataSource
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseMergeOutcome
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
class UseCaseDiModuleRepositoryImpl(
    private val dataSource: DiModuleDataSource
) : UseCaseDiModuleRepository {

    override fun mergeUseCaseModule(
        diDir: Path,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
        diStrategy: DiStrategy,
        useCaseInterfaceFqn: String?
    ): UseCaseMergeOutcome {
        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        if (!diTargetDir.exists()) diTargetDir.createDirectories()
        val out = diTargetDir.resolve("UseCaseModule.kt")
        val existing = if (out.exists()) out.readText() else null

        val content = when {
            diStrategy is DiStrategy.Koin -> {
                dataSource.generateKoinModuleContent(
                    existing,
                    diPackage,
                    useCaseSimpleName,
                    useCaseFqn,
                    repositoryFqns,
                    useCaseInterfaceFqn
                )
            }
            diStrategy is DiStrategy.Hilt && useCaseInterfaceFqn != null -> {
                dataSource.generateHiltModuleContent(
                    existing,
                    diPackage,
                    useCaseSimpleName,
                    useCaseFqn,
                    useCaseInterfaceFqn
                )
            }
            else -> {
                // Return skipped for Hilt without interface or Metro for now
                return UseCaseMergeOutcome(out, "skipped")
            }
        }

        val changed = existing == null || existing != content
        out.writeText(content)
        val status = when {
            existing == null -> "created"
            changed -> "updated"
            else -> "exists"
        }
        return UseCaseMergeOutcome(out, status)
    }
}
