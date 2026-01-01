package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.readText

class FlowStateHolderRepositoryImplTest : BehaviorSpec({

    val tempDir = Files.createTempDirectory("flowstate_gen_test")

    afterSpec {
        tempDir.toFile().deleteRecursively()
    }

    Given("a FlowStateHolder repository") {
        val repo = FlowStateHolderRepositoryImpl()

        When("generating FlowStateHolder") {
            val result = repo.generateFlowStateHolder(tempDir, "com.example", "TestFlowStateHolder")

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                val content = result.path.readText()
                content shouldContain "internal class TestFlowStateHolder"
                content shouldContain "@Composable"
                content shouldContain "internal fun rememberTestFlowStateHolder"
                content shouldContain "navController: NavHostController"
                content shouldContain "@Stable"
            }
        }
    }
})
