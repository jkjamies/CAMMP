package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldBe
import java.nio.file.Files

/**
 * Tests for [ConventionPluginRepositoryImpl].
 */
class ConventionPluginRepositoryImplTest : BehaviorSpec({

    Given("ConventionPluginRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()
        val repo = ConventionPluginRepositoryImpl(fs)

        When("generating presentation plugin with Hilt") {
            Then("it should include compose + hilt navigation dependencies") {
                withTempDir("cammp_convention") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.convention",
                        diMode = DiMode.HILT,
                        type = PluginType.PRESENTATION,
                    )

                    val out = tmp.resolve("PresentationConventionPlugin.kt")
                    fs.readText(out)!!.also { content ->
                        content shouldContain "package com.example.convention"
                        content shouldContain "class PresentationConventionPlugin"
                        content shouldContain "compose = true"
                        // plugin application
                        content shouldContain "Aliases.PluginAliases.COMPOSE_COMPILER"
                        content shouldContain "Aliases.PluginAliases.HILT"
                        // deps
                        content shouldContain "LibsCompose.HILT_NAVIGATION"
                    }
                }
            }
        }

        When("generating data plugin with Koin") {
            Then("it should not include Hilt-specific parts") {
                withTempDir("cammp_convention2") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.convention",
                        diMode = DiMode.KOIN,
                        type = PluginType.DATA,
                    )

                    val out = tmp.resolve("DataConventionPlugin.kt")
                    fs.readText(out)!!.also { content ->
                        content shouldContain "class DataConventionPlugin"
                        // should apply base android + kotlin plugins
                        content shouldContain "Aliases.PluginAliases.ANDROID_LIBRARY"
                        content shouldContain "Aliases.PluginAliases.KOTLIN_ANDROID"
                        // should not apply hilt plugin
                        content.contains("Aliases.PluginAliases.HILT") shouldBe false
                        // should not include compose compiler
                        content.contains("Aliases.PluginAliases.COMPOSE_COMPILER") shouldBe false
                    }
                }
            }
        }

        When("generating remote datasource plugin") {
            Then("it should create the correct file name") {
                withTempDir("cammp_convention3") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.convention",
                        diMode = DiMode.HILT,
                        type = PluginType.REMOTE_DATA_SOURCE,
                    )

                    val out = tmp.resolve("RemoteDataSourceConventionPlugin.kt")
                    fs.readText(out)!!.also { content ->
                        content shouldContain "class RemoteDataSourceConventionPlugin"
                    }
                }
            }
        }
    }
})
