/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.repositorygenerator.data.factory

import com.jkjamies.cammp.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.domain.model.DatasourceStrategy
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
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

        When("creating data implementation with Metro") {
            val params = mockParams("UserRepository", DiStrategy.Metro)
            val spec = factory.createDataImplementation("p", "d", params)
            val content = spec.toString()

            Then("it should add @ContributesBinding annotation") {
                content shouldContain "ContributesBinding"
                content shouldContain "AppScope"
            }

            Then("it should not have @Inject or javax.inject") {
                content shouldNotContain "import dev.zacsweers.metro.Inject"
                content shouldNotContain "javax.inject"
            }
        }

        When("creating data implementation with Koin annotations") {
            val params = mockParams("UserRepository", DiStrategy.Koin(useAnnotations = true))
            val spec = factory.createDataImplementation("p", "d", params)
            val content = spec.toString()

            Then("it should add Koin @Single annotation") {
                content shouldContain "import org.koin.core.`annotation`.Single"
                content shouldContain "@Single"
            }
        }

        When("creating data implementation with Combined DataSource") {
            val params = mockParams("UserRepository").copy(
                datasourceStrategy = DatasourceStrategy.Combined,
            )
            val spec = factory.createDataImplementation(
                "com.example.data.repository",
                "com.example.domain.repository",
                params
            )
            val content = spec.toString()

            Then("it should inject combined DataSource with Repository suffix retained") {
                content shouldContain "private val userRepositoryDataSource: UserRepositoryDataSource"
                content shouldContain "import com.example.`data`.dataSource.UserRepositoryDataSource"
            }
        }

        When("creating data implementation with Remote and Local DataSources") {
            val params = mockParams("UserRepository").copy(
                datasourceStrategy = DatasourceStrategy.RemoteAndLocal,
            )
            val spec = factory.createDataImplementation(
                "com.example.data.repository",
                "com.example.domain.repository",
                params
            )
            val content = spec.toString()

            Then("it should inject both DataSources with Repository suffix retained") {
                content shouldContain "private val userRepositoryRemoteDataSource: UserRepositoryRemoteDataSource"
                content shouldContain "import com.example.`data`.remoteDataSource.UserRepositoryRemoteDataSource"
                content shouldContain "private val userRepositoryLocalDataSource: UserRepositoryLocalDataSource"
                content shouldContain "import com.example.`data`.localDataSource.UserRepositoryLocalDataSource"
            }
        }

        When("creating data implementation with LocalOnly DataSource") {
            val params = mockParams("UserRepository").copy(
                datasourceStrategy = DatasourceStrategy.LocalOnly,
            )
            val spec = factory.createDataImplementation(
                "com.example.data.repository",
                "com.example.domain.repository",
                params,
            )
            val content = spec.toString()

            Then("it should inject LocalDataSource") {
                content shouldContain "private val userRepositoryLocalDataSource: UserRepositoryLocalDataSource"
                content shouldContain "import com.example.`data`.localDataSource.UserRepositoryLocalDataSource"
            }
        }

        When("creating data implementation with RemoteOnly DataSource") {
            val params = mockParams("UserRepository").copy(
                datasourceStrategy = DatasourceStrategy.RemoteOnly,
            )
            val spec = factory.createDataImplementation(
                "com.example.data.repository",
                "com.example.domain.repository",
                params,
            )
            val content = spec.toString()

            Then("it should inject RemoteDataSource") {
                content shouldContain "private val userRepositoryRemoteDataSource: UserRepositoryRemoteDataSource"
                content shouldContain "import com.example.`data`.remoteDataSource.UserRepositoryRemoteDataSource"
            }
        }

        When("creating data implementation with Selected DataSources") {
            val params = mockParams("UserRepository").copy(
                selectedDataSources = listOf("com.external.OtherDataSource")
            )
            val spec = factory.createDataImplementation(
                "com.example.data.repository",
                "com.example.domain.repository",
                params
            )
            val content = spec.toString()

            Then("it should inject selected DataSources") {
                content shouldContain "private val otherDataSource: OtherDataSource"
                content shouldContain "import com.`external`.OtherDataSource"
            }
        }
    }
})

private fun mockParams(name: String, strategy: DiStrategy = DiStrategy.Hilt) = RepositoryParams(
    dataDir = Paths.get("."),
    className = name,
    datasourceStrategy = DatasourceStrategy.None,
    diStrategy = strategy
)
