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
