package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files

/**
 * Test class for [DiModuleRepositoryImpl].
 */
class DiModuleRepositoryImplTest : BehaviorSpec({

    Given("a DiModuleRepositoryImpl") {
        val repository = DiModuleRepositoryImpl()
        val tempDir = Files.createTempDirectory("di_module_test")
        val diDir = tempDir.resolve("di")
        val diPackage = "com.example.di"

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("merging data source module with Koin") {
            val bindings = listOf(
                DataSourceBinding(
                    ifaceImport = "com.example.data.datasource.RemoteDataSource",
                    implImport = "com.example.data.datasource.RemoteDataSourceImpl",
                    signature = "bindRemoteDataSource",
                    block = ""
                )
            )
            val result = repository.mergeDataSourceModule(diDir, diPackage, bindings, useKoin = true)

            Then("it should create the Koin module") {
                Files.exists(result.outPath)
                val content = Files.readString(result.outPath)
                content shouldContain "module {"
                content shouldContain "single<RemoteDataSource> { RemoteDataSourceImpl(get()) }"
            }
        }

        When("merging data source module with Hilt") {
            val bindings = listOf(
                DataSourceBinding(
                    ifaceImport = "com.example.data.datasource.LocalDataSource",
                    implImport = "com.example.data.datasource.LocalDataSourceImpl",
                    signature = "bindLocalDataSource",
                    block = ""
                )
            )
            val result = repository.mergeDataSourceModule(diDir, diPackage, bindings, useKoin = false)

            Then("it should create the Hilt module") {
                Files.exists(result.outPath)
                val content = Files.readString(result.outPath)
                content shouldContain "@Module"
                content shouldContain "@InstallIn(SingletonComponent::class)"
                content shouldContain "@Binds"
                content shouldContain "abstract fun bindLocalDataSource(dataSourceImpl: LocalDataSourceImpl): LocalDataSource"
            }
        }

        When("merging data source module with Hilt and existing content") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/DataSourceModule.kt")
            Files.createDirectories(existingFile.parent)
            val existingContent = """
                package com.example.di
                
                import dagger.Binds
                import dagger.Module
                import dagger.hilt.InstallIn
                import dagger.hilt.components.SingletonComponent
                import com.example.data.datasource.OldDataSource
                import com.example.data.datasource.OldDataSourceImpl
                
                @Module
                @InstallIn(SingletonComponent::class)
                abstract class DataSourceModule {
                    @Binds
                    abstract fun bindOldDataSource(dataSourceImpl: OldDataSourceImpl): OldDataSource
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val bindings = listOf(
                DataSourceBinding(
                    ifaceImport = "com.example.data.datasource.NewDataSource",
                    implImport = "com.example.data.datasource.NewDataSourceImpl",
                    signature = "bindNewDataSource",
                    block = ""
                )
            )
            val result = repository.mergeDataSourceModule(diDir, diPackage, bindings, useKoin = false)

            Then("it should preserve existing bindings and add new one") {
                val content = Files.readString(result.outPath)
                content shouldContain "fun bindOldDataSource"
                content shouldContain "fun bindNewDataSource"
                content shouldContain "import com.example.data.datasource.OldDataSource"
                content shouldContain "import com.example.data.datasource.OldDataSourceImpl"
            }
        }

        When("merging data source module with Hilt where binding already exists") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/DataSourceModule.kt")
            Files.createDirectories(existingFile.parent)
            val existingContent = """
                package com.example.di
                
                import dagger.Binds
                import dagger.Module
                import dagger.hilt.InstallIn
                import dagger.hilt.components.SingletonComponent
                import com.example.data.datasource.ExistingDataSource
                import com.example.data.datasource.ExistingDataSourceImpl
                
                @Module
                @InstallIn(SingletonComponent::class)
                abstract class DataSourceModule {
                    @Binds
                    abstract fun bindExistingDataSource(dataSourceImpl: ExistingDataSourceImpl): ExistingDataSource
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val bindings = listOf(
                DataSourceBinding(
                    ifaceImport = "com.example.data.datasource.ExistingDataSource",
                    implImport = "com.example.data.datasource.ExistingDataSourceImpl",
                    signature = "bindExistingDataSource",
                    block = ""
                )
            )
            val result = repository.mergeDataSourceModule(diDir, diPackage, bindings, useKoin = false)

            Then("it should not duplicate the binding") {
                val content = Files.readString(result.outPath)
                // Count occurrences of "fun bindExistingDataSource"
                val count = content.split("fun bindExistingDataSource").size - 1
                count shouldBe 1
            }
        }

        When("merging repository module with Koin") {
            val result = repository.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = "UserRepository",
                domainFqn = "com.example.domain.repository.UserRepository",
                dataFqn = "com.example.data.repository.UserRepositoryImpl",
                useKoin = true
            )

            Then("it should create the Koin repository module") {
                Files.exists(result.outPath)
                val content = Files.readString(result.outPath)
                content shouldContain "module {"
                content shouldContain "single<UserRepository> { UserRepositoryImpl(get()) }"
            }
        }

        When("merging repository module with Hilt") {
            val result = repository.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = "AuthRepository",
                domainFqn = "com.example.domain.repository.AuthRepository",
                dataFqn = "com.example.data.repository.AuthRepositoryImpl",
                useKoin = false
            )

            Then("it should create the Hilt repository module") {
                Files.exists(result.outPath)
                val content = Files.readString(result.outPath)
                content shouldContain "@Module"
                content shouldContain "@InstallIn(SingletonComponent::class)"
                content shouldContain "@Binds"
                content shouldContain "abstract fun bindAuthRepository(repositoryImpl: AuthRepositoryImpl): AuthRepository"
            }
        }

        When("merging Hilt repository module with existing content") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/RepositoryModule.kt")
            Files.createDirectories(existingFile.parent)
            val existingContent = """
                package com.example.di
                
                import dagger.Binds
                import dagger.Module
                import dagger.hilt.InstallIn
                import dagger.hilt.components.SingletonComponent
                import com.example.domain.repository.OldRepository
                import com.example.data.repository.OldRepositoryImpl
                
                @Module
                @InstallIn(SingletonComponent::class)
                abstract class RepositoryModule {
                    @Binds
                    abstract fun bindOldRepository(repositoryImpl: OldRepositoryImpl): OldRepository
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val result = repository.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = "NewRepository",
                domainFqn = "com.example.domain.repository.NewRepository",
                dataFqn = "com.example.data.repository.NewRepositoryImpl",
                useKoin = false
            )

            Then("it should preserve existing bindings and add new one") {
                val content = Files.readString(result.outPath)
                content shouldContain "fun bindOldRepository"
                content shouldContain "fun bindNewRepository"
                content shouldContain "import com.example.domain.repository.OldRepository"
                content shouldContain "import com.example.data.repository.OldRepositoryImpl"
            }
        }

        When("merging Hilt repository module where binding already exists") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/RepositoryModule.kt")
            Files.createDirectories(existingFile.parent)
            val existingContent = """
                package com.example.di
                
                import dagger.Binds
                import dagger.Module
                import dagger.hilt.InstallIn
                import dagger.hilt.components.SingletonComponent
                import com.example.domain.repository.ExistingRepository
                import com.example.data.repository.ExistingRepositoryImpl
                
                @Module
                @InstallIn(SingletonComponent::class)
                abstract class RepositoryModule {
                    @Binds
                    abstract fun bindExistingRepository(repositoryImpl: ExistingRepositoryImpl): ExistingRepository
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val result = repository.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = "ExistingRepository",
                domainFqn = "com.example.domain.repository.ExistingRepository",
                dataFqn = "com.example.data.repository.ExistingRepositoryImpl",
                useKoin = false
            )

            Then("it should not duplicate the binding") {
                val content = Files.readString(result.outPath)
                val count = content.split("fun bindExistingRepository").size - 1
                count shouldBe 1
            }
        }
        
        When("merging Hilt repository module with existing content in same package") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/RepositoryModule.kt")
            Files.createDirectories(existingFile.parent)
            // SamePackageRepository is in com.example.di, so no import needed
            val existingContent = """
                package com.example.di
                
                import dagger.Binds
                import dagger.Module
                import dagger.hilt.InstallIn
                import dagger.hilt.components.SingletonComponent
                
                @Module
                @InstallIn(SingletonComponent::class)
                abstract class RepositoryModule {
                    @Binds
                    abstract fun bindSamePackageRepository(repositoryImpl: SamePackageRepositoryImpl): SamePackageRepository
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val result = repository.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = "NewRepository",
                domainFqn = "com.example.domain.repository.NewRepository",
                dataFqn = "com.example.data.repository.NewRepositoryImpl",
                useKoin = false
            )

            Then("it should preserve existing bindings without adding imports for same package types") {
                val content = Files.readString(result.outPath)
                content shouldContain "fun bindSamePackageRepository"
                content shouldContain "fun bindNewRepository"
                content shouldContain "bindSamePackageRepository(repositoryImpl: SamePackageRepositoryImpl): SamePackageRepository"
            }
        }

        When("merging Hilt repository module with empty body") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/RepositoryModule.kt")
            Files.createDirectories(existingFile.parent)
            val existingContent = """
                package com.example.di
                
                import dagger.Module
                import dagger.hilt.InstallIn
                import dagger.hilt.components.SingletonComponent
                
                @Module
                @InstallIn(SingletonComponent::class)
                abstract class RepositoryModule {
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val result = repository.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = "NewRepository",
                domainFqn = "com.example.domain.repository.NewRepository",
                dataFqn = "com.example.data.repository.NewRepositoryImpl",
                useKoin = false
            )

            Then("it should add the new binding") {
                val content = Files.readString(result.outPath)
                content shouldContain "fun bindNewRepository"
            }
        }

        When("merging Koin repository module with existing content containing import without dot") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/RepositoryModule.kt")
            Files.createDirectories(existingFile.parent)
            val existingContent = """
                package com.example.di
                
                import org.koin.dsl.module
                import org.koin.core.module.Module
                import ClassInDefaultPackage
                
                val repositoryModule = module {
                    single { ClassInDefaultPackage() }
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val result = repository.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = "NewRepository",
                domainFqn = "com.example.domain.repository.NewRepository",
                dataFqn = "com.example.data.repository.NewRepositoryImpl",
                useKoin = true
            )

            Then("it should preserve the import without dot (or ignore it if invalid but not crash)") {
                val content = Files.readString(result.outPath)
                // KotlinPoet might not add import for default package, but we check it doesn't crash
                // and preserves the module body
                content shouldContain "single { ClassInDefaultPackage() }"
                content shouldContain "single<NewRepository> { NewRepositoryImpl(get()) }"
            }
        }
        
        When("merging Koin repository module with existing content") {
            val existingFile = diDir.resolve("src/main/kotlin/com/example/di/RepositoryModule.kt")
            Files.createDirectories(existingFile.parent)
            val existingContent = """
                package com.example.di
                
                import org.koin.dsl.module
                import org.koin.core.module.Module
                import com.example.domain.repository.OldRepository
                import com.example.data.repository.OldRepositoryImpl
                
                val repositoryModule = module {
                    single<OldRepository> { OldRepositoryImpl(get()) }
                }
            """.trimIndent()
            Files.writeString(existingFile, existingContent)

            val result = repository.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = "NewRepository",
                domainFqn = "com.example.domain.repository.NewRepository",
                dataFqn = "com.example.data.repository.NewRepositoryImpl",
                useKoin = true
            )

            Then("it should preserve existing bindings and add new one") {
                val content = Files.readString(result.outPath)
                content shouldContain "single<OldRepository> { OldRepositoryImpl(get()) }"
                content shouldContain "single<NewRepository> { NewRepositoryImpl(get()) }"
                content shouldContain "import com.example.domain.repository.OldRepository"
                content shouldContain "import com.example.data.repository.OldRepositoryImpl"
            }
        }
    }
})
