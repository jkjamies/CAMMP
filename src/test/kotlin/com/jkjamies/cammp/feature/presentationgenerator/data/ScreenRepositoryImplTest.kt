package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.clearAllMocks
import java.nio.file.Files
import kotlin.io.path.readText

class ScreenRepositoryImplTest : BehaviorSpec({

    Given("a Screen repository") {
        val repository = ScreenRepositoryImpl()
        val tempDir = Files.createTempDirectory("screen_gen_test")

        afterSpec {
            tempDir.toFile().deleteRecursively()
            clearAllMocks()
        }

        When("generating a basic Screen") {
            val packageName = "com.example.test"
            val screenName = "TestScreen"
            
            val result = repository.generateScreen(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = false,
                diKoin = false
            )

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                Files.exists(result.path) shouldBe true
            }

            Then("it should contain the Composable") {
                val content = result.path.readText()
                content shouldContain "@Composable"
                content shouldContain "internal fun TestScreen("
            }

            Then("it should use koinViewModel by default") {
                val content = result.path.readText()
                content shouldContain "viewModel: TestScreenViewModel = koinViewModel()"
            }
        }

        When("generating a Hilt Screen") {
            val packageName = "com.example.test"
            val screenName = "HiltScreen"
            
            val result = repository.generateScreen(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = true,
                diKoin = false
            )

            Then("it should use hiltViewModel") {
                val content = result.path.readText()
                content shouldContain "viewModel: HiltScreenViewModel = hiltViewModel()"
                content shouldContain "import androidx.hilt.navigation.compose.hiltViewModel"
            }
        }

        When("generating a Koin Screen") {
            val packageName = "com.example.test"
            val screenName = "KoinScreen"
            
            val result = repository.generateScreen(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = false,
                diKoin = true
            )

            Then("it should use koinViewModel") {
                val content = result.path.readText()
                content shouldContain "viewModel: KoinScreenViewModel = koinViewModel()"
                content shouldContain "import org.koin.compose.viewmodel.koinViewModel"
            }
        }
    }
})
