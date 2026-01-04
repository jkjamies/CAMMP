package com.jkjamies.cammp.feature.repositorygenerator.data.factory

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain

/**
 * Tests for [DataSourceSpecFactoryImpl].
 */
class DataSourceSpecFactoryTest : BehaviorSpec({

    val factory = DataSourceSpecFactoryImpl()

    Given("DataSourceSpecFactoryImpl") {

        When("creating an interface") {
            Then("it generates an interface with the correct package and name") {
                val spec = factory.createInterface("com.example.data", "UserDataSource")
                val content = spec.toString()

                content shouldContain "package com.example.`data`"
                content shouldContain "interface UserDataSource"
            }
        }

        When("creating an implementation with Hilt") {
            Then("it adds @Inject to the primary constructor") {
                val spec = factory.createImplementation(
                    packageName = "com.example.data",
                    className = "UserDataSourceImpl",
                    interfacePackage = "com.example.data",
                    interfaceName = "UserDataSource",
                    useKoin = false,
                )
                val content = spec.toString()

                content shouldContain "class UserDataSourceImpl"
                content shouldContain ": UserDataSource"
                content shouldContain "@Inject"
            }
        }

        When("creating an implementation with Koin") {
            Then("it does not add @Inject") {
                val spec = factory.createImplementation(
                    packageName = "com.example.data",
                    className = "UserDataSourceImpl",
                    interfacePackage = "com.example.data",
                    interfaceName = "UserDataSource",
                    useKoin = true,
                )
                val content = spec.toString()

                content shouldContain "class UserDataSourceImpl"
                content shouldContain ": UserDataSource"
                content shouldNotContain "@Inject"
            }
        }
    }
})
