package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.readText

class ScreenStateHolderRepositoryImplTest : BehaviorSpec({

    val tempDir = Files.createTempDirectory("screenstate_gen_test")

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    Given("a ScreenStateHolder repository") {
        val fs = FileSystemRepositoryImpl()
        val repo = ScreenStateHolderRepositoryImpl(fs)

        When("generating ScreenStateHolder") {
            val result = repo.generateScreenStateHolder(tempDir, "com.example", "Test")

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                val content = result.path.readText()
                content shouldContain "internal class TestStateHolder"
                content shouldContain "@Composable"
                content shouldContain "internal fun rememberTestStateHolder"
                content shouldContain "@Stable"
            }
        }
    }
})
