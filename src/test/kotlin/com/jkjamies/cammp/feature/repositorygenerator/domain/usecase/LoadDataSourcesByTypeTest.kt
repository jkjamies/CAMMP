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

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceDiscoveryRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify

/**
 * Test class for [LoadDataSourcesByType].
 */
class LoadDataSourcesByTypeTest : BehaviorSpec({

    Given("LoadDataSourcesByType") {

        When("invoked with a data module path") {
            Then("it returns the map of data sources from the repository") {
                val repo = mockk<DataSourceDiscoveryRepository>()
                val useCase = LoadDataSourcesByType(repo)

                val dataModulePath = "/path/to/data"
                val expected = mapOf(
                    "Remote" to listOf("com.example.data.datasource.RemoteDataSource"),
                    "Local" to listOf("com.example.data.datasource.LocalDataSource"),
                )

                every { repo.loadDataSourcesByType(dataModulePath) } returns expected

                useCase(dataModulePath) shouldBe expected
                verify(exactly = 1) { repo.loadDataSourcesByType(dataModulePath) }
            }
        }
    }
})
