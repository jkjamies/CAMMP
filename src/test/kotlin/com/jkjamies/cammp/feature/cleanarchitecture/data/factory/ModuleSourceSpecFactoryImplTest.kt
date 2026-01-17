package com.jkjamies.cammp.feature.cleanarchitecture.data.factory

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Path

/**
 * Tests for [ModuleSourceSpecFactoryImpl].
 */
class ModuleSourceSpecFactoryImplTest : BehaviorSpec({

    Given("ModuleSourceSpecFactoryImpl") {
        val factory = ModuleSourceSpecFactoryImpl()

        val params = CleanArchitectureParams(
            projectBasePath = Path.of("/project"),
            root = "feature",
            feature = "profile",
            orgCenter = "com.example",
            includePresentation = true,
            includeDiModule = true,
            datasourceStrategy = DatasourceStrategy.None,
        )

        When("creating a module package name") {
            val pkg = factory.packageName(params, moduleName = "domain", featureName = "profile")

            Then("it should be deterministic") {
                pkg shouldBe "com.example.feature.profile.domain"
            }
        }

        When("creating placeholder kotlin source") {
            val src = factory.placeholderKotlinFile(params, moduleName = "domain", featureName = "profile")

            Then("it should include package and placeholder type") {
                src.shouldContain("package com.example.feature.profile.domain")
                src.shouldContain("class Placeholder")
            }
        }
    }
})
