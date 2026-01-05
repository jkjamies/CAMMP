package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Paths

/**
 * Tests for [GenerateRepositoryInterfaceStep].
 */
class GenerateRepositoryInterfaceStepTest : BehaviorSpec({

    fun params(dataDir: java.nio.file.Path) = RepositoryParams(
        dataDir = dataDir,
        className = "User",
        datasourceStrategy = DatasourceStrategy.None,
        diStrategy = DiStrategy.Hilt,
    )

    Given("GenerateRepositoryInterfaceStep") {
        val root = Paths.get("/project/feature")
        val dataDir = root.resolve("data")
        val domainDir = root.resolve("domain")

        When("domain module package already ends with .domain") {
            Then("it should use <domain>.repository") {
                val modulePkgRepo = mockk<ModulePackageRepository>(relaxed = true)
                val generationRepo = mockk<RepositoryGenerationRepository>()
                val step = GenerateRepositoryInterfaceStep(modulePkgRepo, generationRepo)

                every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.feature.domain"
                coEvery { generationRepo.generateDomainLayer(any(), any(), any()) } returns domainDir.resolve("UserRepository.kt")

                val result = step.execute(params(dataDir))

                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 1) {
                    generationRepo.generateDomainLayer(
                        params(dataDir),
                        match { it.endsWith(".repository") },
                        domainDir,
                    )
                }
            }
        }

        When("domain module package is a subpackage under .domain") {
            Then("it should normalize to base .domain") {
                val modulePkgRepo = mockk<ModulePackageRepository>(relaxed = true)
                val generationRepo = mockk<RepositoryGenerationRepository>()
                val step = GenerateRepositoryInterfaceStep(modulePkgRepo, generationRepo)

                every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.feature.domain.usecase"
                coEvery { generationRepo.generateDomainLayer(any(), any(), any()) } returns domainDir.resolve("UserRepository.kt")

                val result = step.execute(params(dataDir))

                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 1) {
                    generationRepo.generateDomainLayer(
                        params(dataDir),
                        match { it.endsWith(".repository") },
                        domainDir,
                    )
                }
            }
        }

        When("modulePkgRepo throws") {
            Then("it returns Failure") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val generationRepo = mockk<RepositoryGenerationRepository>()
                val step = GenerateRepositoryInterfaceStep(modulePkgRepo, generationRepo)

                every { modulePkgRepo.findModulePackage(any()) } throws IllegalStateException("boom")

                step.execute(params(dataDir)).shouldBeInstanceOf<StepResult.Failure>()
            }
        }
    }
})
