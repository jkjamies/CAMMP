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

class GenerateRepositoryInterfaceStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val generationRepo = mockk<RepositoryGenerationRepository>()
    val step = GenerateRepositoryInterfaceStep(modulePkgRepo, generationRepo)

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("GenerateRepositoryInterfaceStep") {
        val root = Paths.get("/project/feature")
        val dataDir = root.resolve("data")
        val domainDir = root.resolve("domain")

        val params = RepositoryParams(
            dataDir = dataDir,
            className = "User",
            includeDatasource = false,
            datasourceCombined = false,
            datasourceRemote = false,
            datasourceLocal = false,
            diStrategy = DiStrategy.Hilt
        )

        When("execute is called") {
            every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.domain"
            coEvery { generationRepo.generateDomainLayer(any(), any(), any()) } returns domainDir.resolve("UserRepository.kt")

            val result = step.execute(params)

            Then("it should generate domain layer") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify { 
                    generationRepo.generateDomainLayer(
                        params,
                        "com.example.domain.repository",
                        domainDir
                    )
                }
            }
        }
    }
})
