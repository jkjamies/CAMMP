package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.factory.NavigationSpecFactory
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
 * Test for [NavigationRepositoryImpl].
 */
class NavigationRepositoryImplTest : BehaviorSpec({

    val specFactory = mockk<NavigationSpecFactory>()
    val repository = NavigationRepositoryImpl(specFactory)

    Given("NavigationRepositoryImpl") {
        val tempDir = Files.createTempDirectory("nav_repo_test")
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

        When("generating Navigation Host") {
            every { specFactory.createHost(any(), any()) } returns FileSpec.builder(packageName, "NavHost")
                .addType(TypeSpec.classBuilder("NavHost").build())
                .build()

            val result = repository.generateNavigationHost(tempDir, packageName, "NavHost")

            Then("it should write file") {
                result.status.name shouldBe "CREATED"
                result.path.exists() shouldBe true
            }
        }

        When("generating Destination") {
            every { specFactory.createDestination(any(), any(), any()) } returns FileSpec.builder(packageName, "Dest")
                .addType(TypeSpec.classBuilder("Dest").build())
                .build()

            val result = repository.generateDestination(tempDir, packageName, params, "folder")

            Then("it should write file") {
                result.status.name shouldBe "CREATED"
                result.path.exists() shouldBe true
            }
        }
    }
})
