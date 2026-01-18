package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.data.factory.ModuleBuildGradleSpecFactoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.data.factory.ModuleSourceSpecFactoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.AnnotationModuleRepositoryFake
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldContain
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import java.nio.file.Path

/**
 * Tests for [CleanArchitectureScaffoldRepositoryImpl].
 */
class CleanArchitectureScaffoldRepositoryImplTest : BehaviorSpec({

    fun params(
        base: Path,
        di: DiStrategy,
        datasourceStrategy: DatasourceStrategy,
        includePresentation: Boolean,
        includeApiModule: Boolean = false,
        includeDiModule: Boolean = true
    ) = CleanArchitectureParams(
        projectBasePath = base,
        root = "feature",
        feature = "my-feature",
        orgCenter = "com.example",
        includePresentation = includePresentation,
        includeApiModule = includeApiModule,
        includeDiModule = includeDiModule,
        datasourceStrategy = datasourceStrategy,
        diStrategy = di,
    )

    Given("CleanArchitectureScaffoldRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()
        val templateRepo = TemplateRepositoryImpl()
        val buildSpecFactory = ModuleBuildGradleSpecFactoryImpl()
        val sourceFactory = ModuleSourceSpecFactoryImpl()

        When("project base path is not a directory") {
            Then("it should fail") {
                val tmp = Files.createTempFile("cammp_base", ".txt")
                val repo = CleanArchitectureScaffoldRepositoryImpl(
                    fs = fs,
                    templateRepo = templateRepo,
                    annotationModuleRepo = AnnotationModuleRepositoryFake(),
                    buildGradleSpecFactory = buildSpecFactory,
                    sourceSpecFactory = sourceFactory,
                )

                val err = runCatching {
                    repo.generateModules(params(tmp, DiStrategy.Hilt, DatasourceStrategy.None, includePresentation = true))
                }.exceptionOrNull()

                err!!.message shouldContain "Project base path does not exist or is not a directory"

                // best-effort cleanup
                tmp.toFile().delete()
            }
        }

        When("generating modules with RemoteAndLocal datasource") {
            Then("it should create expected module directories and build files") {
                withTempDir("cammp_scaffold") { tmp ->
                    val annotationRepo = AnnotationModuleRepositoryFake()

                    val repo = CleanArchitectureScaffoldRepositoryImpl(
                        fs = fs,
                        templateRepo = templateRepo,
                        annotationModuleRepo = annotationRepo,
                        buildGradleSpecFactory = buildSpecFactory,
                        sourceSpecFactory = sourceFactory,
                    )

                    val p = params(
                        base = tmp,
                        di = DiStrategy.Koin(useAnnotations = true),
                        datasourceStrategy = DatasourceStrategy.RemoteAndLocal,
                        includePresentation = true,
                        includeApiModule = true,
                    )

                    val result = repo.generateModules(p)

                    result.created shouldContainExactly listOf(
                        "domain",
                        "data",
                        "api",
                        "di",
                        "presentation",
                        "remoteDataSource",
                        "localDataSource",
                    )

                    val featureDir = tmp.resolve("feature").resolve("my-feature")
                    fs.exists(featureDir.resolve("domain/build.gradle.kts")) shouldBe true
                    fs.exists(featureDir.resolve("data/build.gradle.kts")) shouldBe true
                    fs.exists(featureDir.resolve("api/build.gradle.kts")) shouldBe true
                    fs.exists(featureDir.resolve("di/build.gradle.kts")) shouldBe true
                    fs.exists(featureDir.resolve("presentation/build.gradle.kts")) shouldBe true
                    fs.exists(featureDir.resolve("remoteDataSource/build.gradle.kts")) shouldBe true
                    fs.exists(featureDir.resolve("localDataSource/build.gradle.kts")) shouldBe true

                    // verify some source skeleton creation
                    val basePkg = "com/example/feature/myFeature"
                    fs.exists(featureDir.resolve("domain/src/main/kotlin/$basePkg/domain/Placeholder.kt")) shouldBe true
                    fs.exists(featureDir.resolve("api/src/main/kotlin/$basePkg/api/Placeholder.kt")) shouldBe true
                    fs.exists(featureDir.resolve("api/src/main/kotlin/$basePkg/api/usecase")) shouldBe true
                    fs.exists(featureDir.resolve("api/src/main/kotlin/$basePkg/api/model")) shouldBe true
                    fs.exists(featureDir.resolve("domain/src/main/kotlin/$basePkg/domain/usecase")) shouldBe true
                    fs.exists(featureDir.resolve("domain/src/main/kotlin/$basePkg/domain/model")) shouldBe true
                    fs.exists(featureDir.resolve("domain/src/main/kotlin/$basePkg/domain/repository")) shouldBe true

                    fs.exists(featureDir.resolve("data/src/main/kotlin/$basePkg/data/Placeholder.kt")) shouldBe true
                    fs.exists(featureDir.resolve("data/src/main/kotlin/$basePkg/data/remoteDataSource")) shouldBe true
                    fs.exists(featureDir.resolve("data/src/main/kotlin/$basePkg/data/localDataSource")) shouldBe true

                    // Koin annotations should trigger annotation module generation under di
                    annotationRepo.calls.size shouldBe 1
                }
            }
        }

        When("generating modules for Koin with annotations but includeDiModule is false") {
            Then("it should NOT create di directory and NOT call annotation repo") {
                withTempDir("cammp_scaffold_no_di") { tmp ->
                    val annotationRepo = AnnotationModuleRepositoryFake()
                    val repo = CleanArchitectureScaffoldRepositoryImpl(
                        fs = fs,
                        templateRepo = templateRepo,
                        annotationModuleRepo = annotationRepo,
                        buildGradleSpecFactory = buildSpecFactory,
                        sourceSpecFactory = sourceFactory,
                    )

                    val p = params(
                        base = tmp,
                        di = DiStrategy.Koin(useAnnotations = true),
                        datasourceStrategy = DatasourceStrategy.None,
                        includePresentation = false,
                        includeDiModule = false,
                    )

                    val result = repo.generateModules(p)

                    result.created shouldBe listOf("domain", "data")
                    fs.exists(tmp.resolve("feature/my-feature/di")) shouldBe false
                    annotationRepo.calls.size shouldBe 0
                }
            }
        }

        When("module directories already exist") {
            Then("it should skip all and report no created modules") {
                withTempDir("cammp_scaffold2") { tmp ->
                    val p = params(tmp, DiStrategy.Hilt, DatasourceStrategy.None, includePresentation = false)
                    val featureDir = tmp.resolve("feature").resolve("my-feature")
                    Files.createDirectories(featureDir.resolve("domain"))
                    Files.createDirectories(featureDir.resolve("data"))
                    Files.createDirectories(featureDir.resolve("di"))

                    val repo = CleanArchitectureScaffoldRepositoryImpl(
                        fs = fs,
                        templateRepo = templateRepo,
                        annotationModuleRepo = AnnotationModuleRepositoryFake(),
                        buildGradleSpecFactory = buildSpecFactory,
                        sourceSpecFactory = sourceFactory,
                    )

                    val result = repo.generateModules(p)

                    result.created shouldBe emptyList()
                    result.skipped shouldContain "domain"
                    result.message shouldContain "No modules created"
                }
            }
        }
    }
})
