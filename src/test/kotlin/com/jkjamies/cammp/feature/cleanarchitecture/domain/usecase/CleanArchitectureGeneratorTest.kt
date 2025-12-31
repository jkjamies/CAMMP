package com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AliasesRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.ConventionPluginRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import com.jkjamies.cammp.feature.cleanarchitecture.fakes.FakeFileSystemRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Path

/**
 * Test class for [CleanArchitectureGenerator].
 */
class CleanArchitectureGeneratorTest : BehaviorSpec({

    Given("a clean architecture generator") {
        val fakeFs = FakeFileSystemRepository()
        val fakeTemplateRepo = FakeTemplateRepository()
        val fakeSettingsRepo = FakeGradleSettingsRepository()
        val fakeAnnotationRepo = FakeAnnotationModuleRepository()
        val fakeConventionRepo = FakeConventionPluginRepository()
        val fakeAliasesRepo = FakeAliasesRepository()

        val generator = CleanArchitectureGenerator(
            fs = fakeFs,
            templateRepo = fakeTemplateRepo,
            settingsRepo = fakeSettingsRepo,
            annotationModuleRepo = fakeAnnotationRepo,
            conventionPluginRepo = fakeConventionRepo,
            aliasesRepo = fakeAliasesRepo
        )

        When("invoked with valid params and koin annotations") {
            val projectBase = Path.of("project")
            fakeFs.directories.add(projectBase)
            fakeFs.existingPaths.add(projectBase)

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

            val result = generator(params)

            Then("it should generate annotation module") {
                result.isSuccess shouldBe true
                
                fakeAnnotationRepo.lastCall shouldBe AnnotationModuleCall(
                    packageName = "com.example.app.myFeature.di",
                    scanPackage = "com.example.app.myFeature",
                    featureName = "myFeature"
                )
            }

            Then("it should generate aliases for KOIN_ANNOTATIONS") {
                fakeAliasesRepo.lastCall shouldBe AliasesCall(
                    packageName = "com.example.convention.core",
                    diMode = DiMode.KOIN_ANNOTATIONS,
                    tomlPath = projectBase.resolve("gradle/libs.versions.toml")
                )
            }
        }

        When("invoked with valid params and koin (no annotations)") {
            val projectBase = Path.of("project")
            fakeFs.directories.add(projectBase)
            fakeFs.existingPaths.add(projectBase)

            val params = CleanArchitectureParams(
                projectBasePath = projectBase,
                root = "app",
                feature = "my-feature-koin",
                orgCenter = "com.example",
                includePresentation = true,
                includeDatasource = true,
                datasourceCombined = true,
                datasourceRemote = false,
                datasourceLocal = false,
                diHilt = false,
                diKoin = true,
                diKoinAnnotations = false
            )

            val result = generator(params)

            Then("it should generate aliases for KOIN") {
                result.isSuccess shouldBe true
                fakeAliasesRepo.lastCall shouldBe AliasesCall(
                    packageName = "com.example.convention.core",
                    diMode = DiMode.KOIN,
                    tomlPath = projectBase.resolve("gradle/libs.versions.toml")
                )
            }
        }

        When("invoked with valid params and hilt") {
            val projectBase = Path.of("project")
            fakeFs.directories.add(projectBase)
            fakeFs.existingPaths.add(projectBase)

            val params = CleanArchitectureParams(
                projectBasePath = projectBase,
                root = "app",
                feature = "my-feature-hilt",
                orgCenter = "com.example",
                includePresentation = true,
                includeDatasource = true,
                datasourceCombined = true,
                datasourceRemote = false,
                datasourceLocal = false,
                diHilt = true,
                diKoin = false,
                diKoinAnnotations = false
            )

            val result = generator(params)

            Then("it should generate aliases for HILT") {
                result.isSuccess shouldBe true
                fakeAliasesRepo.lastCall shouldBe AliasesCall(
                    packageName = "com.example.convention.core",
                    diMode = DiMode.HILT,
                    tomlPath = projectBase.resolve("gradle/libs.versions.toml")
                )
            }
        }

        When("invoked with blank orgCenter") {
            val projectBase = Path.of("project")
            fakeFs.directories.add(projectBase)
            fakeFs.existingPaths.add(projectBase)

            val params = CleanArchitectureParams(
                projectBasePath = projectBase,
                root = "app",
                feature = "my-feature-blank",
                orgCenter = "   ", // Blank
                includePresentation = true,
                includeDatasource = true,
                datasourceCombined = true,
                datasourceRemote = false,
                datasourceLocal = false,
                diHilt = true,
                diKoin = false,
                diKoinAnnotations = false
            )

            val result = generator(params)

            Then("it should use default 'cammp' orgCenter") {
                result.isSuccess shouldBe true
                fakeAliasesRepo.lastCall?.packageName shouldBe "com.cammp.convention.core"
            }
        }

        When("invoked with orgCenter needing sanitization") {
            val projectBase = Path.of("project")
            fakeFs.directories.add(projectBase)
            fakeFs.existingPaths.add(projectBase)

            val params = CleanArchitectureParams(
                projectBasePath = projectBase,
                root = "app",
                feature = "my-feature-sanitize",
                orgCenter = "\${com.My-Org.Center}", // Needs sanitization
                includePresentation = true,
                includeDatasource = true,
                datasourceCombined = true,
                datasourceRemote = false,
                datasourceLocal = false,
                diHilt = true,
                diKoin = false,
                diKoinAnnotations = false
            )

            val result = generator(params)

            Then("it should sanitize orgCenter") {
                result.isSuccess shouldBe true
                // ${com.My-Org.Center} -> com.My-Org.Center -> My-Org.Center -> MyOrg.Center -> myOrg.Center
                fakeAliasesRepo.lastCall?.packageName shouldBe "com.myOrg.Center.convention.core"
            }
        }

        When("invoked with uppercase orgCenter") {
            val projectBase = Path.of("project")
            fakeFs.directories.add(projectBase)
            fakeFs.existingPaths.add(projectBase)

            val params = CleanArchitectureParams(
                projectBasePath = projectBase,
                root = "app",
                feature = "my-feature-uppercase",
                orgCenter = "UppercaseOrg",
                includePresentation = true,
                includeDatasource = true,
                datasourceCombined = true,
                datasourceRemote = false,
                datasourceLocal = false,
                diHilt = true,
                diKoin = false,
                diKoinAnnotations = false
            )

            val result = generator(params)

            Then("it should lowercase the first char of orgCenter") {
                result.isSuccess shouldBe true
                fakeAliasesRepo.lastCall?.packageName shouldBe "com.uppercaseOrg.convention.core"
            }
        }
    }
})

// Fakes
private data class AnnotationModuleCall(
    val packageName: String,
    val scanPackage: String,
    val featureName: String
)

private class FakeAnnotationModuleRepository : AnnotationModuleRepository {
    var lastCall: AnnotationModuleCall? = null
    
    override fun generate(
        outputDirectory: Path,
        packageName: String,
        scanPackage: String,
        featureName: String
    ) {
        lastCall = AnnotationModuleCall(packageName, scanPackage, featureName)
    }
}

private data class AliasesCall(
    val packageName: String,
    val diMode: DiMode,
    val tomlPath: Path
)

private class FakeAliasesRepository : AliasesRepository {
    var lastCall: AliasesCall? = null

    override fun generateAliases(
        outputDirectory: Path,
        packageName: String,
        diMode: DiMode,
        tomlPath: Path
    ) {
        lastCall = AliasesCall(packageName, diMode, tomlPath)
    }
}

private class FakeTemplateRepository : TemplateRepository {
    override fun getTemplateText(resourcePath: String): String = "template content"
}

private class FakeGradleSettingsRepository : GradleSettingsRepository {
    override fun ensureIncludes(projectBase: Path, root: String, feature: String, modules: List<String>): Boolean = true
    override fun ensureIncludeBuild(projectBase: Path, buildLogicName: String): Boolean = true
    override fun ensureVersionCatalogPluginAliases(projectBase: Path, orgSegment: String, enabledModules: List<String>): Boolean = true
    override fun ensureAppDependency(projectBase: Path, root: String, feature: String, diMode: DiMode): Boolean = true
}

private class FakeConventionPluginRepository : ConventionPluginRepository {
    override fun generate(outputDirectory: Path, packageName: String, diMode: DiMode, type: PluginType) {
        // no-op
    }
}
