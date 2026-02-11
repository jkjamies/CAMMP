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

package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.domain.step.StepResult
import com.jkjamies.cammp.feature.usecasegenerator.domain.step.UseCaseStep
import dev.zacsweers.metro.Inject

@Inject
class UseCaseGenerator(
    private val steps: Set<UseCaseStep>
) {
    suspend operator fun invoke(params: UseCaseParams): Result<String> {
        return runCatching {
            val className = if (params.className.endsWith("UseCase")) params.className else "${params.className}UseCase"
            val updatedParams = params.copy(className = className)

            val messages = mutableListOf<String>()
            
            for (step in steps.sortedBy { it.phase }) {
                when (val result = step.execute(updatedParams)) {
                    is StepResult.Success -> {
                        result.message?.let { messages.add(it) }
                    }
                    is StepResult.Failure -> throw result.error
                }
            }
            
            if (messages.isEmpty()) throw IllegalStateException("No output from generation steps")
            messages.joinToString("\n\n")
        }
    }
}
