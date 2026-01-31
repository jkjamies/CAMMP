/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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

        When("di package cannot be found") {
            Then("it should skip") {
                TestFiles.withTempDir("uc_update_di_skip") { root ->
                    val featureRoot = root.resolve("feature").also { it.createDirectories() }
                    val domainDir = featureRoot.resolve("domain").also { it.createDirectories() }
                    val diDir = featureRoot.resolve("di").also { it.createDirectories() }

                    val diRepo = UseCaseDiModuleRepositoryFake()
                    val pkgRepo = ModulePackageRepositoryFake(
                        mapping = mapOf(domainDir to "com.example.domain")
                        // diDir missing from mapping
                    )
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

        When("di package has .usecase or .repository suffix") {
            Then("it should strip the suffix") {
                TestFiles.withTempDir("uc_update_di_strip") { root ->
                    val featureRoot = root.resolve("feature").also { it.createDirectories() }
                    val domainDir = featureRoot.resolve("domain").also { it.createDirectories() }
                    val diDir = featureRoot.resolve("di").also { it.createDirectories() }

                    val pkgRepo = ModulePackageRepositoryFake(
                        mapping = mapOf(
                            diDir to "com.example.di.usecase",
                            domainDir to "com.example.domain",
                        )
                    )
                    val diRepo = UseCaseDiModuleRepositoryFake()
                    val step = UpdateDiStep(diRepo, pkgRepo)

                    val result = step.execute(params(domainDir))
                    result.shouldBeInstanceOf<StepResult.Success>()

                    val call = diRepo.calls.single()
                    call.diPackage shouldBe "com.example.di"
                }
            }
        }

        When("api module exists") {
            Then("it should pass useCaseInterfaceFqn to repository") {
                TestFiles.withTempDir("uc_update_di_api") { root ->
                    val featureRoot = root.resolve("feature").also { it.createDirectories() }
                    val domainDir = featureRoot.resolve("domain").also { it.createDirectories() }
                    val diDir = featureRoot.resolve("di").also { it.createDirectories() }
                    val apiDir = featureRoot.resolve("api").also { it.createDirectories() }

                    val pkgRepo = ModulePackageRepositoryFake(
                        mapping = mapOf(
                            diDir to "com.example.feature.di",
                            domainDir to "com.example.feature.domain",
                        )
                    )
                    val diRepo = UseCaseDiModuleRepositoryFake()
                    val step = UpdateDiStep(diRepo, pkgRepo)

                    val result = step.execute(params(domainDir))
                    result.shouldBeInstanceOf<StepResult.Success>()

                    val call = diRepo.calls.single()
                    call.useCaseInterfaceFqn shouldBe "com.example.feature.usecase.MyUseCase"
                }
            }
        }
    }
})
