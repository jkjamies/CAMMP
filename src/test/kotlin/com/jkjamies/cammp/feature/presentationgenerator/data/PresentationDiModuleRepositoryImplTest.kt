package com.jkjamies.cammp.feature.presentationgenerator.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

class PresentationDiModuleRepositoryImplTest : BehaviorSpec({

    Given("a DI module repository") {
        val repository = PresentationDiModuleRepositoryImpl()
        val tempDir = Files.createTempDirectory("di_gen_test")

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("merging a new ViewModel module") {
            val diDir = tempDir.resolve("di")
            val diPackage = "com.example.di"
            val viewModelSimpleName = "TestViewModel"
            val viewModelFqn = "com.example.presentation.TestViewModel"
            val dependencyCount = 2
            
            val result = repository.mergeViewModelModule(
                diDir = diDir,
                diPackage = diPackage,
                viewModelSimpleName = viewModelSimpleName,
                viewModelFqn = viewModelFqn,
                dependencyCount = dependencyCount
            )

            Then("it should create the file") {
                result.status shouldBe "created"
                Files.exists(result.outPath) shouldBe true
            }

            Then("it should contain the module definition") {
                val content = result.outPath.readText()
                content shouldContain "package com.example.di"
                content shouldContain "val viewModelModule"
                content shouldContain "module {"
                content shouldContain "viewModel { TestViewModel(get(), get()) }"
                content shouldContain "import com.example.presentation.TestViewModel"
            }
        }

        When("merging into an existing ViewModel module") {
            val diDir = tempDir.resolve("di_existing")
            val diPackage = "com.example.di"
            val viewModelSimpleName = "NewViewModel"
            val viewModelFqn = "com.example.presentation.NewViewModel"
            
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/ViewModelModule.kt")
            existingFile.parent.createDirectories()
            existingFile.writeText(
                """
                package com.example.di
                
                import org.koin.dsl.module
                import org.koin.androidx.viewmodel.dsl.viewModel
                
                val viewModelModule = module {
                }
                """.trimIndent()
            )
            
            val result = repository.mergeViewModelModule(
                diDir = diDir,
                diPackage = diPackage,
                viewModelSimpleName = viewModelSimpleName,
                viewModelFqn = viewModelFqn,
                dependencyCount = 0
            )

            Then("it should update the file") {
                result.status shouldBe "updated"
            }

            Then("it should contain the new binding") {
                val content = result.outPath.readText()
                content shouldContain "viewModel { NewViewModel() }"
                content shouldContain "import com.example.presentation.NewViewModel"
            }
        }

        When("merging a duplicate binding") {
            val diDir = tempDir.resolve("di_duplicate")
            val diPackage = "com.example.di"
            val viewModelSimpleName = "DuplicateViewModel"
            val viewModelFqn = "com.example.presentation.DuplicateViewModel"
            
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/ViewModelModule.kt")
            existingFile.parent.createDirectories()
            existingFile.writeText(
                """
                package com.example.di
                
                import org.koin.dsl.module
                import org.koin.androidx.viewmodel.dsl.viewModel
                import com.example.presentation.DuplicateViewModel
                
                val viewModelModule = module {
                    viewModel { DuplicateViewModel() }
                }
                """.trimIndent()
            )
            
            val result = repository.mergeViewModelModule(
                diDir = diDir,
                diPackage = diPackage,
                viewModelSimpleName = viewModelSimpleName,
                viewModelFqn = viewModelFqn,
                dependencyCount = 0
            )

            Then("it should report as exists") {
                result.status shouldBe "exists"
            }
        }
    }
})
