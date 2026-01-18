package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.testutil.TestFiles
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationResult
import com.jkjamies.cammp.feature.usecasegenerator.testutil.ModulePackageRepositoryFake
import com.jkjamies.cammp.feature.usecasegenerator.testutil.UseCaseGenerationRepositoryFake
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.io.path.createDirectories

/**
 * Tests for [GenerateUseCaseStep].
 */
class GenerateUseCaseStepTest : BehaviorSpec({

    fun params(domainDir: Path, className: String = "MyUseCase") = UseCaseParams(
        domainDir = domainDir,
        className = className,
        diStrategy = DiStrategy.Hilt,
        repositories = emptyList(),
    )

    Given("GenerateUseCaseStep") {

        When("package is a standard domain module") {
            Then("it should generate in .usecase subpackage") {
                val domainDir = Paths.get("/project/feature/domain")
                val pkgRepo = ModulePackageRepositoryFake(mapping = mapOf(domainDir to "com.example.feature.domain"))
                val genRepo = UseCaseGenerationRepositoryFake { _, useCasePkg, domainPkg, _ ->
                    useCasePkg shouldBe "com.example.feature.domain.usecase"
                    domainPkg shouldBe "com.example.feature.domain"
                    UseCaseGenerationResult(Paths.get("/out/MyUseCase.kt"))
                }
                val step = GenerateUseCaseStep(genRepo, pkgRepo)

                val result = step.execute(params(domainDir))
                result.shouldBeInstanceOf<StepResult.Success>()

                genRepo.calls.single().packageName shouldBe "com.example.feature.domain.usecase"
                genRepo.calls.single().baseDomainPackage shouldBe "com.example.feature.domain"
            }
        }

        When("package is already a usecase module") {
            Then("it should not append .usecase again") {
                val domainDir = Paths.get("/project/feature/domain")
                val pkgRepo = ModulePackageRepositoryFake(mapping = mapOf(domainDir to "com.example.feature.domain.usecase"))
                val genRepo = UseCaseGenerationRepositoryFake { _, useCasePkg, _, _ ->
                    useCasePkg shouldBe "com.example.feature.domain.usecase"
                    UseCaseGenerationResult(Paths.get("/out/MyUseCase.kt"))
                }
                val step = GenerateUseCaseStep(genRepo, pkgRepo)

                val result = step.execute(params(domainDir))
                result.shouldBeInstanceOf<StepResult.Success>()
                genRepo.calls.single().packageName shouldBe "com.example.feature.domain.usecase"
            }
        }

        When("api module exists") {
            Then("it should pass apiDir and include interface path in result") {
                TestFiles.withTempDir("uc_gen_step_api") { root ->
                    val featureRoot = root.resolve("feature").also { it.createDirectories() }
                    val domainDir = featureRoot.resolve("domain").also { it.createDirectories() }
                    val apiDir = featureRoot.resolve("api").also { it.createDirectories() }

                    val pkgRepo = ModulePackageRepositoryFake(mapping = mapOf(domainDir to "com.example.feature.domain"))
                    val genRepo = UseCaseGenerationRepositoryFake { _, _, _, passedApiDir ->
                        passedApiDir shouldBe apiDir
                        UseCaseGenerationResult(
                            useCasePath = Paths.get("/out/MyUseCase.kt"),
                            interfacePath = Paths.get("/out/api/MyUseCase.kt")
                        )
                    }
                    val step = GenerateUseCaseStep(genRepo, pkgRepo)

                    val result = step.execute(params(domainDir))
                    result.shouldBeInstanceOf<StepResult.Success>()
                    result.message shouldContain "Interface: /out/api/MyUseCase.kt"
                }
            }
        }

        When("repository throws") {
            Then("it should return Failure") {
                val domainDir = Paths.get("/project/feature/domain")
                val error = RuntimeException("Disk error")
                val pkgRepo = ModulePackageRepositoryFake(mapping = mapOf(domainDir to "com.example.feature.domain"))
                val genRepo = UseCaseGenerationRepositoryFake { _, _, _, _ -> throw error }
                val step = GenerateUseCaseStep(genRepo, pkgRepo)

                val result = step.execute(params(domainDir))
                result.shouldBeInstanceOf<StepResult.Failure>()
                result.error shouldBe error
            }
        }
    }
})
