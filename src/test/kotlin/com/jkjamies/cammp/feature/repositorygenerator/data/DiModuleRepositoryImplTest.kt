package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.collections.shouldBeOneOf
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Tests for [DiModuleRepositoryImpl].
 */
class DiModuleRepositoryImplTest : BehaviorSpec({

    fun newProject(root: java.nio.file.Path): java.nio.file.Path {
        root.resolve("src/main/kotlin").createDirectories()
        return root
    }

    val diPackage = "com.example.di"

    Given("DiModuleRepositoryImpl") {

        When("merging Repository Module (Hilt) - new file") {
            Then("it creates RepositoryModule.kt with a bind function") {
                withTempDir("di_repo_test") { root ->
                    val repository = DiModuleRepositoryImpl()
                    val tempDir = newProject(root)

                    val result = repository.mergeRepositoryModule(
                        diDir = tempDir,
                        diPackage = diPackage,
                        className = "UserRepository",
                        domainFqn = "com.example.domain.repository",
                        dataFqn = "com.example.data.repository",
                        useKoin = false
                    )

                    result.status shouldBe "created"
                    val content = result.outPath.readText()
                    content shouldContain "package $diPackage"
                    content shouldContain "@Module"
                    content shouldContain "@InstallIn(SingletonComponent::class)"
                    content shouldContain "abstract class RepositoryModule"
                    content shouldContain "abstract fun bindUserRepository(repositoryImpl: UserRepositoryImpl): UserRepository"
                }
            }
        }

        When("merging Repository Module (Hilt) - existing file already has binding") {
            Then("it returns status=exists and does not duplicate the function") {
                withTempDir("di_repo_test_exists") { root ->
                    val repository = DiModuleRepositoryImpl()
                    val tempDir = newProject(root)
                    val diPath = tempDir.resolve("src/main/kotlin/com/example/di").also { it.createDirectories() }

                    val existingFile = diPath.resolve("RepositoryModule.kt")
                    existingFile.writeText(
                        """
                        package com.example.di

                        import com.example.domain.repository.UserRepository
                        import com.example.data.repository.UserRepositoryImpl
                        import dagger.Binds
                        import dagger.Module
                        import dagger.hilt.InstallIn
                        import dagger.hilt.components.SingletonComponent

                        @Module
                        @InstallIn(SingletonComponent::class)
                        abstract class RepositoryModule {
                            @Binds
                            abstract fun bindUserRepository(repositoryImpl: UserRepositoryImpl): UserRepository
                        }
                        """.trimIndent()
                    )

                    val result = repository.mergeRepositoryModule(
                        diDir = tempDir,
                        diPackage = diPackage,
                        className = "UserRepository",
                        domainFqn = "com.example.domain.repository",
                        dataFqn = "com.example.data.repository",
                        useKoin = false
                    )

                    result.status shouldBeOneOf(listOf("exists", "updated"))
                    val content = result.outPath.readText()
                    content.split("bindUserRepository").size - 1 shouldBe 1
                }
            }
        }

        When("merging Repository Module (Koin) - existing file has content") {
            Then("it inserts a single binding into the module body") {
                withTempDir("di_repo_test_koin") { root ->
                    val repository = DiModuleRepositoryImpl()
                    val tempDir = newProject(root)
                    val diPath = tempDir.resolve("src/main/kotlin/com/example/di").also { it.createDirectories() }

                    val existingFile = diPath.resolve("RepositoryModule.kt")
                    existingFile.writeText(
                        """
                        package com.example.di

                        import org.koin.dsl.module

                        val repositoryModule = module {
                            // existing
                        }
                        """.trimIndent()
                    )

                    val result = repository.mergeRepositoryModule(
                        diDir = tempDir,
                        diPackage = diPackage,
                        className = "KoinRepo",
                        domainFqn = "com.example.domain.repository",
                        dataFqn = "com.example.data.repository",
                        useKoin = true
                    )

                    val content = result.outPath.readText()
                    content shouldContain "val repositoryModule: Module = module {"
                    content shouldContain "single<KoinRepo> { KoinRepoImpl(get()) }"
                }
            }
        }

        When("merging DataSource Module (Koin) - new file") {
            Then("it creates a module with desired bindings") {
                withTempDir("di_repo_test_ds_new") { root ->
                    val repository = DiModuleRepositoryImpl()
                    val tempDir = newProject(root)

                    val bindings = listOf(
                        DataSourceBinding(
                            ifaceImport = "import com.example.data.remote.RemoteDataSource",
                            implImport = "import com.example.data.remote.RemoteDataSourceImpl",
                            signature = "single<RemoteDataSource> { RemoteDataSourceImpl(get()) }",
                            block = "    single<RemoteDataSource> { RemoteDataSourceImpl(get()) }"
                        )
                    )

                    val result = repository.mergeDataSourceModule(
                        diDir = tempDir,
                        diPackage = diPackage,
                        desiredBindings = bindings,
                        useKoin = true
                    )

                    result.status shouldBe "created"
                    val content = result.outPath.readText()
                    content shouldContain "public val dataSourceModule: Module = module {"
                    content shouldContain "single<RemoteDataSource> { RemoteDataSourceImpl(get()) }"
                }
            }
        }

        When("merging DataSource Module (Hilt) - existing file has one function") {
            Then("it appends only missing bindings and preserves existing ones") {
                withTempDir("di_repo_test_ds_hilt") { root ->
                    val repository = DiModuleRepositoryImpl()
                    val tempDir = newProject(root)
                    val diPath = tempDir.resolve("src/main/kotlin/com/example/di").also { it.createDirectories() }

                    val existingFile = diPath.resolve("DataSourceModule.kt")
                    existingFile.writeText(
                        """
                        package com.example.di

                        import com.example.data.ds.LocalDataSource
                        import com.example.data.ds.LocalDataSourceImpl
                        import dagger.Binds
                        import dagger.Module
                        import dagger.hilt.InstallIn
                        import dagger.hilt.components.SingletonComponent

                        @Module
                        @InstallIn(SingletonComponent::class)
                        abstract class DataSourceModule {
                            @Binds
                            abstract fun bindLocalDataSource(dataSourceImpl: LocalDataSourceImpl): LocalDataSource
                        }
                        """.trimIndent()
                    )

                    val bindings = listOf(
                        DataSourceBinding(
                            ifaceImport = "import com.example.data.ds.LocalDataSource",
                            implImport = "import com.example.data.ds.LocalDataSourceImpl",
                            signature = "bindLocalDataSource",
                            block = ""
                        ),
                        DataSourceBinding(
                            ifaceImport = "import com.example.data.ds.RemoteDataSource",
                            implImport = "import com.example.data.ds.RemoteDataSourceImpl",
                            signature = "bindRemoteDataSource",
                            block = ""
                        )
                    )

                    val result = repository.mergeDataSourceModule(
                        diDir = tempDir,
                        diPackage = diPackage,
                        desiredBindings = bindings,
                        useKoin = false
                    )

                    result.status shouldBe "updated"

                    val content = result.outPath.readText()
                    content.split("bindLocalDataSource").size - 1 shouldBe 1
                    content shouldContain "bindRemoteDataSource"
                    content shouldContain "import com.example.data.ds.RemoteDataSource"
                    content shouldContain "import com.example.data.ds.RemoteDataSourceImpl"
                    content shouldNotContain "`data`"
                }
            }
        }
    }
})
