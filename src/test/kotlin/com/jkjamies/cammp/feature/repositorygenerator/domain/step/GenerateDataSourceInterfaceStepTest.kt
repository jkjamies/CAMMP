package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.types.shouldBeInstanceOf
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Paths

/**
 * Tests for [GenerateDataSourceInterfaceStep].
 */
class GenerateDataSourceInterfaceStepTest : BehaviorSpec({

    Given("GenerateDataSourceInterfaceStep") {
        val root = Paths.get("/project/feature")
        val dataDir = root.resolve("data")

        fun params(
            include: Boolean = true,
            combined: Boolean = true,
            remote: Boolean = false,
            local: Boolean = false,
        ) = RepositoryParams(
            dataDir = dataDir,
            className = "User",
            includeDatasource = include,
            datasourceCombined = combined,
            datasourceRemote = remote,
            datasourceLocal = local,
            diStrategy = DiStrategy.Hilt,
        )

        When("includeDatasource=false") {
            Then("it returns Success and does not call scaffold") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val scaffoldRepo = mockk<DatasourceScaffoldRepository>()
                val step = GenerateDataSourceInterfaceStep(modulePkgRepo, scaffoldRepo)

                step.execute(params(include = false)).shouldBeInstanceOf<StepResult.Success>()
                coVerify(exactly = 0) { scaffoldRepo.generateInterface(any(), any(), any()) }
            }
        }

        When("combined datasource is enabled") {
            Then("it generates one interface in <base>.data.dataSource") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val scaffoldRepo = mockk<DatasourceScaffoldRepository>()
                val step = GenerateDataSourceInterfaceStep(modulePkgRepo, scaffoldRepo)

                every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.feature.data"

                val expectedPkg = "com.example.feature.data.dataSource"
                val expectedDir = dataDir.resolve("src/main/kotlin").resolve("com/example/feature/data/dataSource")
                val expectedClass = "UserDataSource"

                coEvery { scaffoldRepo.generateInterface(expectedDir, expectedPkg, expectedClass) } returns expectedDir.resolve("$expectedClass.kt")

                step.execute(params()).shouldBeInstanceOf<StepResult.Success>()

                coVerify(exactly = 1) {
                    scaffoldRepo.generateInterface(
                        directory = expectedDir,
                        packageName = expectedPkg,
                        className = expectedClass,
                    )
                }
            }
        }

        When("remote+local datasource is enabled") {
            Then("it generates both interfaces in their respective subpackages") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val scaffoldRepo = mockk<DatasourceScaffoldRepository>()
                val step = GenerateDataSourceInterfaceStep(modulePkgRepo, scaffoldRepo)

                every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.feature.data"

                val base = "com.example.feature.data"

                val remotePkg = "$base.remoteDataSource"
                val remoteDir = dataDir.resolve("src/main/kotlin").resolve("com/example/feature/data/remoteDataSource")
                val remoteClass = "UserRemoteDataSource"

                val localPkg = "$base.localDataSource"
                val localDir = dataDir.resolve("src/main/kotlin").resolve("com/example/feature/data/localDataSource")
                val localClass = "UserLocalDataSource"

                coEvery { scaffoldRepo.generateInterface(remoteDir, remotePkg, remoteClass) } returns remoteDir.resolve("$remoteClass.kt")
                coEvery { scaffoldRepo.generateInterface(localDir, localPkg, localClass) } returns localDir.resolve("$localClass.kt")

                step.execute(params(combined = false, remote = true, local = true)).shouldBeInstanceOf<StepResult.Success>()

                coVerify(exactly = 1) { scaffoldRepo.generateInterface(remoteDir, remotePkg, remoteClass) }
                coVerify(exactly = 1) { scaffoldRepo.generateInterface(localDir, localPkg, localClass) }
            }
        }

        When("scaffold repo throws") {
            Then("it returns Failure") {
                val modulePkgRepo = mockk<ModulePackageRepository>()
                val scaffoldRepo = mockk<DatasourceScaffoldRepository>()
                val step = GenerateDataSourceInterfaceStep(modulePkgRepo, scaffoldRepo)

                every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.feature.data"

                val expectedPkg = "com.example.feature.data.dataSource"
                val expectedDir = dataDir.resolve("src/main/kotlin").resolve("com/example/feature/data/dataSource")
                val expectedClass = "UserDataSource"

                coEvery { scaffoldRepo.generateInterface(expectedDir, expectedPkg, expectedClass) } throws IllegalStateException("boom")

                step.execute(params()).shouldBeInstanceOf<StepResult.Failure>()
            }
        }
    }
})
