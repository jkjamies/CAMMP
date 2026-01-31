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

package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class GenerateUseCaseStep(
    private val repository: UseCaseGenerationRepository,
    private val modulePackageRepository: ModulePackageRepository
) : UseCaseStep {

    override suspend fun execute(params: UseCaseParams): StepResult {
        return try {
            val basePackage = modulePackageRepository.findModulePackage(params.domainDir)
                ?: throw IllegalStateException("Could not determine package for ${params.domainDir}")
            
            val targetPackage = if (basePackage.endsWith(".usecase")) {
                basePackage
            } else {
                "$basePackage.usecase"
            }

            // Calculate base domain package(e.g. com.example.domain)
            val marker = ".domain"
            val idx = basePackage.lastIndexOf(marker)
            val baseDomainPackage = if (idx >= 0) basePackage.substring(0, idx + marker.length) else basePackage

            val apiDir = params.domainDir.parent?.resolve("api")
            val result = repository.generateUseCase(params, targetPackage, baseDomainPackage, apiDir)
            
            val message = buildString {
                append("UseCase: ${result.useCasePath}")
                result.interfacePath?.let {
                    append("\nInterface: $it")
                }
            }
            StepResult.Success(path = result.useCasePath, message = message)
        } catch (e: Throwable) {
            StepResult.Failure(e)
        }
    }
}