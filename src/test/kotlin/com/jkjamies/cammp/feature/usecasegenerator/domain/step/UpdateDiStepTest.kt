package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.testutil.ModulePackageRepositoryFake
import com.jkjamies.cammp.feature.usecasegenerator.testutil.UseCaseDiModuleRepositoryFake
import com.jkjamies.cammp.feature.presentationgenerator.testutil.TestFiles
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Path
import kotlin.io.path.createDirectories

/**
 * Tests for [UpdateDiStep].
 */
class UpdateDiStepTest : BehaviorSpec({

    fun params(
        domainDir: Path,
        className: String = "MyUseCase",
        diStrategy: DiStrategy = DiStrategy.Koin(useAnnotations = false),
        repositories: List<String> = listOf("MyRepo"),
    ) = UseCaseParams(
        domainDir = domainDir,
        className = className,
        diStrategy = diStrategy,
        repositories = repositories,
    )

    Given("UpdateDiStep") {

        When("di strategy is not Koin") {
            Then("it should skip") {
                TestFiles.withTempDir("uc_update_di_skip") { root ->
                    val featureRoot = root.resolve("feature").also { it.createDirectories() }
                    val domainDir = featureRoot.resolve("domain").also { it.createDirectories() }

                    val diRepo = UseCaseDiModuleRepositoryFake()
                    val pkgRepo = ModulePackageRepositoryFake(defaultPkg = "com.example.domain")
                    val step = UpdateDiStep(diRepo, pkgRepo)

                    val result = step.execute(params(domainDir, diStrategy = DiStrategy.Hilt))
                    result.shouldBeInstanceOf<StepResult.Success>()
                    diRepo.calls.size shouldBe 0
                }
            }
        }

        When("koin annotations are enabled") {
            Then("it should skip") {
                TestFiles.withTempDir("uc_update_di_skip") { root ->
                    val featureRoot = root.resolve("feature").also { it.createDirectories() }
                    val domainDir = featureRoot.resolve("domain").also { it.createDirectories() }

                    val diRepo = UseCaseDiModuleRepositoryFake()
                    val pkgRepo = ModulePackageRepositoryFake(defaultPkg = "com.example.domain")
                    val step = UpdateDiStep(diRepo, pkgRepo)

                    val result = step.execute(params(domainDir, diStrategy = DiStrategy.Koin(useAnnotations = true)))
                    result.shouldBeInstanceOf<StepResult.Success>()
                    diRepo.calls.size shouldBe 0
                }
            }
        }

        When("di dir does not exist") {
            Then("it should skip") {
                TestFiles.withTempDir("uc_update_di_skip") { root ->
                    val featureRoot = root.resolve("feature").also { it.createDirectories() }
                    val domainDir = featureRoot.resolve("domain").also { it.createDirectories() }
                    // no di directory

                    val diRepo = UseCaseDiModuleRepositoryFake()
                    val pkgRepo = ModulePackageRepositoryFake(defaultPkg = "com.example.domain")
                    val step = UpdateDiStep(diRepo, pkgRepo)

                    val result = step.execute(params(domainDir))
                    result.shouldBeInstanceOf<StepResult.Success>()
                    diRepo.calls.size shouldBe 0
                }
            }
        }

        When("koin without annotations and di dir exists") {
            Then("it should call mergeUseCaseModule with expected args") {
                TestFiles.withTempDir("uc_update_di") { root ->
                    val featureRoot = root.resolve("feature").also { it.createDirectories() }
                    val domainDir = featureRoot.resolve("domain").also { it.createDirectories() }
                    val diDir = featureRoot.resolve("di").also { it.createDirectories() }

                    val pkgRepo = ModulePackageRepositoryFake(
                        mapping = mapOf(
                            diDir to "com.example.di",
                            domainDir to "com.example.domain",
                        )
                    )
                    val diRepo = UseCaseDiModuleRepositoryFake()
                    val step = UpdateDiStep(diRepo, pkgRepo)

                    val result = step.execute(params(domainDir, repositories = listOf("MyRepo")))
                    result.shouldBeInstanceOf<StepResult.Success>()

                    diRepo.calls.size shouldBe 1
                    val call = diRepo.calls.single()
                    call.diDir shouldBe diDir
                    call.diPackage shouldBe "com.example.di"
                    call.useCaseSimpleName shouldBe "MyUseCase"
                    call.useCaseFqn shouldBe "com.example.domain.usecase.MyUseCase"
                    call.repositoryFqns shouldBe listOf("com.example.domain.repository.MyRepo")
                }
            }
        }
    }
})
