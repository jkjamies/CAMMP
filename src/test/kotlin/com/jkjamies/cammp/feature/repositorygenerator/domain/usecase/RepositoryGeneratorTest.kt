package com.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.MergeOutcome
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Path

class RepositoryGeneratorTest : BehaviorSpec({

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a repository generator") {
        val mockModulePkgRepo = mockk<ModulePackageRepository>()
        val mockDiRepo = mockk<DiModuleRepository>()
        val mockDsRepo = mockk<DatasourceScaffoldRepository>()
        val mockGenRepo = mockk<RepositoryGenerationRepository>()

        val useCase = RepositoryGenerator(
            modulePkgRepo = mockModulePkgRepo,
            diRepo = mockDiRepo,
            dsRepo = mockDsRepo,
            generationRepo = mockGenRepo
        )

        When("invoked with valid params") {
            val params = RepositoryParams(
                dataDir = Path.of("src/main/kotlin/com/test/data"),
                className = "TestRepository",
                includeDatasource = true,
                datasourceCombined = false,
                datasourceRemote = true,
                datasourceLocal = false,
                useKoin = false,
                koinAnnotations = false,
                selectedDataSources = emptyList()
            )

            // Mock behavior
            every { mockModulePkgRepo.findModulePackage(any()) } answers {
                val dir = firstArg<Path>()
                when {
                    dir.endsWith("data") -> "com.test.data"
                    dir.endsWith("domain") -> "com.test.domain"
                    dir.endsWith("di") -> "com.test.di"
                    else -> ""
                }
            }
            
            every { 
                mockDiRepo.mergeRepositoryModule(any(), any(), any(), any(), any(), any()) 
            } returns MergeOutcome(Path.of("di/Module.kt"), "merged")
            
            every {
                mockDiRepo.mergeDataSourceModule(any(), any(), any(), any())
            } returns MergeOutcome(Path.of("di/DsModule.kt"), "merged")

            every {
                mockDsRepo.generate(any(), any(), any(), any(), any(), any())
            } returns listOf("- Datasource: Generated")

            every {
                mockGenRepo.generateDomainLayer(any(), any(), any())
            } returns Path.of("domain/Repo.kt")

            every {
                mockGenRepo.generateDataLayer(any(), any(), any())
            } returns Path.of("data/RepoImpl.kt")

            val deepDataDir = Path.of("project/app/src/main/kotlin/com/test/data")
            val deepParams = params.copy(dataDir = deepDataDir)

            val result = useCase(deepParams)

            Then("it should return success message") {
                result.isSuccess shouldBe true
                val message = result.getOrNull() ?: ""
                message shouldContain "Repository generation completed"
                message shouldContain "Domain: domain/Repo.kt"
                message shouldContain "Data: data/RepoImpl.kt"
                message shouldContain "DI: di/Module.kt"
                message shouldContain "Datasource: Generated"
            }
        }
    }
})
