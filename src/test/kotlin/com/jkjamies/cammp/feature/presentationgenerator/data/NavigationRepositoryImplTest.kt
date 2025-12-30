package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.readText
import kotlin.io.path.writeText

class NavigationRepositoryImplTest : BehaviorSpec({

    val tempDir = Files.createTempDirectory("nav_gen_test")

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    Given("a Navigation repository") {
        val repo = NavigationRepositoryImpl()

        When("generating NavigationHost") {
            val result = repo.generateNavigationHost(tempDir, "com.example", "TestNavHost")

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                val content = result.path.readText()
                content shouldContain "fun TestNavHost"
                content shouldContain "NavHost(navController"
            }
        }

        When("generating NavigationHost that already exists") {
            val existingFile = tempDir.resolve("ExistingNavHost.kt")
            existingFile.writeText("// Existing content")
            
            val result = repo.generateNavigationHost(tempDir, "com.example", "ExistingNavHost")

            Then("it should skip generation") {
                result.status shouldBe GenerationStatus.SKIPPED
                result.path.readText() shouldBe "// Existing content"
            }
        }

        When("generating Destination") {
            val result = repo.generateDestination(tempDir, "com.example", "TestScreen", "testscreen")

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                val content = result.path.readText()
                content shouldContain "object TestScreenDestination"
                content shouldContain "fun NavGraphBuilder.testScreen"
                content shouldContain "composable<TestScreenDestination>"
            }
        }

        When("generating Destination that already exists") {
            val existingFile = tempDir.resolve("ExistingScreenDestination.kt")
            existingFile.writeText("// Existing destination content")

            val result = repo.generateDestination(tempDir, "com.example", "ExistingScreen", "existingscreen")

            Then("it should skip generation") {
                result.status shouldBe GenerationStatus.SKIPPED
                result.path.readText() shouldBe "// Existing destination content"
            }
        }
    }
})
