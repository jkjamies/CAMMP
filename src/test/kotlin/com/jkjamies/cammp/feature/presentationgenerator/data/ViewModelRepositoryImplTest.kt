package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.readText

class ViewModelRepositoryImplTest : BehaviorSpec({

    Given("a ViewModel repository") {
        val repository = ViewModelRepositoryImpl()
        val tempDir = Files.createTempDirectory("viewmodel_gen_test")

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("generating a basic ViewModel") {
            val packageName = "com.example.test"
            val screenName = "TestScreen"
            
            val result = repository.generateViewModel(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false,
                patternMVI = false,
                useCaseFqns = emptyList()
            )

            Then("it should create the file") {
                result.status shouldBe GenerationStatus.CREATED
                Files.exists(result.path) shouldBe true
            }

            Then("it should contain the basic structure") {
                val content = result.path.readText()
                content shouldContain "package com.example.test"
                content shouldContain "class TestScreenViewModel"
                content shouldContain ": ViewModel()"
            }
        }

        When("generating a Hilt ViewModel") {
            val packageName = "com.example.test"
            val screenName = "HiltScreen"
            
            val result = repository.generateViewModel(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = true,
                diKoin = false,
                diKoinAnnotations = false,
                patternMVI = false,
                useCaseFqns = emptyList()
            )

            Then("it should contain Hilt annotations") {
                val content = result.path.readText()
                content shouldContain "@HiltViewModel"
                content shouldContain "@Inject constructor"
            }
        }

        When("generating a Koin ViewModel") {
            val packageName = "com.example.test"
            val screenName = "KoinScreen"
            
            val result = repository.generateViewModel(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = false,
                diKoin = true,
                diKoinAnnotations = false,
                patternMVI = false,
                useCaseFqns = emptyList()
            )

            Then("it should be internal") {
                val content = result.path.readText()
                content shouldContain "internal class KoinScreenViewModel"
            }
        }

        When("generating a Koin Annotated ViewModel") {
            val packageName = "com.example.test"
            val screenName = "KoinAnnotatedScreen"
            
            val result = repository.generateViewModel(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = false,
                diKoin = true,
                diKoinAnnotations = true,
                patternMVI = false,
                useCaseFqns = emptyList()
            )

            Then("it should contain Koin annotations") {
                val content = result.path.readText()
                content shouldContain "@KoinViewModel"
                content shouldContain "import org.koin.android.annotation.KoinViewModel"
            }
        }

        When("generating an MVI ViewModel") {
            val packageName = "com.example.test"
            val screenName = "MviScreen"
            
            val result = repository.generateViewModel(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false,
                patternMVI = true,
                useCaseFqns = emptyList()
            )

            Then("it should contain intent handler") {
                val content = result.path.readText()
                content shouldContain "fun handleIntent(intent: MviScreenIntent)"
            }
        }

        When("generating a ViewModel with UseCases") {
            val packageName = "com.example.test"
            val screenName = "UseCaseScreen"
            val useCases = listOf("com.example.domain.usecase.GetSomethingUseCase")
            
            val result = repository.generateViewModel(
                targetDir = tempDir,
                packageName = packageName,
                screenName = screenName,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false,
                patternMVI = false,
                useCaseFqns = useCases
            )

            Then("it should inject the use case") {
                val content = result.path.readText()
                content shouldContain "private val getSomethingUseCase: GetSomethingUseCase"
                content shouldContain "import com.example.domain.usecase.GetSomethingUseCase"
            }
        }
    }
})
