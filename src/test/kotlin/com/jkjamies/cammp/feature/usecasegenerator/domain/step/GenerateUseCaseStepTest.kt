package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.testutil.ModulePackageRepositoryFake
import com.jkjamies.cammp.feature.usecasegenerator.testutil.UseCaseGenerationRepositoryFake
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import java.nio.file.Path
import java.nio.file.Paths

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
        val domainDir = Paths.get("/project/feature/domain")

        When("package is a standard domain module") {
            Then("it should generate in .usecase subpackage") {
                val pkgRepo = ModulePackageRepositoryFake(mapping = mapOf(domainDir to "com.example.feature.domain"))
                val genRepo = UseCaseGenerationRepositoryFake { _, useCasePkg, domainPkg, _ ->
                    useCasePkg shouldBe "com.example.feature.domain.usecase"
                    domainPkg shouldBe "com.example.feature.domain"
                    Paths.get("/out/MyUseCase.kt")
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
                val pkgRepo = ModulePackageRepositoryFake(mapping = mapOf(domainDir to "com.example.feature.domain.usecase"))
                val genRepo = UseCaseGenerationRepositoryFake { _, useCasePkg, _, _ ->
                    useCasePkg shouldBe "com.example.feature.domain.usecase"
                    Paths.get("/out/MyUseCase.kt")
                }
                val step = GenerateUseCaseStep(genRepo, pkgRepo)

                val result = step.execute(params(domainDir))
                result.shouldBeInstanceOf<StepResult.Success>()
                genRepo.calls.single().packageName shouldBe "com.example.feature.domain.usecase"
            }
        }

        When("repository throws") {
            Then("it should return Failure") {
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
