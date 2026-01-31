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

package com.jkjamies.cammp.feature.repositorygenerator.data.datasource

import com.jkjamies.cammp.feature.repositorygenerator.datasource.RepoDiModuleDataSourceImpl
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain

/**
 * Tests for [RepoDiModuleDataSourceImpl].
 */
class RepoDiModuleDataSourceTest : BehaviorSpec({

    val dataSource = RepoDiModuleDataSourceImpl()

    Given("RepoDiModuleDataSourceImpl") {

        When("generating Koin module") {
            Then("it should contain single definition") {
                val result = dataSource.generateKoinModule(
                    packageName = "com.example.di",
                    existingContent = null,
                    domainFqn = "com.example.domain.Repo",
                    dataFqn = "com.example.data.RepoImpl",
                    className = "Repo"
                )

                result shouldContain "single<Repo> { RepoImpl(get()) }"
                result shouldContain "import com.example.domain.Repo"
                // KotlinPoet escapes `data`
                result shouldContain "import com.example.`data`.RepoImpl"
            }
        }

        When("generating Koin module with existing content") {
            Then("it keeps the existing body and adds a new binding") {
                val existing = """
                    package com.example.di

                    import org.koin.dsl.module
                    import org.koin.core.module.Module
                    import com.other.Thing

                    val repositoryModule: Module = module {
                        // existing binding
                        single<Thing> { Thing() }
                    }
                """.trimIndent()

                val result = dataSource.generateKoinModule(
                    packageName = "com.example.di",
                    existingContent = existing,
                    domainFqn = "com.example.domain.Repo",
                    dataFqn = "com.example.data.RepoImpl",
                    className = "Repo"
                )

                result shouldContain "single<Thing> { Thing() }"
                result shouldContain "single<Repo> { RepoImpl(get()) }"
                // should keep non-koin imports
                result shouldContain "import com.other.Thing"
            }
        }

        When("generating Hilt module") {
            Then("it should contain Binds method") {
                val result = dataSource.generateHiltModule(
                    packageName = "com.example.di",
                    existingContent = null,
                    domainFqn = "com.example.domain.Repo",
                    dataFqn = "com.example.data.RepoImpl",
                    className = "Repo"
                )

                result shouldContain "@Binds"
                result shouldContain "abstract fun bindRepo(repositoryImpl: RepoImpl): Repo"
            }
        }

        When("generating Hilt module with existing bind functions") {
            Then("it preserves existing bind signatures and does not duplicate") {
                val existing = """
                    package com.example.di

                    import dagger.Binds
                    import dagger.Module
                    import dagger.hilt.InstallIn
                    import dagger.hilt.components.SingletonComponent
                    import com.example.domain.Other
                    import com.example.data.OtherImpl

                    @Module
                    @InstallIn(SingletonComponent::class)
                    abstract class RepositoryModule {
                        @Binds
                        abstract fun bindOther(repositoryImpl: OtherImpl): Other
                    }
                """.trimIndent()

                val result = dataSource.generateHiltModule(
                    packageName = "com.example.di",
                    existingContent = existing,
                    domainFqn = "com.example.domain.Other",
                    dataFqn = "com.example.data.OtherImpl",
                    className = "Other"
                )

                // should keep existing, only once
                result.split("bindOther").size - 1 shouldBe 1
            }
        }

        When("generating Koin DataSource module") {
            Then("it should contain single definition") {
                val bindings = listOf(
                    DataSourceBinding(
                        ifaceImport = "import com.example.data.DS",
                        implImport = "import com.example.remote.DSImpl",
                        signature = "single<DS> { DSImpl(get()) }",
                        block = ""
                    )
                )
                val result = dataSource.generateKoinDataSourceModule(
                    packageName = "com.example.di",
                    existingContent = null,
                    bindings = bindings
                )

                result shouldContain "single<DS> { DSImpl(get()) }"
            }
        }

        When("generating Hilt DataSource module") {
            Then("it should contain Binds method") {
                val bindings = listOf(
                    DataSourceBinding(
                        ifaceImport = "import com.example.data.DS",
                        implImport = "import com.example.remote.DSImpl",
                        signature = "",
                        block = ""
                    )
                )
                val result = dataSource.generateHiltDataSourceModule(
                    packageName = "com.example.di",
                    existingContent = null,
                    bindings = bindings
                )

                result shouldContain "abstract fun bindDS(dataSourceImpl: DSImpl): DS"
            }
        }

        When("generating Hilt DataSource module with existing function") {
            Then("it does not duplicate bind function") {
                val existing = """
                    package com.example.di

                    import dagger.Binds
                    import dagger.Module
                    import dagger.hilt.InstallIn
                    import dagger.hilt.components.SingletonComponent
                    import com.example.data.DS
                    import com.example.remote.DSImpl

                    @Module
                    @InstallIn(SingletonComponent::class)
                    abstract class DataSourceModule {
                        @Binds
                        abstract fun bindDS(dataSourceImpl: DSImpl): DS
                    }
                """.trimIndent()

                val bindings = listOf(
                    DataSourceBinding(
                        ifaceImport = "import com.example.data.DS",
                        implImport = "import com.example.remote.DSImpl",
                        signature = "bindDS",
                        block = ""
                    )
                )

                val result = dataSource.generateHiltDataSourceModule(
                    packageName = "com.example.di",
                    existingContent = existing,
                    bindings = bindings
                )

                result.split("bindDS").size - 1 shouldBe 1
            }
        }
    }
})
