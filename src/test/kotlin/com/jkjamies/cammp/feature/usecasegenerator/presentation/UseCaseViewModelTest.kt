package com.jkjamies.cammp.feature.usecasegenerator.presentation

import app.cash.turbine.test
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.usecase.LoadRepositories
import com.jkjamies.cammp.feature.usecasegenerator.domain.usecase.UseCaseGenerator
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import java.nio.file.Paths

/**
 * Tests for [UseCaseViewModel].
 */
@OptIn(ExperimentalCoroutinesApi::class, ExperimentalStdlibApi::class)
class UseCaseViewModelTest : BehaviorSpec({

    fun newVm(
        scope: TestScope,
        generator: UseCaseGenerator,
        loadRepositories: LoadRepositories,
        domainPackage: String = "",
    ) = UseCaseViewModel(
        domainPackage = domainPackage,
        scope = scope,
        generator = generator,
        loadRepositories = loadRepositories,
    )

    Given("UseCaseViewModel") {

        When("initialized") {
            Then("it should emit initial state") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<UseCaseGenerator>()
                val loadRepos = mockk<LoadRepositories>()
                val vm = newVm(scope, generator, loadRepos)

                vm.state.test {
                    val state = awaitItem()
                    state.name shouldBe ""
                    state.domainPackage shouldBe ""
                    state.async shouldBe true
                    state.sync shouldBe false
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("setting name") {
            Then("it should update state") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val vm = newVm(scope, mockk(), mockk())

                vm.state.test {
                    awaitItem()
                    vm.handleIntent(UseCaseIntent.SetName("GetItems"))
                    awaitItem().name shouldBe "GetItems"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("setting domain package to a non-domain folder") {
            Then("it should set an error and clear repositories") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val loadRepos = mockk<LoadRepositories>()
                val vm = newVm(scope, mockk(), loadRepos)

                vm.state.test {
                    awaitItem()
                    vm.handleIntent(UseCaseIntent.SetDomainPackage("/path/to/invalid"))
                    val state = awaitItem()
                    state.domainPackage shouldBe "/path/to/invalid"
                    state.errorMessage shouldBe "Selected directory must be a domain module"
                    state.availableRepositories shouldBe emptyList()
                    state.selectedRepositories shouldBe emptySet()
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 0) { loadRepos(any()) }
            }
        }

        When("setting a valid domain package") {
            Then("it should load repositories and prune selections") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<UseCaseGenerator>()
                val loadRepos = mockk<LoadRepositories>()
                every { loadRepos("/path/to/domain") } returns listOf("Repo1", "Repo2")

                val vm = newVm(scope, generator, loadRepos)

                vm.state.test {
                    awaitItem()

                    vm.handleIntent(UseCaseIntent.ToggleRepository("OldRepo", true))
                    awaitItem().selectedRepositories shouldBe setOf("OldRepo")

                    vm.handleIntent(UseCaseIntent.SetDomainPackage("/path/to/domain"))
                    val afterSet = awaitItem()
                    afterSet.domainPackage shouldBe "/path/to/domain"
                    afterSet.errorMessage shouldBe null

                    scope.advanceUntilIdle()
                    val afterLoad = awaitItem()
                    afterLoad.availableRepositories shouldBe listOf("Repo1", "Repo2")
                    afterLoad.selectedRepositories shouldBe emptySet() // pruned

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("toggling async/sync") {
            Then("they should be mutually exclusive") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val vm = newVm(scope, mockk(), mockk())

                vm.state.test {
                    val initial = awaitItem()
                    initial.async shouldBe true
                    initial.sync shouldBe false

                    vm.handleIntent(UseCaseIntent.SetSync(true))
                    val sync = awaitItem()
                    sync.sync shouldBe true
                    sync.async shouldBe false

                    vm.handleIntent(UseCaseIntent.SetAsync(true))
                    val async = awaitItem()
                    async.async shouldBe true
                    async.sync shouldBe false

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("toggling DI framework") {
            Then("Metro, Hilt and Koin should be mutually exclusive and annotations reset when leaving koin") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val vm = newVm(scope, mockk(), mockk())

                vm.state.test {
                    val initial = awaitItem()
                    initial.diMetro shouldBe false
                    initial.diHilt shouldBe true
                    initial.diKoin shouldBe false
                    initial.diKoinAnnotations shouldBe false

                    vm.handleIntent(UseCaseIntent.SetDiKoin(true))
                    val koin = awaitItem()
                    koin.diKoin shouldBe true
                    koin.diMetro shouldBe false
                    koin.diHilt shouldBe false

                    vm.handleIntent(UseCaseIntent.ToggleKoinAnnotations(true))
                    awaitItem().diKoinAnnotations shouldBe true

                    vm.handleIntent(UseCaseIntent.SetDiMetro(true))
                    val metro = awaitItem()
                    metro.diMetro shouldBe true
                    metro.diHilt shouldBe false
                    metro.diKoin shouldBe false
                    metro.diKoinAnnotations shouldBe false

                    vm.handleIntent(UseCaseIntent.SetDiHilt(true))
                    val hilt = awaitItem()
                    hilt.diHilt shouldBe true
                    hilt.diMetro shouldBe false
                    hilt.diKoin shouldBe false
                    hilt.diKoinAnnotations shouldBe false

                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("generating with invalid state") {
            Then("it should set error and not call generator") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<UseCaseGenerator>()
                val vm = newVm(scope, generator, mockk())

                vm.state.test {
                    awaitItem()
                    vm.handleIntent(UseCaseIntent.Generate)
                    awaitItem().errorMessage shouldBe "Name is required"
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 0) { generator(any()) }
            }
        }

        When("generating succeeds") {
            Then("it should emit generating then final state and call generator with sorted repos") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<UseCaseGenerator>()
                val loadRepos = mockk<LoadRepositories>()
                every { loadRepos("/path/to/domain") } returns emptyList()

                val expectedParams = UseCaseParams(
                    domainDir = Paths.get("/path/to/domain"),
                    className = "ValidUseCase",
                    diStrategy = DiStrategy.Hilt,
                    repositories = listOf("ARepo", "BRepo"),
                )

                coEvery { generator(expectedParams) } returns Result.success(Paths.get("out/UseCase.kt"))

                val vm = newVm(scope, generator, loadRepos)

                vm.state.test {
                    awaitItem()

                    vm.handleIntent(UseCaseIntent.SetName("ValidUseCase"))
                    awaitItem()
                    vm.handleIntent(UseCaseIntent.SetDomainPackage("/path/to/domain"))
                    awaitItem()
                    scope.advanceUntilIdle() // allow repo load

                    vm.handleIntent(UseCaseIntent.ToggleRepository("BRepo", true))
                    awaitItem()
                    vm.handleIntent(UseCaseIntent.ToggleRepository("ARepo", true))
                    awaitItem()

                    vm.handleIntent(UseCaseIntent.Generate)
                    awaitItem().isGenerating shouldBe true

                    scope.advanceUntilIdle()
                    val final = awaitItem()
                    final.isGenerating shouldBe false
                    final.lastGeneratedPath shouldBe "out/UseCase.kt"
                    final.errorMessage shouldBe null

                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { generator(expectedParams) }
            }
        }

        When("generating fails") {
            Then("it should surface exception message") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<UseCaseGenerator>()
                val loadRepos = mockk<LoadRepositories>()
                every { loadRepos("/path/to/domain") } returns emptyList()

                val expectedParams = UseCaseParams(
                    domainDir = Paths.get("/path/to/domain"),
                    className = "ValidUseCase",
                    diStrategy = DiStrategy.Hilt,
                    repositories = emptyList(),
                )

                coEvery { generator(expectedParams) } returns Result.failure(IllegalStateException("Failure"))

                val vm = newVm(scope, generator, loadRepos)

                vm.handleIntent(UseCaseIntent.SetName("ValidUseCase"))
                vm.handleIntent(UseCaseIntent.SetDomainPackage("/path/to/domain"))

                vm.state.test {
                    awaitItem()
                    vm.handleIntent(UseCaseIntent.Generate)
                    awaitItem().isGenerating shouldBe true
                    scope.advanceUntilIdle()
                    val final = awaitItem()
                    final.isGenerating shouldBe false
                    final.errorMessage shouldBe "Failure"
                    cancelAndIgnoreRemainingEvents()
                }
            }
        }

        When("name normalization") {
            Then("it should append UseCase suffix if missing") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<UseCaseGenerator>()
                val loadRepos = mockk<LoadRepositories>()
                every { loadRepos("/path/to/domain") } returns emptyList()

                val expectedParams = UseCaseParams(
                    domainDir = Paths.get("/path/to/domain"),
                    className = "FooUseCase",
                    diStrategy = DiStrategy.Hilt,
                    repositories = emptyList(),
                )

                coEvery { generator(expectedParams) } returns Result.success(Paths.get("out/FooUseCase.kt"))

                val vm = newVm(scope, generator, loadRepos)

                vm.handleIntent(UseCaseIntent.SetName("Foo"))
                vm.handleIntent(UseCaseIntent.SetDomainPackage("/path/to/domain"))

                vm.state.test {
                    awaitItem()
                    vm.handleIntent(UseCaseIntent.Generate)
                    awaitItem().isGenerating shouldBe true
                    scope.advanceUntilIdle()
                    val final = awaitItem()
                    final.lastGeneratedPath shouldBe "out/FooUseCase.kt"
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { generator(expectedParams) }
            }
        }

        When("DI strategy is Koin") {
            Then("it should invoke generator with Koin strategy") {
                val dispatcher = StandardTestDispatcher()
                val scope = TestScope(dispatcher)
                val generator = mockk<UseCaseGenerator>()
                val loadRepos = mockk<LoadRepositories>()
                every { loadRepos("/path/to/domain") } returns emptyList()

                val expectedParams = UseCaseParams(
                    domainDir = Paths.get("/path/to/domain"),
                    className = "ValidUseCase",
                    diStrategy = DiStrategy.Koin(useAnnotations = true),
                    repositories = emptyList(),
                )

                coEvery { generator(expectedParams) } returns Result.success(Paths.get("out/UseCase.kt"))

                val vm = newVm(scope, generator, loadRepos)

                vm.handleIntent(UseCaseIntent.SetName("ValidUseCase"))
                vm.handleIntent(UseCaseIntent.SetDomainPackage("/path/to/domain"))
                vm.handleIntent(UseCaseIntent.SetDiKoin(true))
                vm.handleIntent(UseCaseIntent.ToggleKoinAnnotations(true))

                vm.state.test {
                    awaitItem()
                    vm.handleIntent(UseCaseIntent.Generate)
                    awaitItem().isGenerating shouldBe true
                    scope.advanceUntilIdle()
                    awaitItem()
                    cancelAndIgnoreRemainingEvents()
                }

                coVerify(exactly = 1) { generator(expectedParams) }
            }
        }
    }
})
