package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.readText

class UiStateRepositoryImplTest : BehaviorSpec({

    val tempDir = Files.createTempDirectory("uistate_gen_test")

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    Given("a UiState repository") {
        val repo = UiStateRepositoryImpl()

        When("generating UiState") {
            val result = repo.generateUiState(tempDir, "com.example", "Test")

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                val content = result.path.readText()
                content shouldContain "data class TestUiState"
                content shouldContain "val isLoading: Boolean"
            }
        }
    }
})
