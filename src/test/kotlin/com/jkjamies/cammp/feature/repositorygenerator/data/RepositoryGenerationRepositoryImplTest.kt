package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.readText

class RepositoryGenerationRepositoryImplTest : BehaviorSpec({

    Given("a repository generation repository") {
        val repository = RepositoryGenerationRepositoryImpl()
        val tempDir = Files.createTempDirectory("repo_gen_test")
        val dataDir = tempDir.resolve("data")
        val domainDir = tempDir.resolve("domain")

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("generating domain layer") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "TestRepository",
                includeDatasource = false,
                datasourceCombined = false,
                datasourceRemote = false,
                datasourceLocal = false,
                useKoin = false,
                koinAnnotations = false,
                selectedDataSources = emptyList()
            )
            val packageName = "com.example.domain.repository"
            
            val resultPath = repository.generateDomainLayer(params, packageName, domainDir)

            Then("it should create the file") {
                Files.exists(resultPath)
            }
            Then("it should contain the interface definition") {
                val content = resultPath.readText()
                content shouldContain "package com.example.domain.repository"
                content shouldContain "interface TestRepository"
            }
        }

        When("generating data layer with Hilt") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "TestRepository",
                includeDatasource = false,
                datasourceCombined = false,
                datasourceRemote = false,
                datasourceLocal = false,
                useKoin = false,
                koinAnnotations = false,
                selectedDataSources = emptyList()
            )
            val dataPackage = "com.example.data.repository"
            val domainPackage = "com.example.domain.repository"

            val resultPath = repository.generateDataLayer(params, dataPackage, domainPackage)

            Then("it should create the file") {
                Files.exists(resultPath)
            }
            Then("it should contain the implementation definition") {
                val content = resultPath.readText()
                content shouldContain "package com.example.data.repository"
                content shouldContain "class TestRepositoryImpl"
                content shouldContain "TestRepository"
                content shouldContain "@Inject"
            }
        }

        When("generating data layer with Koin Annotations") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "KoinRepository",
                includeDatasource = false,
                datasourceCombined = false,
                datasourceRemote = false,
                datasourceLocal = false,
                useKoin = true,
                koinAnnotations = true,
                selectedDataSources = emptyList()
            )
            val dataPackage = "com.example.data.repository"
            val domainPackage = "com.example.domain.repository"

            val resultPath = repository.generateDataLayer(params, dataPackage, domainPackage)

            Then("it should contain the Single annotation") {
                val content = resultPath.readText()
                content shouldContain "@Single"
                content shouldContain "import org.koin.core.annotation.Single"
            }
        }

        When("generating data layer with datasources") {
            val params = RepositoryParams(
                dataDir = dataDir,
                className = "DsRepository",
                includeDatasource = true,
                datasourceCombined = false,
                datasourceRemote = true,
                datasourceLocal = true,
                useKoin = false,
                koinAnnotations = false,
                selectedDataSources = emptyList()
            )
            val dataPackage = "com.example.data.repository"
            val domainPackage = "com.example.domain.repository"

            val resultPath = repository.generateDataLayer(params, dataPackage, domainPackage)

            Then("it should inject datasources") {
                val content = resultPath.readText()
                content shouldContain "dsRemoteDataSource: DsRemoteDataSource"
                content shouldContain "dsLocalDataSource: DsLocalDataSource"
            }
        }
    }
})
