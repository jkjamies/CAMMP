package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.factory.IntentSpecFactory
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
 * Test for [IntentRepositoryImpl].
 */
class IntentRepositoryImplTest : BehaviorSpec({

    val specFactory = mockk<IntentSpecFactory>()
    val repository = IntentRepositoryImpl(specFactory)

    Given("IntentRepositoryImpl") {
        val tempDir = Files.createTempDirectory("intent_repo_test")
        val packageName = "com.example"
        val params = PresentationParams(
            moduleDir = tempDir,
            screenName = "Test",
            patternStrategy = PresentationPatternStrategy.MVI,
            diStrategy = DiStrategy.Hilt
        )

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("generating Intent") {
            every { specFactory.create(any(), any()) } returns FileSpec.builder(packageName, "TestIntent")
                .addType(TypeSpec.classBuilder("TestIntent").build())
                .build()

            val result = repository.generateIntent(tempDir, packageName, params)

            Then("it should write file") {
                result.status.name shouldBe "CREATED"
                result.path.exists() shouldBe true
            }
        }
    }
})
