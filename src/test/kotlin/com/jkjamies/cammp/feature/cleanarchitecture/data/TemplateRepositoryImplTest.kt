package com.jkjamies.cammp.feature.cleanarchitecture.data

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * Test class for [TemplateRepositoryImpl].
 */
class TemplateRepositoryImplTest : BehaviorSpec({

    Given("a TemplateRepositoryImpl") {
        val repository = TemplateRepositoryImpl()

        When("getting a template text") {

            Then("it should return content for existing template") {
                // Using a known template path from the project structure
                // Since this is a unit test running in the same module,
                // it should have access to main resources
                val content = repository.getTemplateText("templates/cleanArchitecture/buildLogic/build.gradle.kts")
                content.isNotEmpty() shouldBe true
            }

            Then("it should throw error if template not found") {
                val exception = shouldThrow<IllegalStateException> {
                    repository.getTemplateText("non/existent/template.txt")
                }
                exception.message shouldContain "Template not found"
            }
        }
    }
})
