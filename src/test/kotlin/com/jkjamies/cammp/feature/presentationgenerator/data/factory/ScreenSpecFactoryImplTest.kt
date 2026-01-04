package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for [ScreenSpecFactoryImpl].
 *
 * Reference: `src/main/kotlin/com/jkjamies/cammp/feature/presentationgenerator/data/factory/ScreenSpecFactory.kt`
 */
class ScreenSpecFactoryImplTest : BehaviorSpec({

    Given("ScreenSpecFactoryImpl") {
        val factory = ScreenSpecFactoryImpl()

        When("DI strategy is Hilt") {
            Then("it should use hiltViewModel default and be internal") {
                val spec = factory.create(
                    packageName = "com.example.presentation.home",
                    screenName = "Home",
                    diHilt = true,
                    diKoin = false,
                )

                val text = spec.toString()
                text.contains("internal fun Home") shouldBe true
                text.contains("hiltViewModel") shouldBe true
                text.contains("viewModel: HomeViewModel") shouldBe true
            }
        }

        When("DI strategy is Koin") {
            Then("it should use koinViewModel default") {
                val spec = factory.create(
                    packageName = "com.example.presentation.home",
                    screenName = "Home",
                    diHilt = false,
                    diKoin = true,
                )

                val text = spec.toString()
                text.contains("internal fun Home") shouldBe true
                text.contains("koinViewModel") shouldBe true
                text.contains("viewModel: HomeViewModel") shouldBe true
            }
        }

        When("no DI strategy") {
            Then("it should require an explicit viewModel parameter") {
                val spec = factory.create(
                    packageName = "com.example.presentation.home",
                    screenName = "Home",
                    diHilt = false,
                    diKoin = false,
                )

                val text = spec.toString()
                text.contains("internal fun Home") shouldBe true
                // no default value
                text.contains("= hiltViewModel") shouldBe false
                text.contains("= koinViewModel") shouldBe false
            }
        }
    }
})

