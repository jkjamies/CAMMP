package com.jkjamies.cammp.feature.cleanarchitecture.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf

/**
 * Tests for [TemplateRepositoryImpl].
 */
class TemplateRepositoryImplTest : BehaviorSpec({

    Given("TemplateRepositoryImpl") {
        val repo = TemplateRepositoryImpl()

        When("requesting an existing template") {
            Then("it should return the resource text") {
                // This resource is part of the plugin resources and should always exist.
                val content = repo.getTemplateText("templates/cleanArchitecture/module/domain.gradle.kts")
                content shouldContain "plugins"
            }
        }

        When("requesting a missing template") {
            Then("it should throw") {
                val err = runCatching { repo.getTemplateText("templates/does-not-exist.txt") }.exceptionOrNull()
                err.shouldBeInstanceOf<IllegalStateException>()
                err.message shouldBe "Template not found: templates/does-not-exist.txt"
            }
        }
    }
})
