package com.jkjamies.cammp.feature.repositorygenerator.data.datasource

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain

class RepoDiModuleDataSourceTest : BehaviorSpec({

    val dataSource = RepoDiModuleDataSourceImpl()

    Given("RepoDiModuleDataSource") {
        When("generating Koin module") {
            val result = dataSource.generateKoinModule(
                packageName = "com.example.di",
                existingContent = null,
                domainFqn = "com.example.domain.Repo",
                dataFqn = "com.example.data.RepoImpl",
                className = "Repo"
            )

            Then("it should contain single definition") {
                result shouldContain "single<Repo> { RepoImpl(get()) }"
                result shouldContain "import com.example.domain.Repo"
                result shouldContain "import com.example.`data`.RepoImpl"
            }
        }

        When("generating Hilt module") {
            val result = dataSource.generateHiltModule(
                packageName = "com.example.di",
                existingContent = null,
                domainFqn = "com.example.domain.Repo",
                dataFqn = "com.example.data.RepoImpl",
                className = "Repo"
            )

            Then("it should contain Binds method") {
                result shouldContain "@Binds"
                result shouldContain "abstract fun bindRepo(repositoryImpl: RepoImpl): Repo"
            }
        }

        When("generating Koin DataSource module") {
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

            Then("it should contain single definition") {
                result shouldContain "single<DS> { DSImpl(get()) }"
            }
        }

        When("generating Hilt DataSource module") {
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

            Then("it should contain Binds method") {
                result shouldContain "abstract fun bindDS(dataSourceImpl: DSImpl): DS"
            }
        }
    }
})
