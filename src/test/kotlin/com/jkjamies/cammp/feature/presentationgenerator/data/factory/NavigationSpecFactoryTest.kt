package com.jkjamies.cammp.feature.presentationgenerator.data.factory

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain

class NavigationSpecFactoryTest : BehaviorSpec({

    val factory = NavigationSpecFactoryImpl()

    Given("a NavigationSpecFactory") {
        When("creating a Navigation Host") {
            val spec = factory.createHost("com.example.presentation.navigation", "TestNavHost")
            val content = spec.toString()

            Then("it should generate a NavHost composable") {
                content shouldContain "@Composable"
                content shouldContain "fun TestNavHost("
                content shouldContain "NavHost(navController = navController"
            }
        }

        When("creating a Navigation Destination") {
            val spec = factory.createDestination("com.example.presentation", "Test", "test")
            val content = spec.toString()

            Then("it should generate a destination function") {
                content shouldContain "fun NavGraphBuilder.TestDestination()"
                content shouldContain "composable(\"TestDestination\")"
                content shouldContain "Test()"
            }
        }
    }
})
