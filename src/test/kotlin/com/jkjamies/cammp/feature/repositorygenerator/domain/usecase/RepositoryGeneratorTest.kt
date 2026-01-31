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

package com.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.step.RepositoryStep
import com.jkjamies.cammp.feature.repositorygenerator.domain.step.StepResult
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeFailure
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.nio.file.Paths

class RepositoryGeneratorTest : BehaviorSpec({

    val step1 = mockk<RepositoryStep>()
    val step2 = mockk<RepositoryStep>()
    val steps = setOf(step1, step2)
    val generator = RepositoryGenerator(steps)

    val params = RepositoryParams(
        dataDir = Paths.get("data"),
        className = "TestRepo",
        datasourceStrategy = DatasourceStrategy.None,
        diStrategy = DiStrategy.Hilt
    )

    Given("A RepositoryGenerator with steps") {

        When("All steps succeed") {
            coEvery { step1.execute(params) } returns StepResult.Success("Step 1 done")
            coEvery { step2.execute(params) } returns StepResult.Success("Step 2 done")

            val result = generator(params)

            Then("Result should be success") {
                result.shouldBeSuccess()
            }

            Then("Result message should contain step messages") {
                val msg = result.getOrNull()
                msg shouldContain "Step 1 done"
                msg shouldContain "Step 2 done"
            }

            Then("All steps should be executed") {
                coVerify(exactly = 1) { step1.execute(params) }
                coVerify(exactly = 1) { step2.execute(params) }
            }
        }

        When("A step fails") {
            val error = RuntimeException("Step failed")
            coEvery { step1.execute(params) } returns StepResult.Success("Step 1 done")
            coEvery { step2.execute(params) } returns StepResult.Failure(error)

            val result = generator(params)

            Then("Result should be failure") {
                result.shouldBeFailure {
                    it shouldBe error
                }
            }
        }
    }
})
