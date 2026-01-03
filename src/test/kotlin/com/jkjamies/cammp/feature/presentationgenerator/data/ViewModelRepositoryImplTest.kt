package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.factory.ViewModelSpecFactory
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
import kotlin.io.path.readText

/**
 * Test for [ViewModelRepositoryImpl].
 */
class ViewModelRepositoryImplTest : BehaviorSpec({

    val specFactory = mockk<ViewModelSpecFactory>()
    val repository = ViewModelRepositoryImpl(specFactory)

    Given("ViewModelRepositoryImpl") {
        val tempDir = Files.createTempDirectory("viewmodel_repo_test")
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

        When("generating ViewModel (New File)") {
            every { specFactory.create(any(), any()) } returns FileSpec.builder(packageName, "TestViewModel")
                .addType(TypeSpec.classBuilder("TestViewModel").build())
                .build()

            val result = repository.generateViewModel(tempDir, packageName, params)

            Then("it should write file") {
                result.status.name shouldBe "CREATED"
                result.path.exists() shouldBe true
                result.path.readText() shouldBe FileSpec.builder(packageName, "TestViewModel")
                    .addType(TypeSpec.classBuilder("TestViewModel").build())
                    .build().toString()
            }
        }

        When("generating ViewModel (Existing File)") {
            tempDir.resolve("TestViewModel.kt")
            
            val result = repository.generateViewModel(tempDir, packageName, params)

            Then("it should skip") {
                result.status.name shouldBe "SKIPPED"
            }
        }
    }
})
