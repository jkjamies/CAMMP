package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify

/**
 * Test class for [LoadRepositories].
 */
class LoadRepositoriesTest : BehaviorSpec({

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a LoadRepositories use case") {
        val mockRepo = mockk<RepositoryDiscoveryRepository>()
        val loadRepositories = LoadRepositories(mockRepo)
        val domainModulePath = "/path/to/domain"
        val expectedRepositories = listOf("AuthRepository", "UserRepository")

        When("invoked with a domain module path") {
            every { mockRepo.loadRepositories(domainModulePath) } returns expectedRepositories

            val result = loadRepositories(domainModulePath)

            Then("it should return the list of repositories from the repository") {
                result shouldBe expectedRepositories
                verify { mockRepo.loadRepositories(domainModulePath) }
            }
        }
    }
})
