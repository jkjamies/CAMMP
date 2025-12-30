package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import io.kotest.core.spec.style.BehaviorSpec
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
