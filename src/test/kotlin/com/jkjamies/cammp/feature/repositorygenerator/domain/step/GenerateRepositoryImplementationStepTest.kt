package com.jkjamies.cammp.feature.repositorygenerator.domain.step

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
 * Tests for [GenerateRepositoryImplementationStep].
 */
class GenerateRepositoryImplementationStepTest : BehaviorSpec({

    fun params(dataDir: java.nio.file.Path) = RepositoryParams(
        dataDir = dataDir,
        className = "UserRepository",
        includeDatasource = false,
        datasourceCombined = false,
        datasourceRemote = false,
        datasourceLocal = false,
        diStrategy = DiStrategy.Hilt,
    )

    Given("GenerateRepositoryImplementationStep") {
        val root = Paths.get("/project/feature")
        val dataDir = root.resolve("data")
        val domainDir = root.resolve("domain")

        When("execute is called with valid paths") {
            Then("it should calculate correct packages and call generation repo") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val generationRepo = mockk<RepositoryGenerationRepository>()
                val step = GenerateRepositoryImplementationStep(modulePkgRepo, generationRepo)

                every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.feature.data"
                every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.feature.domain"
                coEvery { generationRepo.generateDataLayer(any(), any(), any()) } returns dataDir.resolve("UserRepositoryImpl.kt")

                val result = step.execute(params(dataDir))

                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 1) {
                    generationRepo.generateDataLayer(
                        params = params(dataDir),
                        dataPackage = "com.example.feature.data.repository",
                        domainPackage = "com.example.feature.domain.repository",
                    )
                }
            }
        }

        When("modulePkgRepo throws") {
            Then("it should return Failure") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val generationRepo = mockk<RepositoryGenerationRepository>()
                val step = GenerateRepositoryImplementationStep(modulePkgRepo, generationRepo)

                every { modulePkgRepo.findModulePackage(any()) } throws IllegalStateException("boom")

                step.execute(params(dataDir)).shouldBeInstanceOf<StepResult.Failure>()
            }
        }
    }
})
