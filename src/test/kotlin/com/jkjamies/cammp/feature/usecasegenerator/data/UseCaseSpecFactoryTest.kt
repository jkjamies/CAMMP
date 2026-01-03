package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.factory.UseCaseSpecFactoryImpl
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import java.nio.file.Paths

class UseCaseSpecFactoryTest : BehaviorSpec({

    val factory = UseCaseSpecFactoryImpl()

    Given("a UseCaseSpecFactory") {
        When("creating a spec for Hilt") {
            val params = UseCaseParams(
                domainDir = Paths.get("."),
                className = "MyUseCase",
                diStrategy = DiStrategy.Hilt,
                repositories = listOf("MyRepository")
            )

            val spec = factory.create("com.example.usecase", params, "com.example.domain")
            val content = spec.toString()

            Then("it should include Hilt annotations and imports") {
                content shouldContain "package com.example.usecase"
                content shouldContain "import javax.inject.Inject"
                content shouldContain "class MyUseCase @Inject constructor("
                content shouldContain "import com.example.domain.repository.MyRepository"
                content shouldContain "private val myRepository: MyRepository"
                content shouldContain "suspend operator fun invoke()"
            }
        }

        When("creating a spec for Koin with Annotations") {
            val params = UseCaseParams(
                domainDir = Paths.get("."),
                className = "MyUseCase",
                diStrategy = DiStrategy.Koin(useAnnotations = true),
                repositories = listOf("MyRepository")
            )

            val spec = factory.create("com.example.usecase", params, "com.example.domain")
            val content = spec.toString()

            Then("it should include Koin annotations") {
                content shouldContain "import org.koin.core.`annotation`.Single"
                content shouldContain "@Single"
                content shouldContain "class MyUseCase("
            }
        }
    }
})
