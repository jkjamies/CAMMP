package com.jkjamies.cammp.feature.cleanarchitecture.data.factory

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import java.nio.file.Path

/**
 * Tests for [ModuleBuildGradleSpecFactoryImpl].
 */
class ModuleBuildGradleSpecFactoryImplTest : BehaviorSpec({

    Given("ModuleBuildGradleSpecFactoryImpl") {
        val factory = ModuleBuildGradleSpecFactoryImpl()

        val params = CleanArchitectureParams(
            projectBasePath = Path.of("/project"),
            root = "feature",
            feature = "profile",
            orgCenter = "com.Example-Org", // exercise sanitization
            includePresentation = true,
            includeDiModule = true,
            datasourceStrategy = DatasourceStrategy.None,
            diStrategy = DiStrategy.Hilt,
        )

        When("creating a data module spec") {
            val out = factory.create(
                params = params,
                moduleName = "data",
                featureName = "profile",
                enabledModules = listOf("domain", "data", "di", "presentation"),
                rawTemplate = "namespace='${'$'}{NAMESPACE}'\n${'$'}{DEPENDENCIES}",
            )

            Then("it should replace namespace") {
                out.shouldContain("namespace='com.exampleOrg.feature.profile.data'")
            }

            Then("it should include a dependency on domain") {
                out.shouldContain("implementation(project(\":feature:profile:domain\"))")
            }
        }

        When("creating a di module spec") {
            val out = factory.create(
                params = params,
                moduleName = "di",
                featureName = "profile",
                enabledModules = listOf("domain", "data", "di", "presentation"),
                rawTemplate = "${'$'}{DEPENDENCIES}",
            )

            Then("it should include dependencies on all enabled modules except di") {
                out.shouldContain("implementation(project(\":feature:profile:domain\"))")
                out.shouldContain("implementation(project(\":feature:profile:data\"))")
                out.shouldContain("implementation(project(\":feature:profile:presentation\"))")
            }
        }
    }
})
