package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Files
import kotlin.io.path.createDirectories
import kotlin.io.path.readText
import kotlin.io.path.writeText

class DiModuleRepositoryImplTest : BehaviorSpec({

    val repository = DiModuleRepositoryImpl()

    Given("DiModuleRepositoryImpl") {
        val tempDir = Files.createTempDirectory("di_repo_test")
        val diPackage = "com.example.di"
        val diPath = tempDir.resolve("src/main/kotlin/com/example/di")
        diPath.createDirectories()

        When("merging Repository Module (Hilt) - New File") {
            val result = repository.mergeRepositoryModule(
                diDir = tempDir,
                diPackage = diPackage,
                className = "UserRepository",
                domainFqn = "com.example.domain.UserRepository",
                dataFqn = "com.example.data.UserRepositoryImpl",
                useKoin = false
            )

            Then("it should create the file") {
                result.status shouldBe "created"
                val content = result.outPath.readText()
                content shouldContain "@Module"
                content shouldContain "@InstallIn(SingletonComponent::class)"
                content shouldContain "abstract fun bindUserRepository(repositoryImpl: UserRepositoryImpl): UserRepository"
            }
        }

        When("merging Repository Module (Hilt) - Existing File") {
            val existingFile = diPath.resolve("RepositoryModule.kt")
            existingFile.writeText("""
                package com.example.di
                import dagger.Module
                import dagger.hilt.InstallIn
                import dagger.hilt.components.SingletonComponent
                import dagger.Binds
                import com.example.domain.OtherRepo
                import com.example.data.OtherRepoImpl

                @Module
                @InstallIn(SingletonComponent::class)
                abstract class RepositoryModule {
                    @Binds
                    abstract fun bindOtherRepo(impl: OtherRepoImpl): OtherRepo
                }
            """.trimIndent())

            val result = repository.mergeRepositoryModule(
                diDir = tempDir,
                diPackage = diPackage,
                className = "NewRepository",
                domainFqn = "com.example.domain.NewRepository",
                dataFqn = "com.example.data.NewRepositoryImpl",
                useKoin = false
            )

            Then("it should append the new binding") {
                result.status shouldBe "updated"
                val content = result.outPath.readText()
                content shouldContain "fun bindOtherRepo"
                content shouldContain "fun bindNewRepository"
                content shouldContain "import com.example.domain.NewRepository"
            }
        }

        When("merging DataSource Module (Koin) - New File") {
            val bindings = listOf(
                DataSourceBinding(
                    ifaceImport = "import com.example.data.RemoteDataSource",
                    implImport = "import com.example.remote.RemoteDataSourceImpl",
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

            Then("it should create Koin module") {
                result.status shouldBe "created"
                val content = result.outPath.readText()
                content shouldContain "public val dataSourceModule: Module = module {"
                content shouldContain "single<RemoteDataSource> { RemoteDataSourceImpl(get()) }"
            }
        }

        When("merging Repository Module (Koin) - Existing File") {
            val existingFile = diPath.resolve("RepositoryModule.kt")
            existingFile.writeText("""
                package com.example.di
                import org.koin.dsl.module
                val repositoryModule = module {
                }
            """.trimIndent())

            val result = repository.mergeRepositoryModule(
                diDir = tempDir,
                diPackage = diPackage,
                className = "KoinRepo",
                domainFqn = "com.example.domain.KoinRepo",
                dataFqn = "com.example.data.KoinRepoImpl",
                useKoin = true
            )

            Then("it should insert into module block") {
                val content = result.outPath.readText()
                content shouldContain "single<KoinRepo> { KoinRepoImpl(get()) }"
            }
        }
    }
})
