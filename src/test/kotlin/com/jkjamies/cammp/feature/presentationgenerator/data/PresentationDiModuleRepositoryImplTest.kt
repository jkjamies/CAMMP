package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.testutil.TestFiles
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import kotlin.io.path.createDirectories
import kotlin.io.path.readText

/**
 * Tests for [PresentationDiModuleRepositoryImpl].
 */
class PresentationDiModuleRepositoryImplTest : BehaviorSpec({

    Given("PresentationDiModuleRepositoryImpl") {

        When("module file does not exist") {
            Then("it should create a new module") {
                TestFiles.withTempDir("pg_dimodule") { dir ->
                    val repo = PresentationDiModuleRepositoryImpl()
                    val diDir = dir.resolve("di").also { it.createDirectories() }

                    val out = repo.mergeViewModelModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        viewModelSimpleName = "HomeViewModel",
                        viewModelFqn = "com.example.presentation.home.HomeViewModel",
                        dependencyCount = 2,
                    )

                    out.status shouldBe "created"
                    val text = out.outPath.readText()
                    text.contains("module") shouldBe true
                    text.contains("viewModel { HomeViewModel(get(), get()) }") shouldBe true
                    text.contains("import com.example.presentation.home.HomeViewModel") shouldBe true
                }
            }
        }

        When("module file exists and already contains binding") {
            Then("it should return exists") {
                TestFiles.withTempDir("pg_dimodule") { dir ->
                    val repo = PresentationDiModuleRepositoryImpl()
                    val diDir = dir.resolve("di").also { it.createDirectories() }

                    // First create
                    repo.mergeViewModelModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        viewModelSimpleName = "HomeViewModel",
                        viewModelFqn = "com.example.presentation.home.HomeViewModel",
                        dependencyCount = 0,
                    )

                    val out = repo.mergeViewModelModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        viewModelSimpleName = "HomeViewModel",
                        viewModelFqn = "com.example.presentation.home.HomeViewModel",
                        dependencyCount = 1,
                    )

                    out.status shouldBe "exists"
                }
            }
        }

        When("module file exists and needs a new binding") {
            Then("it should insert import and binding") {
                TestFiles.withTempDir("pg_dimodule") { dir ->
                    val repo = PresentationDiModuleRepositoryImpl()
                    val diDir = dir.resolve("di").also { it.createDirectories() }

                    // Create with one VM
                    repo.mergeViewModelModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        viewModelSimpleName = "FirstViewModel",
                        viewModelFqn = "com.example.presentation.first.FirstViewModel",
                        dependencyCount = 0,
                    )

                    val out = repo.mergeViewModelModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        viewModelSimpleName = "SecondViewModel",
                        viewModelFqn = "com.example.presentation.second.SecondViewModel",
                        dependencyCount = 1,
                    )

                    out.status shouldBe "updated"

                    val text = out.outPath.readText()
                    text.contains("import com.example.presentation.second.SecondViewModel") shouldBe true
                    text.contains("viewModel { SecondViewModel(get()) }") shouldBe true
                }
            }
        }

        When("module file exists with no imports") {
            Then("it should insert import after package line") {
                TestFiles.withTempDir("pg_dimodule_noimports") { dir ->
                    val repo = PresentationDiModuleRepositoryImpl()
                    val diDir = dir.resolve("di").also { it.createDirectories() }

                    val diTargetDir = diDir.resolve("src/main/kotlin/com/example/di").also { it.createDirectories() }
                    val outFile = diTargetDir.resolve("ViewModelModule.kt")
                    outFile.toFile().writeText(
                        """
                        package com.example.di
                        
                        val viewModelModule = module {
                        }
                        """.trimIndent()
                    )

                    val out = repo.mergeViewModelModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        viewModelSimpleName = "XViewModel",
                        viewModelFqn = "com.example.presentation.x.XViewModel",
                        dependencyCount = 0,
                    )

                    out.status shouldBe "updated"
                    val text = out.outPath.readText()
                    text.contains("import com.example.presentation.x.XViewModel") shouldBe true
                    text.contains("viewModel { XViewModel() }") shouldBe true
                }
            }
        }

        When("module file exists with no package declaration") {
            Then("it should insert import at top") {
                TestFiles.withTempDir("pg_dimodule_nopackage") { dir ->
                    val repo = PresentationDiModuleRepositoryImpl()
                    val diDir = dir.resolve("di").also { it.createDirectories() }

                    val diTargetDir = diDir.resolve("src/main/kotlin/com/example/di").also { it.createDirectories() }
                    val outFile = diTargetDir.resolve("ViewModelModule.kt")
                    outFile.toFile().writeText(
                        """
                        val viewModelModule = module {
                        }
                        """.trimIndent()
                    )

                    val out = repo.mergeViewModelModule(
                        diDir = diDir,
                        diPackage = "com.example.di",
                        viewModelSimpleName = "YViewModel",
                        viewModelFqn = "com.example.presentation.y.YViewModel",
                        dependencyCount = 1,
                    )

                    out.status shouldBe "updated"
                    val text = out.outPath.readText()
                    text.lines().first().trim() shouldBe "import com.example.presentation.y.YViewModel"
                    text.contains("viewModel { YViewModel(get()) }") shouldBe true
                }
            }
        }
    }
})
