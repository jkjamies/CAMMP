package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.factory.ScreenSpecFactory
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
 * Test for [ScreenRepositoryImpl].
 */
class ScreenRepositoryImplTest : BehaviorSpec({

    val specFactory = mockk<ScreenSpecFactory>()
    val repository = ScreenRepositoryImpl(specFactory)

    Given("ScreenRepositoryImpl") {
        val tempDir = Files.createTempDirectory("screen_repo_test")
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

        When("generating Screen") {
            every { specFactory.create(any(), any(), any(), any()) } returns FileSpec.builder(packageName, "Test")
                .addType(TypeSpec.classBuilder("Test").build())
                .build()

            val result = repository.generateScreen(tempDir, packageName, params)

            Then("it should write file") {
                result.status.name shouldBe "CREATED"
                result.path.exists() shouldBe true
            }
        }
    }
})
