package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.factory.FlowStateHolderSpecFactory
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationPatternStrategy
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Files
import kotlin.io.path.exists

/**
 * Test for [FlowStateHolderRepositoryImpl].
 */
class FlowStateHolderRepositoryImplTest : BehaviorSpec({

    val specFactory = mockk<FlowStateHolderSpecFactory>()
    val repository = FlowStateHolderRepositoryImpl(specFactory)

    Given("FlowStateHolderRepositoryImpl") {
        val tempDir = Files.createTempDirectory("flow_repo_test")
        val packageName = "com.example"
        val params = PresentationParams(
            moduleDir = tempDir,
            screenName = "Test",
            patternStrategy = PresentationPatternStrategy.MVVM,
            diStrategy = DiStrategy.Hilt
        )

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("generating FlowStateHolder") {
            every { specFactory.create(any(), any()) } returns FileSpec.builder(packageName, "TestFlow")
                .addType(TypeSpec.classBuilder("TestFlow").build())
                .build()

            val result = repository.generateFlowStateHolder(tempDir, packageName, "TestFlow", params)

            Then("it should write file") {
                result.status.name shouldBe "CREATED"
                result.path.exists() shouldBe true
            }
        }
    }
})
