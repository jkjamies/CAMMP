package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.nio.file.Path

class ConventionPluginRepositoryImplTest : BehaviorSpec({

    Given("a convention plugin repository") {
        val mockFs = mockk<FileSystemRepository>(relaxed = true)
        val repository = ConventionPluginRepositoryImpl(mockFs)
        val outputDir = Path.of("output")
        val packageName = "com.example.convention"

        When("generating DataConventionPlugin for Hilt") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.DATA)

            Then("it should contain Hilt dependencies and plugins") {
                val content = contentSlot.captured
                content shouldContain "class DataConventionPlugin"
                content shouldContain "PluginAliases.HILT"
                content shouldContain "LibsCommon.HILT"
                content shouldContain "LibsCommon.HILT_COMPILER"
            }
            
            Then("it should use static imports") {
                val content = contentSlot.captured
                content shouldContain "import com.example.convention.core.Aliases.PluginAliases"
                content shouldContain "import com.example.convention.core.Aliases.Dependencies.LibsCommon"
            }
        }

        When("generating DataConventionPlugin for Koin") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.KOIN, PluginType.DATA)

            Then("it should contain Koin dependencies") {
                val content = contentSlot.captured
                content shouldContain "LibsCommon.KOIN"
                content shouldContain "PluginAliases.ANDROID_LIBRARY"
            }
            
            Then("it should NOT contain Hilt dependencies") {
                val content = contentSlot.captured
                content shouldNotContain  "LibsCommon.HILT"
            }
        }

        When("generating DataConventionPlugin for Koin Annotations") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.KOIN_ANNOTATIONS, PluginType.DATA)

            Then("it should contain Koin Annotations dependencies") {
                val content = contentSlot.captured
                content shouldContain "LibsCommon.KOIN_CORE"
                content shouldContain "LibsCommon.KOIN_ANNOTATIONS"
                content shouldContain "LibsCommon.KOIN_KSP_COMPILER"
                content shouldContain "PluginAliases.KSP"
            }
        }

        When("generating DIConventionPlugin for Hilt") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.DI)

            Then("it should contain Hilt dependencies") {
                val content = contentSlot.captured
                content shouldContain "class DIConventionPlugin"
                content shouldContain "LibsCommon.HILT"
            }
        }

        When("generating DomainConventionPlugin for Hilt") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.DOMAIN)

            Then("it should contain Hilt dependencies") {
                val content = contentSlot.captured
                content shouldContain "class DomainConventionPlugin"
                content shouldContain "LibsCommon.HILT"
            }
        }

        When("generating PresentationConventionPlugin for Hilt") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.PRESENTATION)

            Then("it should contain Presentation specific configuration") {
                val content = contentSlot.captured
                content shouldContain "class PresentationConventionPlugin"
                content shouldContain "buildFeatures { compose = true }"
                content shouldContain "LibsCompose.UI"
                content shouldContain "LibsCompose.HILT_NAVIGATION"
            }
        }

        When("generating PresentationConventionPlugin for Koin") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.KOIN, PluginType.PRESENTATION)

            Then("it should contain Koin Navigation") {
                val content = contentSlot.captured
                content shouldContain "LibsCompose.KOIN_NAVIGATION"
                content shouldNotContain "LibsCompose.HILT_NAVIGATION"
            }
        }

        When("generating DataSourceConventionPlugin for Hilt") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.DATA_SOURCE)

            Then("it should contain correct class name") {
                val content = contentSlot.captured
                content shouldContain "class DataSourceConventionPlugin"
            }
        }

        When("generating RemoteDataSourceConventionPlugin for Hilt") {
            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, DiMode.HILT, PluginType.REMOTE_DATA_SOURCE)

            Then("it should contain correct class name") {
                val content = contentSlot.captured
                content shouldContain "class RemoteDataSourceConventionPlugin"
            }
        }
    }
})
