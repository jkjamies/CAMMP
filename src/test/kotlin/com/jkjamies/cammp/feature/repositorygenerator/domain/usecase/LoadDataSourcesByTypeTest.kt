package com.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceDiscoveryRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify

/**
 * Test class for [LoadDataSourcesByType].
 */
class LoadDataSourcesByTypeTest : BehaviorSpec({

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a LoadDataSourcesByType use case") {
        val mockRepo = mockk<DataSourceDiscoveryRepository>()
        val loadDataSourcesByType = LoadDataSourcesByType(mockRepo)
        val dataModulePath = "/path/to/data"
        val expectedDataSources = mapOf(
            "Remote" to listOf("com.example.data.datasource.RemoteDataSource"),
            "Local" to listOf("com.example.data.datasource.LocalDataSource")
        )

        When("invoked with a data module path") {
            every { mockRepo.loadDataSourcesByType(dataModulePath) } returns expectedDataSources

            val result = loadDataSourcesByType(dataModulePath)

            Then("it should return the map of data sources from the repository") {
                result shouldBe expectedDataSources
                verify { mockRepo.loadDataSourcesByType(dataModulePath) }
            }
        }
    }
})
