package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.readText

class IntentRepositoryImplTest : BehaviorSpec({

    val tempDir = Files.createTempDirectory("intent_gen_test")

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    Given("an Intent repository") {
        val fs = FileSystemRepositoryImpl()
        val repo = IntentRepositoryImpl(fs)

        When("generating Intent") {
            val result = repo.generateIntent(tempDir, "com.example", "Test")

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                val content = result.path.readText()
                content shouldContain "sealed interface TestIntent"
                content shouldContain "object NoOp"
            }
        }
    }
})
