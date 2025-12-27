package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import com.jkjamies.cammp.feature.cleanarchitecture.fakes.FakeFileSystemRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path

/**
 * Test class for [ConventionPluginRepositoryImpl].
 */
class ConventionPluginRepositoryImplTest : BehaviorSpec({

    Given("a convention plugin repository") {
        val fakeFs = FakeFileSystemRepository()
        val repository = ConventionPluginRepositoryImpl(fakeFs)
        val outputDir = Path.of("output")
        val packageName = "com.example.convention"

        When("generating DataConventionPlugin for Hilt") {
            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.DATA)
            val outputFile = outputDir.resolve("DataConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain Hilt dependencies and plugins") {
                content.shouldNotBeNull()
                content shouldContain "class DataConventionPlugin"
                content shouldContain "PluginAliases.HILT"
                content shouldContain "LibsCommon.HILT"
                content shouldContain "LibsCommon.HILT_COMPILER"
            }
            
            Then("it should use static imports") {
                content.shouldNotBeNull()
                content shouldContain "import com.example.convention.core.Aliases.PluginAliases"
                content shouldContain "import com.example.convention.core.Aliases.Dependencies.LibsCommon"
            }
        }

        When("generating DataConventionPlugin for Koin") {
            repository.generate(outputDir, packageName, DiMode.KOIN, PluginType.DATA)
            val outputFile = outputDir.resolve("DataConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain Koin dependencies") {
                content.shouldNotBeNull()
                content shouldContain "LibsCommon.KOIN"
                content shouldContain "PluginAliases.ANDROID_LIBRARY"
            }
            
            Then("it should NOT contain Hilt dependencies") {
                content.shouldNotBeNull()
                content shouldNotContain  "LibsCommon.HILT"
            }
        }

        When("generating DataConventionPlugin for Koin Annotations") {
            repository.generate(outputDir, packageName, DiMode.KOIN_ANNOTATIONS, PluginType.DATA)
            val outputFile = outputDir.resolve("DataConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain Koin Annotations dependencies") {
                content.shouldNotBeNull()
                content shouldContain "LibsCommon.KOIN_CORE"
                content shouldContain "LibsCommon.KOIN_ANNOTATIONS"
                content shouldContain "LibsCommon.KOIN_KSP_COMPILER"
                content shouldContain "PluginAliases.KSP"
            }
        }

        When("generating DIConventionPlugin for Hilt") {
            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.DI)
            val outputFile = outputDir.resolve("DIConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain Hilt dependencies") {
                content.shouldNotBeNull()
                content shouldContain "class DIConventionPlugin"
                content shouldContain "LibsCommon.HILT"
            }
        }

        When("generating DomainConventionPlugin for Hilt") {
            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.DOMAIN)
            val outputFile = outputDir.resolve("DomainConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain Hilt dependencies") {
                content.shouldNotBeNull()
                content shouldContain "class DomainConventionPlugin"
                content shouldContain "LibsCommon.HILT"
            }
        }

        When("generating PresentationConventionPlugin for Hilt") {
            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.PRESENTATION)
            val outputFile = outputDir.resolve("PresentationConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain Presentation specific configuration") {
                content.shouldNotBeNull()
                content shouldContain "class PresentationConventionPlugin"
                content shouldContain "buildFeatures { compose = true }"
                content shouldContain "LibsCompose.UI"
                content shouldContain "LibsCompose.HILT_NAVIGATION"
            }
        }

        When("generating PresentationConventionPlugin for Koin") {
            repository.generate(outputDir, packageName, DiMode.KOIN, PluginType.PRESENTATION)
            val outputFile = outputDir.resolve("PresentationConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain Koin Navigation") {
                content.shouldNotBeNull()
                content shouldContain "LibsCompose.KOIN_NAVIGATION"
                content shouldNotContain "LibsCompose.HILT_NAVIGATION"
            }
        }

        When("generating DataSourceConventionPlugin for Hilt") {
            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.DATA_SOURCE)
            val outputFile = outputDir.resolve("DataSourceConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain correct class name") {
                content.shouldNotBeNull()
                content shouldContain "class DataSourceConventionPlugin"
            }
        }

        When("generating RemoteDataSourceConventionPlugin for Hilt") {
            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.REMOTE_DATA_SOURCE)
            val outputFile = outputDir.resolve("RemoteDataSourceConventionPlugin.kt")
            val content = fakeFs.writtenFiles[outputFile]

            Then("it should contain correct class name") {
                content.shouldNotBeNull()
                content shouldContain "class RemoteDataSourceConventionPlugin"
            }
        }
    }
})
