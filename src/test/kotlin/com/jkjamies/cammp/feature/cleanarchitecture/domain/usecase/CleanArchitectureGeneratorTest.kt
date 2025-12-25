package com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AliasesRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.ConventionPluginRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.file.Path

class CleanArchitectureGeneratorTest : BehaviorSpec({

    Given("a clean architecture generator") {
        val mockFs = mockk<FileSystemRepository>(relaxed = true)
        val mockTemplateRepo = mockk<TemplateRepository>(relaxed = true)
        val mockSettingsRepo = mockk<GradleSettingsRepository>(relaxed = true)
        val mockAnnotationRepo = mockk<AnnotationModuleRepository>(relaxed = true)
        val mockConventionRepo = mockk<ConventionPluginRepository>(relaxed = true)
        val mockAliasesRepo = mockk<AliasesRepository>(relaxed = true)

        val generator = CleanArchitectureGenerator(
            fs = mockFs,
            templateRepo = mockTemplateRepo,
            settingsRepo = mockSettingsRepo,
            annotationModuleRepo = mockAnnotationRepo,
            conventionPluginRepo = mockConventionRepo,
            aliasesRepo = mockAliasesRepo
        )

        When("invoked with valid params and koin annotations") {
            val projectBase = Path.of("project")
            val params = CleanArchitectureParams(
                projectBasePath = projectBase,
                root = "app",
                feature = "my-feature",
                orgCenter = "com.example",
                includePresentation = true,
                includeDatasource = true,
                datasourceCombined = true,
                datasourceRemote = false,
                datasourceLocal = false,
                diHilt = false,
                diKoin = true,
                diKoinAnnotations = true
            )

            every { mockFs.isDirectory(projectBase) } returns true
            every { mockFs.exists(any()) } returns false
            every { mockFs.createDirectories(any()) } answers { firstArg() }
            every { mockTemplateRepo.getTemplateText(any()) } returns "template content"

            val result = generator(params)

            Then("it should generate annotation module") {
                result.isSuccess shouldBe true
                verify {
                    mockAnnotationRepo.generate(
                        outputDirectory = any(),
                        packageName = "com.example.app.myFeature.di",
                        scanPackage = "com.example.app.myFeature",
                        featureName = "myFeature"
                    )
                }
            }

            Then("it should generate aliases") {
                verify {
                    mockAliasesRepo.generateAliases(
                        outputDirectory = any(),
                        packageName = "com.example.convention.core",
                        diMode = DiMode.KOIN_ANNOTATIONS
                    )
                }
            }
        }
    }
})
