package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.data.factory.BuildLogicSpecFactoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.AliasesRepositoryWritingFake
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.ConventionPluginRepositoryWritingFake
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Tests for [BuildLogicScaffoldRepositoryImpl].
 */
class BuildLogicScaffoldRepositoryImplTest : BehaviorSpec({

    fun params(base: Path) = CleanArchitectureParams(
        projectBasePath = base,
        root = "feature",
        feature = "profile",
        orgCenter = "com.example",
        includePresentation = true,
        datasourceStrategy = DatasourceStrategy.RemoteAndLocal,
        diStrategy = DiStrategy.Hilt,
    )

    Given("BuildLogicScaffoldRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()
        val templateRepo = TemplateRepositoryImpl()
        val buildLogicSpecFactory = BuildLogicSpecFactoryImpl()

        When("build-logic directory does not exist") {
            Then("it should scaffold baseline structure and report changed=true") {
                withTempDir("cammp_buildlogic") { tmp: Path ->
                    val repo = BuildLogicScaffoldRepositoryImpl(
                        fs = fs,
                        templateRepo = templateRepo,
                        aliasesRepo = AliasesRepositoryWritingFake(fs),
                        conventionPluginRepo = ConventionPluginRepositoryWritingFake(fs),
                        buildLogicSpecFactory = buildLogicSpecFactory,
                    )

                    val changed = repo.ensureBuildLogic(
                        params = params(tmp),
                        enabledModules = listOf("domain", "data", "di", "presentation", "remoteDataSource", "localDataSource"),
                        diMode = DiMode.HILT,
                    )

                    changed shouldBe true

                    // Baseline files
                    fs.exists(tmp.resolve("build-logic/settings.gradle.kts")) shouldBe true
                    fs.exists(tmp.resolve("build-logic/build.gradle.kts")) shouldBe true

                    // Helpers + core
                    fs.exists(tmp.resolve("build-logic/src/main/kotlin/com/example/convention/helpers/AndroidLibraryDefaults.kt")) shouldBe true
                    fs.exists(tmp.resolve("build-logic/src/main/kotlin/com/example/convention/helpers/StandardTestDependencies.kt")) shouldBe true
                    fs.exists(tmp.resolve("build-logic/src/main/kotlin/com/example/convention/helpers/TestOptions.kt")) shouldBe true
                    fs.exists(tmp.resolve("build-logic/src/main/kotlin/com/example/convention/core/Dependencies.kt")) shouldBe true

                    // We don't assert on Aliases.kt / plugin outputs here because those are delegated to other repos.
                }
            }
        }

        When("running ensureBuildLogic twice") {
            Then("it should be repeatable (second run does not throw)") {
                withTempDir("cammp_buildlogic2") { tmp: Path ->
                    val repo = BuildLogicScaffoldRepositoryImpl(
                        fs = fs,
                        templateRepo = templateRepo,
                        aliasesRepo = AliasesRepositoryWritingFake(fs),
                        conventionPluginRepo = ConventionPluginRepositoryWritingFake(fs),
                        buildLogicSpecFactory = buildLogicSpecFactory,
                    )

                    val enabled = listOf("domain", "data", "di")

                    // First run should create the baseline structure.
                    repo.ensureBuildLogic(params(tmp), enabled, DiMode.HILT) shouldBe true

                    val settings = tmp.resolve("build-logic/settings.gradle.kts")
                    val build = tmp.resolve("build-logic/build.gradle.kts")
                    fs.exists(settings) shouldBe true
                    fs.exists(build) shouldBe true

                    val settingsContent = fs.readText(settings)
                    val buildContent = fs.readText(build)

                    // Second run should not overwrite existing files.
                    repo.ensureBuildLogic(params(tmp), enabled, DiMode.HILT) shouldBe false

                    fs.readText(settings) shouldBe settingsContent
                    fs.readText(build) shouldBe buildContent
                }
            }
        }
    }
})
