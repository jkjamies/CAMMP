package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.fakes.FakeFileSystemRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path

/**
 * Test class for [AliasesRepositoryImpl].
 */
class AliasesRepositoryImplTest : BehaviorSpec({

    Given("an aliases repository") {
        val fakeFs = FakeFileSystemRepository()
        val repository = AliasesRepositoryImpl(fakeFs)
        val outputDir = Path.of("output")
        val packageName = "com.example.convention.core"

        fun assertCommonAliases(content: String) {
            // Operations
            content shouldContain "const val LIBS: String = \"libs\""
            content shouldContain "const val IMPLEMENTATION: String = \"implementation\""
            
            // Plugin Aliases
            content shouldContain "const val ANDROID_LIBRARY: String = \"android-library\""
            content shouldContain "const val KOTLIN_ANDROID: String = \"kotlin-android\""
            content shouldContain "const val PARCELIZE: String = \"parcelize\""
            content shouldContain "const val KOTLIN_SERIALIZATION: String = \"kotlin-serialization\""
            content shouldContain "const val COMPOSE_COMPILER: String = \"compose-compiler\""

            // Build Props
            content shouldContain "const val COMPILE_SDK: String = \"compileSdk\""
            content shouldContain "const val DEFAULT_COMPILE_SDK: Int = 35"

            // Libs Common
            content shouldContain "const val KOTLINX_SERIALIZATION: String = \"kotlinx-serialization\""
            content shouldContain "const val JSON: String = \"json\""
            content shouldContain "const val CORE_KTX: String = \"androidx-core-ktx\""

            // Compose
            content shouldContain "const val UI: String = \"compose-ui\""
            content shouldContain "const val MATERIAL3_ANDROID: String = \"compose-material3-android\""
            content shouldContain "const val TOOLING: String = \"compose-tooling\""

            // Coroutines
            content shouldContain "const val CORE: String = \"coroutines-core\""
            content shouldContain "const val ANDROID: String = \"coroutines-android\""

            // Unit Test
            content shouldContain "const val KOTEST_RUNNER: String = \"kotest-runner\""
            content shouldContain "const val MOCKK: String = \"mockk\""
            content shouldContain "const val JUNIT_VINTAGE_ENGINE: String = \"junit-vintage-engine\""

            // Android Test
            content shouldContain "const val ANDROIDX_TEST_RUNNER: String = \"androidx-test-runner\""
            content shouldContain "const val COMPOSE_UI_TEST: String = \"compose-ui-test\""
        }

        When("generating Aliases file with Hilt") {
            repository.generateAliases(outputDir, packageName, DiMode.HILT)
            val content = fakeFs.writtenFiles.values.first()

            Then("it should contain common aliases") {
                assertCommonAliases(content)
            }

            Then("it should generate Hilt and KSP aliases") {
                content shouldContain "const val HILT: String = \"hilt\""
                content shouldContain "const val HILT_COMPILER: String = \"hilt-compiler\""
                content shouldContain "const val HILT_NAVIGATION: String = \"compose-hilt-navigation\""
                content shouldContain "const val KSP: String = \"ksp\""
            }

            Then("it should NOT generate Koin aliases") {
                content shouldNotContain "const val KOIN: String = \"koin\""
                content shouldNotContain "const val KOIN_CORE: String = \"koin-core\""
                content shouldNotContain "const val KOIN_ANNOTATIONS: String = \"koin-annotations\""
                content shouldNotContain "const val KOIN_KSP_COMPILER: String = \"koin-ksp-compiler\""
                content shouldNotContain "const val KOIN_NAVIGATION: String = \"compose-koin-navigation\""
            }
        }

        When("generating Aliases file with Koin without Annotations") {
            repository.generateAliases(outputDir, packageName, DiMode.KOIN)
            val content = fakeFs.writtenFiles.values.first()

            Then("it should contain common aliases") {
                assertCommonAliases(content)
            }

            Then("it should generate Koin aliases") {
                content shouldContain "const val KOIN: String = \"koin\""
                content shouldContain "const val KOIN_NAVIGATION: String = \"compose-koin-navigation\""
            }

            Then("it should NOT generate Hilt or KSP aliases") {
                content shouldNotContain "const val HILT: String = \"hilt\""
                content shouldNotContain "const val HILT_COMPILER: String = \"hilt-compiler\""
                content shouldNotContain "const val HILT_NAVIGATION: String = \"compose-hilt-navigation\""

                // Check KSP is in PluginAliases
                val pluginAliasesBlock = content.substringAfter("object PluginAliases").substringBefore("object BuildPropAliases")
                pluginAliasesBlock shouldNotContain "const val KSP: String = \"ksp\""
            }

            Then("it should NOT generate Koin Annotation aliases") {
                content shouldNotContain "const val KOIN_CORE: String = \"koin-core\""
                content shouldNotContain "const val KOIN_ANNOTATIONS: String = \"koin-annotations\""
                content shouldNotContain "const val KOIN_KSP_COMPILER: String = \"koin-ksp-compiler\""
            }
        }

        When("generating Aliases file with Koin Annotations") {
            repository.generateAliases(outputDir, packageName, DiMode.KOIN_ANNOTATIONS)
            val content = fakeFs.writtenFiles.values.first()

            Then("it should contain common aliases") {
                assertCommonAliases(content)
            }

            Then("it should generate Koin Annotation aliases and KSP") {
                content shouldContain "const val KOIN_CORE: String = \"koin-core\""
                content shouldContain "const val KOIN_ANNOTATIONS: String = \"koin-annotations\""
                content shouldContain "const val KOIN_KSP_COMPILER: String = \"koin-ksp-compiler\""
                content shouldContain "const val KOIN_NAVIGATION: String = \"compose-koin-navigation\""
                
                // Check KSP is in PluginAliases
                val pluginAliasesBlock = content.substringAfter("object PluginAliases").substringBefore("object BuildPropAliases")
                pluginAliasesBlock shouldContain "const val KSP: String = \"ksp\""
            }

            Then("it should NOT generate Hilt aliases") {
                content shouldNotContain "const val HILT: String = \"hilt\""
                content shouldNotContain "const val HILT_COMPILER: String = \"hilt-compiler\""
                content shouldNotContain "const val HILT_NAVIGATION: String = \"compose-hilt-navigation\""
            }

            Then("it should NOT generate standard Koin alias") {
                content shouldNotContain "const val KOIN: String = \"koin\""
            }
        }
    }
})
