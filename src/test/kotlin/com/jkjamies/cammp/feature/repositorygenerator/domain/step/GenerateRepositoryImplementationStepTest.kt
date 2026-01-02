package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Paths

class GenerateRepositoryImplementationStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val generationRepo = mockk<RepositoryGenerationRepository>()
    val step = GenerateRepositoryImplementationStep(modulePkgRepo, generationRepo)

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a GenerateRepositoryImplementationStep") {
        val root = Paths.get("/project/feature")
        val dataDir = root.resolve("data")
        val domainDir = root.resolve("domain")

        val params = RepositoryParams(
            dataDir = dataDir,
            className = "UserRepository",
            includeDatasource = false,
            datasourceCombined = false,
            datasourceRemote = false,
            datasourceLocal = false,
            diStrategy = DiStrategy.Hilt
        )

        When("execute is called with valid paths") {
            every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.feature.data"
            every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.feature.domain"
            coEvery { generationRepo.generateDataLayer(any(), any(), any()) } returns dataDir.resolve("UserRepositoryImpl.kt")

            val result = step.execute(params)

            Then("it should calculate correct packages and call repository") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify {
                    generationRepo.generateDataLayer(
                        params = params,
                        dataPackage = "com.example.feature.data.repository",
                        domainPackage = "com.example.feature.domain.repository"
                    )
                }
            }
        }
    }
})
