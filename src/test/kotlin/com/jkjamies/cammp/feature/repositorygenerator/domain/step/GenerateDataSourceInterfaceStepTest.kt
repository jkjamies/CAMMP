package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Paths

class GenerateDataSourceInterfaceStepTest : BehaviorSpec({

    val modulePkgRepo = mockk<ModulePackageRepository>()
    val scaffoldRepo = mockk<DatasourceScaffoldRepository>()
    val step = GenerateDataSourceInterfaceStep(modulePkgRepo, scaffoldRepo)

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("GenerateDataSourceInterfaceStep") {
        val root = Paths.get("/project/feature")
        val dataDir = root.resolve("data")

        val params = RepositoryParams(
            dataDir = dataDir,
            className = "User",
            includeDatasource = true,
            datasourceCombined = true,
            datasourceRemote = false,
            datasourceLocal = false,
            diStrategy = DiStrategy.Hilt
        )

        When("execute is called") {
            every { modulePkgRepo.findModulePackage(dataDir) } returns "com.example.data"
            coEvery { scaffoldRepo.generateInterface(any(), any(), any()) } returns dataDir.resolve("UserDataSource.kt")

            val result = step.execute(params)
            // IO independent logic should work
        }
    }
})
