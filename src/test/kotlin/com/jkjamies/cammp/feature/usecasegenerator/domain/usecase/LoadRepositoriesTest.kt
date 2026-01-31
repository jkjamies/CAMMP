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

import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for [LoadRepositories].
 *
 * Reference: `src/main/kotlin/com/jkjamies/cammp/feature/usecasegenerator/domain/usecase/LoadRepositories.kt`
 */
class LoadRepositoriesTest : BehaviorSpec({

    class RepositoryDiscoveryRepositoryFake(
        private val result: List<String>,
    ) : RepositoryDiscoveryRepository {
        val calls = mutableListOf<String>()

        override fun loadRepositories(domainModulePath: String): List<String> {
            calls.add(domainModulePath)
            return result
        }
    }

    Given("LoadRepositories") {
        val domainModulePath = "/path/to/domain"
        val expectedRepositories = listOf("AuthRepository", "UserRepository")

        When("invoked with a domain module path") {
            Then("it should return the list from RepositoryDiscoveryRepository") {
                val repo = RepositoryDiscoveryRepositoryFake(expectedRepositories)
                val loadRepositories = LoadRepositories(repo)

                val result = loadRepositories(domainModulePath)

                result shouldBe expectedRepositories
                repo.calls shouldBe listOf(domainModulePath)
            }
        }
    }
})
