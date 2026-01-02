package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.MergeOutcome
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Paths

class UpdateRepositoryDiStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val diRepo = mockk<DiModuleRepository>()
    val step = UpdateRepositoryDiStep(modulePkgRepo, diRepo)

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("an UpdateRepositoryDiStep") {
        val root = Paths.get("/project/feature")
        val dataDir = root.resolve("data")
        val domainDir = root.resolve("domain")
        val diDir = root.resolve("di")

        When("execute is called with Hilt strategy") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "MyRepository",
                includeDatasource = false,
                datasourceCombined = false,
                datasourceRemote = false,
                datasourceLocal = false,
                diStrategy = DiStrategy.Hilt
            )

            every { modulePkgRepo.findModulePackage(diDir) } returns "com.example.di"
            every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.data"
            every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.domain"
            
            coEvery { 
                diRepo.mergeRepositoryModule(any(), any(), any(), any(), any(), any()) 
            } returns MergeOutcome(diDir.resolve("RepositoryModule.kt"), "Updated")

            val result = step.execute(params)

            Then("it should merge repository module") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify { 
                    diRepo.mergeRepositoryModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        className = "MyRepository",
                        domainFqn = "com.example.domain.repository",
                        dataFqn = "com.example.data.repository",
                        useKoin = false
                    )
                }
            }
        }

        When("execute is called with Koin strategy") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "MyRepository",
                includeDatasource = false,
                datasourceCombined = false,
                datasourceRemote = false,
                datasourceLocal = false,
                diStrategy = DiStrategy.Koin(useAnnotations = false)
            )

            every { modulePkgRepo.findModulePackage(diDir) } returns "com.example.di"
            every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.data"
            every { modulePkgRepo.findModulePackage(domainDir) } returns "com.example.domain"
            
            coEvery { 
                diRepo.mergeRepositoryModule(any(), any(), any(), any(), any(), any()) 
            } returns MergeOutcome(diDir.resolve("RepositoryModule.kt"), "Updated")

            val result = step.execute(params)

            Then("it should merge repository module with useKoin=true") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify { 
                    diRepo.mergeRepositoryModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        className = "MyRepository",
                        domainFqn = "com.example.domain.repository",
                        dataFqn = "com.example.data.repository",
                        useKoin = true
                    )
                }
            }
        }
    }
})
