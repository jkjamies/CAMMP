package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Files

class GenerateDataSourceImplementationStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val scaffoldRepo = mockk<DatasourceScaffoldRepository>()
    val step = GenerateDataSourceImplementationStep(modulePkgRepo, scaffoldRepo)

    val tempRoot = Files.createTempDirectory("cammp_test_gen_ds_impl")
    val featureRoot = tempRoot.resolve("feature")
    val dataDir = featureRoot.resolve("data")
    val dataSourceDir = featureRoot.resolve("dataSource")
    val remoteDataSourceDir = featureRoot.resolve("remoteDataSource")
    val localDataSourceDir = featureRoot.resolve("localDataSource")

    // Create physical directories
    Files.createDirectories(dataDir)
    Files.createDirectories(dataSourceDir)
    Files.createDirectories(remoteDataSourceDir)
    Files.createDirectories(localDataSourceDir)

    beforeContainer {
        clearAllMocks()
        // Mock package finding
        every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.data"
        every { modulePkgRepo.findModulePackage(dataSourceDir) } returns "com.example.datasource"
        every { modulePkgRepo.findModulePackage(remoteDataSourceDir) } returns "com.example.remote"
        every { modulePkgRepo.findModulePackage(localDataSourceDir) } returns "com.example.local"

        // Mock scaffold repo
        coEvery {
            scaffoldRepo.generateImplementation(any(), any(), any(), any(), any(), any())
        } returns dataSourceDir.resolve("File.kt")
    }

    afterSpec {
        unmockkAll()
        tempRoot.toFile().deleteRecursively()
    }

    Given("GenerateDataSourceImplementationStep with real file structure") {
        When("execute is called for combined datasource") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "User",
                includeDatasource = true,
                datasourceCombined = true,
                datasourceRemote = false,
                datasourceLocal = false,
                diStrategy = DiStrategy.Hilt
            )
            val result = step.execute(params)

            Then("it should call generateImplementation for combined source") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 1) {
                    scaffoldRepo.generateImplementation(
                        directory = dataSourceDir.resolve("src/main/kotlin/com/example/datasource"),
                        packageName = "com.example.datasource",
                        className = "UserDataSourceImpl",
                        interfacePackage = "com.example.data.dataSource",
                        interfaceName = "UserDataSource",
                        useKoin = false
                    )
                }
            }
        }

        When("execute is called for remote and local datasources") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "User",
                includeDatasource = true,
                datasourceCombined = false,
                datasourceRemote = true,
                datasourceLocal = true,
                diStrategy = DiStrategy.Koin(false)
            )
            val result = step.execute(params)

            Then("it should call generateImplementation for remote and local sources") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 1) {
                    scaffoldRepo.generateImplementation(
                        directory = remoteDataSourceDir.resolve("src/main/kotlin/com/example/remote"),
                        packageName = "com.example.remote",
                        className = "UserRemoteDataSourceImpl",
                        interfacePackage = "com.example.data.remoteDataSource",
                        interfaceName = "UserRemoteDataSource",
                        useKoin = true
                    )
                }
                coVerify(exactly = 1) {
                    scaffoldRepo.generateImplementation(
                        directory = localDataSourceDir.resolve("src/main/kotlin/com/example/local"),
                        packageName = "com.example.local",
                        className = "UserLocalDataSourceImpl",
                        interfacePackage = "com.example.data.localDataSource",
                        interfaceName = "UserLocalDataSource",
                        useKoin = true
                    )
                }
            }
        }

        When("includeDatasource is false") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "User",
                includeDatasource = false,
                datasourceCombined = false,
                datasourceRemote = false,
                datasourceLocal = false,
                diStrategy = DiStrategy.Hilt
            )
            val result = step.execute(params)

            Then("it should do nothing") {
                result.shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 0) { scaffoldRepo.generateImplementation(any(), any(), any(), any(), any(), any()) }
            }
        }
    }
})
