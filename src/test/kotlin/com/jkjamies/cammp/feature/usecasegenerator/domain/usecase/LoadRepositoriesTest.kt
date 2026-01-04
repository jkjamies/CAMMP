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
