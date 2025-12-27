package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceOptions
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.MergeOutcome
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import io.mockk.verify
import java.nio.file.Files
import java.nio.file.Path

/**
 * Test class for [DatasourceScaffoldRepositoryImpl].
 */
class DatasourceScaffoldRepositoryImplTest : BehaviorSpec({

    val mockModulePkgRepo = mockk<ModulePackageRepository>()
    val mockDiRepo = mockk<DiModuleRepository>()

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a DatasourceScaffoldRepositoryImpl") {
        val repository = DatasourceScaffoldRepositoryImpl(mockModulePkgRepo, mockDiRepo)

        When("generate is called with include=false") {
            val options = DatasourceOptions(
                include = false,
                combined = false,
                remote = false,
                local = false,
                useKoin = false,
                koinAnnotations = false
            )
            val result = repository.generate(
                dataDir = Path.of("/tmp"),
                dataBasePkg = "com.example",
                repositoryBaseName = "Test",
                diDir = null,
                diPackage = null,
                options = options
            )

            Then("it should return empty list") {
                result.shouldHaveSize(0)
            }
        }

        When("generate is called with combined=true") {
            val tempDir = Files.createTempDirectory("test_scaffold_combined")
            val dataDir = tempDir.resolve("data")
            val dataSourceDir = tempDir.resolve("dataSource")
            
            Files.createDirectories(dataDir.resolve("src/main/kotlin"))
            Files.createDirectories(dataSourceDir.resolve("src/main/kotlin"))

            every { mockModulePkgRepo.findModulePackage(any()) } returns "com.example.datasource"

            val options = DatasourceOptions(
                include = true,
                combined = true,
                remote = false,
                local = false,
                useKoin = false,
                koinAnnotations = false
            )
            val result = repository.generate(
                dataDir = dataDir,
                dataBasePkg = "com.example.data",
                repositoryBaseName = "User",
                diDir = null,
                diPackage = null,
                options = options
            )

            Then("it should generate interface and implementation") {
                result.any { it.contains("DataSource Interface") } shouldBe true
                result.any { it.contains("DataSource Impl") } shouldBe true
                
                val ifaceFile = dataDir.resolve("src/main/kotlin/com/example/data/dataSource/UserDataSource.kt")
                Files.exists(ifaceFile) shouldBe true
                
                val implFile = dataSourceDir.resolve("src/main/kotlin/com/example/datasource/UserDataSourceImpl.kt")
                Files.exists(implFile) shouldBe true
            }
            
            tempDir.toFile().deleteRecursively()
        }

        When("generate is called with remote and local options") {
            val tempDir = Files.createTempDirectory("test_scaffold_split")
            val dataDir = tempDir.resolve("data")
            val remoteDir = tempDir.resolve("remoteDataSource")
            val localDir = tempDir.resolve("localDataSource")
            
            Files.createDirectories(dataDir.resolve("src/main/kotlin"))
            Files.createDirectories(remoteDir.resolve("src/main/kotlin"))
            Files.createDirectories(localDir.resolve("src/main/kotlin"))

            every { mockModulePkgRepo.findModulePackage(any()) } returns "com.example.datasource"

            val options = DatasourceOptions(
                include = true,
                combined = false,
                remote = true,
                local = true,
                useKoin = false,
                koinAnnotations = false
            )
            val result = repository.generate(
                dataDir = dataDir,
                dataBasePkg = "com.example.data",
                repositoryBaseName = "User",
                diDir = null,
                diPackage = null,
                options = options
            )

            Then("it should generate remote and local data sources") {
                result.any { it.contains("RemoteDataSource") } shouldBe true
                result.any { it.contains("LocalDataSource") } shouldBe true
            }
            
            tempDir.toFile().deleteRecursively()
        }

        When("generate is called with DI options (Hilt)") {
            val tempDir = Files.createTempDirectory("test_scaffold_di")
            val dataDir = tempDir.resolve("data")
            val dataSourceDir = tempDir.resolve("dataSource")
            val diDir = tempDir.resolve("di")
            
            Files.createDirectories(dataDir.resolve("src/main/kotlin"))
            Files.createDirectories(dataSourceDir.resolve("src/main/kotlin"))
            Files.createDirectories(diDir)

            every { mockModulePkgRepo.findModulePackage(any()) } returns "com.example.datasource"
            every { mockDiRepo.mergeDataSourceModule(any(), any(), any(), any()) } returns MergeOutcome(diDir.resolve("Module.kt"), "Updated")

            val options = DatasourceOptions(
                include = true,
                combined = true,
                remote = false,
                local = false,
                useKoin = false,
                koinAnnotations = false
            )
            val result = repository.generate(
                dataDir = dataDir,
                dataBasePkg = "com.example.data",
                repositoryBaseName = "User",
                diDir = diDir,
                diPackage = "com.example.di",
                options = options
            )

            Then("it should call mergeDataSourceModule") {
                verify { mockDiRepo.mergeDataSourceModule(diDir, "com.example.di", any(), false) }
                result.any { it.contains("DI:") } shouldBe true
            }
            
            tempDir.toFile().deleteRecursively()
        }
        
        When("generate is called with DI options (Koin)") {
            val tempDir = Files.createTempDirectory("test_scaffold_di_koin")
            val dataDir = tempDir.resolve("data")
            val dataSourceDir = tempDir.resolve("dataSource")
            val diDir = tempDir.resolve("di")
            
            Files.createDirectories(dataDir.resolve("src/main/kotlin"))
            Files.createDirectories(dataSourceDir.resolve("src/main/kotlin"))
            Files.createDirectories(diDir)

            every { mockModulePkgRepo.findModulePackage(any()) } returns "com.example.datasource"
            every { mockDiRepo.mergeDataSourceModule(any(), any(), any(), any()) } returns MergeOutcome(diDir.resolve("Module.kt"), "Updated")

            val options = DatasourceOptions(
                include = true,
                combined = true,
                remote = false,
                local = false,
                useKoin = true,
                koinAnnotations = false
            )
            val result = repository.generate(
                dataDir = dataDir,
                dataBasePkg = "com.example.data",
                repositoryBaseName = "User",
                diDir = diDir,
                diPackage = "com.example.di",
                options = options
            )

            Then("it should call mergeDataSourceModule with useKoin=true") {
                verify { mockDiRepo.mergeDataSourceModule(diDir, "com.example.di", any(), true) }
            }
            
            tempDir.toFile().deleteRecursively()
        }
    }
})
