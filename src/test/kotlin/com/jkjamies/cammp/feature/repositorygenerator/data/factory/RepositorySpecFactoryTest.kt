package com.jkjamies.cammp.feature.repositorygenerator.data.factory

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import java.nio.file.Paths

class RepositorySpecFactoryTest : BehaviorSpec({

    val factory = RepositorySpecFactoryImpl()

    Given("a RepositorySpecFactory") {
        When("creating domain interface") {
            val params = mockParams("UserRepository")
            val spec = factory.createDomainInterface("com.example.domain", params)
            val content = spec.toString()

            Then("it should generate interface with correct name") {
                content shouldContain "package com.example.domain"
                content shouldContain "interface UserRepository"
            }
        }

        When("creating data implementation") {
            val params = mockParams("UserRepository")
            val spec = factory.createDataImplementation(
                "com.example.data",
                "com.example.domain",
                params
            )
            val content = spec.toString()

            Then("it should generate class implementing interface") {
                content shouldContain "package com.example.`data`"
                content shouldContain "class UserRepositoryImpl"
                content shouldContain ": UserRepository"
                content shouldContain "import com.example.domain.UserRepository"
            }
        }

        When("creating data implementation with Hilt") {
            val params = mockParams("UserRepository", DiStrategy.Hilt)
            val spec = factory.createDataImplementation("p", "d", params)
            val content = spec.toString()

            Then("it should add Inject annotation") {
                content shouldContain "@Inject"
            }
        }

        When("creating data implementation with Combined DataSource") {
            val params = mockParams("UserRepository").copy(
                includeDatasource = true,
                datasourceCombined = true
            )
            val spec = factory.createDataImplementation(
                "com.example.data.repository",
                "com.example.domain.repository",
                params
            )
            val content = spec.toString()

            Then("it should inject combined DataSource") {
                content shouldContain "private val userDataSource: UserDataSource"
                content shouldContain "import com.example.`data`.dataSource.UserDataSource"
            }
        }

        When("creating data implementation with Remote and Local DataSources") {
            val params = mockParams("UserRepository").copy(
                includeDatasource = true,
                datasourceCombined = false,
                datasourceRemote = true,
                datasourceLocal = true
            )
            val spec = factory.createDataImplementation(
                "com.example.data.repository",
                "com.example.domain.repository",
                params
            )
            val content = spec.toString()

            Then("it should inject both DataSources") {
                content shouldContain "private val userRemoteDataSource: UserRemoteDataSource"
                content shouldContain "import com.example.`data`.remoteDataSource.UserRemoteDataSource"
                content shouldContain "private val userLocalDataSource: UserLocalDataSource"
                content shouldContain "import com.example.`data`.localDataSource.UserLocalDataSource"
            }
        }

        When("creating data implementation with Selected DataSources") {
            val params = mockParams("UserRepository").copy(
                selectedDataSources = listOf("com.other.OtherDataSource")
            )
            val spec = factory.createDataImplementation(
                "com.example.data.repository",
                "com.example.domain.repository",
                params
            )
            val content = spec.toString()

            Then("it should inject selected DataSources") {
                content shouldContain "private val otherDataSource: OtherDataSource"
                content shouldContain "import com.other.OtherDataSource"
            }
        }
    }
})

private fun mockParams(name: String, strategy: DiStrategy = DiStrategy.Hilt) = RepositoryParams(
    dataDir = Paths.get("."),
    className = name,
    includeDatasource = false,
    datasourceCombined = false,
    datasourceRemote = false,
    datasourceLocal = false,
    diStrategy = strategy
)
