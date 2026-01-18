package com.jkjamies.cammp.feature.cleanarchitecture.domain.step

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.BuildLogicScaffoldRepositoryFake
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Tests for [EnsureBuildLogicStep].
 */
class EnsureBuildLogicStepTest : BehaviorSpec({

    Given("EnsureBuildLogicStep") {
        val params = CleanArchitectureParams(
            projectBasePath = Path.of("/project"),
            root = "app",
            feature = "my-feature",
            orgCenter = "com.example",
            includePresentation = false,
            includeDiModule = true,
            datasourceStrategy = DatasourceStrategy.RemoteAndLocal,
            diStrategy = DiStrategy.Hilt,
        )

        When("executed") {
            val repo = BuildLogicScaffoldRepositoryFake(updated = false)
            val step = EnsureBuildLogicStep(repo)

            val result = step.execute(params)

            Then("it should return StepResult.BuildLogic reflecting updated=false") {
                result shouldBe StepResult.BuildLogic(updated = false, message = "- build-logic updated: false")
            }

            Then("it should compute enabled module list") {
                repo.calls.single().enabledModules.shouldContainExactly(
                    "domain",
                    "data",
                    "di",
                    "remoteDataSource",
                    "localDataSource",
                )
            }

            Then("it should compute di mode") {
                repo.calls.single().diMode shouldBe DiMode.HILT
            }
        }

        When("executed with includeApiModule = true") {
            val repo = BuildLogicScaffoldRepositoryFake(updated = false)
            val step = EnsureBuildLogicStep(repo)

            step.execute(params.copy(includeApiModule = true))

            Then("it should compute enabled module list with api") {
                repo.calls.single().enabledModules.shouldContainExactly(
                    "domain",
                    "data",
                    "api",
                    "di",
                    "remoteDataSource",
                    "localDataSource",
                )
            }
        }

        When("executed with includeDiModule = false") {
            val repo = BuildLogicScaffoldRepositoryFake(updated = false)
            val step = EnsureBuildLogicStep(repo)

            step.execute(params.copy(includeDiModule = false))

            Then("it should compute enabled module list without di") {
                repo.calls.single().enabledModules.shouldContainExactly(
                    "domain",
                    "data",
                    "remoteDataSource",
                    "localDataSource",
                )
            }
        }
    }
})
