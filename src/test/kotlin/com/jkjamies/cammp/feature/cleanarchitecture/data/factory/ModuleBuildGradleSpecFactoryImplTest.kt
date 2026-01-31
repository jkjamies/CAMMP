/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.cleanarchitecture.data.factory

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
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

        When("creating a presentation module spec with API module enabled") {
            val out = factory.create(
                params = params.copy(includeApiModule = true),
                moduleName = "presentation",
                featureName = "profile",
                enabledModules = listOf("domain", "data", "api", "di", "presentation"),
                rawTemplate = "${'$'}{DEPENDENCIES}",
            )

            Then("it should include a dependency on api instead of domain") {
                out.shouldContain("implementation(project(\":feature:profile:api\"))")
                out.shouldNotContain("implementation(project(\":feature:profile:domain\"))")
            }
        }

        When("creating a domain module spec with API module enabled") {
            val out = factory.create(
                params = params.copy(includeApiModule = true),
                moduleName = "domain",
                featureName = "profile",
                enabledModules = listOf("domain", "data", "api", "di", "presentation"),
                rawTemplate = "${'$'}{DEPENDENCIES}",
            )

            Then("it should include a dependency on api") {
                out.shouldContain("implementation(project(\":feature:profile:api\"))")
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
