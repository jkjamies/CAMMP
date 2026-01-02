package com.jkjamies.cammp.feature.repositorygenerator.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.exists
import kotlin.io.path.readText

class DatasourceScaffoldRepositoryImplTest : BehaviorSpec({

    val repository = DatasourceScaffoldRepositoryImpl()

    Given("DatasourceScaffoldRepositoryImpl") {
        val tempDir = Files.createTempDirectory("scaffold_repo_test")

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("generating interface") {
            val packageName = "com.example.data.datasource"
            val className = "UserDataSource"
            val result = repository.generateInterface(tempDir, packageName, className)

            Then("it should create file with interface") {
                result.exists() shouldBe true
                val content = result.readText()
                content shouldContain "package com.example.data.datasource"
                content shouldContain "interface UserDataSource"
            }
        }

        When("generating implementation (Hilt)") {
            val packageName = "com.example.datasource"
            val className = "UserDataSourceImpl"
            val interfacePackage = "com.example.data.datasource"
            val interfaceName = "UserDataSource"
            
            val result = repository.generateImplementation(
                directory = tempDir,
                packageName = packageName,
                className = className,
                interfacePackage = interfacePackage,
                interfaceName = interfaceName,
                useKoin = false
            )

            Then("it should create class with @Inject") {
                result.exists() shouldBe true
                val content = result.readText()
                content shouldContain "package com.example.datasource"
                content shouldContain "class UserDataSourceImpl"
                content shouldContain "@Inject constructor"
                content shouldContain ": UserDataSource"
            }
        }

        When("generating implementation (Koin)") {
            val packageName = "com.example.datasource"
            val className = "UserDataSourceImplKoin"
            val interfacePackage = "com.example.data.datasource"
            val interfaceName = "UserDataSource"
            
            val result = repository.generateImplementation(
                directory = tempDir,
                packageName = packageName,
                className = className,
                interfacePackage = interfacePackage,
                interfaceName = interfaceName,
                useKoin = true
            )

            Then("it should create class without @Inject") {
                result.exists() shouldBe true
                val content = result.readText()
                content shouldContain "class UserDataSourceImplKoin"
                // Should NOT contain @Inject
                content.contains("@Inject") shouldBe false
            }
        }
    }
})
