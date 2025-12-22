package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import java.nio.file.Files
import kotlin.io.path.readText

class NavigationRepositoryImplTest : BehaviorSpec({

    val tempDir = Files.createTempDirectory("nav_gen_test")

    afterSpec {
        tempDir.toFile().deleteRecursively()
        clearAllMocks()
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

        When("generating Destination") {
            val result = repo.generateDestination(tempDir, "com.example", "TestScreen", "testscreen")

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                val content = result.path.readText()
                content shouldContain "object TestScreenDestination"
                content shouldContain "fun NavGraphBuilder.TestScreen"
                content shouldContain "composable<TestScreenDestination>"
            }
        }
    }
})
