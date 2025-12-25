package com.jkjamies.cammp.feature.presentationgenerator.domain.usecase

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.result.shouldBeSuccess
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Paths

/**
 * Test class for [PresentationGenerator].
 */
class PresentationGeneratorTest : BehaviorSpec({

    val repository = mockk<PresentationRepository>()
    val generator = PresentationGenerator(repository)

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a presentation generator") {

        When("invoking the generator with valid params") {
            val params = PresentationParams(
                moduleDir = Paths.get("/fake/path"),
                screenName = "TestScreen",
                patternMVI = false,
                patternMVVM = true,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false
            )
            coEvery { repository.generate(params) } returns PresentationResult(
                created = listOf("file.kt"),
                skipped = emptyList(),
                message = "Success"
            )

            val result = generator(params)

            Then("it should call the repository's generate method") {
                coVerify(exactly = 1) { repository.generate(params) }
            }

            Then("it should return a success result") {
                result.shouldBeSuccess()
            }
        }

        When("invoking the generator with a blank screen name") {
            val params = PresentationParams(
                moduleDir = Paths.get("/fake/path"),
                screenName = " ",
                patternMVI = false,
                patternMVVM = true,
                diHilt = false,
                diKoin = false,
                diKoinAnnotations = false
            )

            val result = generator(params)

            Then("it should fail without calling the repository") {
                coVerify(exactly = 0) { repository.generate(any()) }
                result.isFailure shouldBe true
            }
        }
    }
})
