package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import com.jkjamies.cammp.feature.repositorygenerator.data.factory.RepositorySpecFactory
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.TypeSpec
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText

/**
 * Tests for [RepositoryGenerationRepositoryImpl].
 */
class RepositoryGenerationRepositoryImplTest : BehaviorSpec({

    fun params(projectRoot: Path, className: String) = RepositoryParams(
        dataDir = projectRoot.resolve("data"),
        className = className,
        datasourceStrategy = DatasourceStrategy.None,
        diStrategy = DiStrategy.Hilt,
    )

    Given("RepositoryGenerationRepositoryImpl") {

        When("generateDomainLayer") {
            Then("it writes to <domainDir>/src/main/kotlin/<packagePath>/<ClassName>.kt and sanitizes backticks") {
                withTempDir("repo_gen_domain") { tmp ->
                    val specFactory = io.mockk.mockk<RepositorySpecFactory>()
                    val repo = RepositoryGenerationRepositoryImpl(specFactory)

                    val className = "TestRepository"
                    val packageName = "com.example.domain.repository"
                    val p = params(tmp, className)

                    val dummySpec = FileSpec.builder(packageName, className)
                        .addType(TypeSpec.interfaceBuilder(className).build())
                        .addFileComment("This has `data` in it")
                        .build()

                    io.mockk.every { specFactory.createDomainInterface(packageName, p) } returns dummySpec

                    val out = repo.generateDomainLayer(p, packageName, tmp)

                    out.exists() shouldBe true
                    out.toString() shouldBe tmp
                        .resolve("src/main/kotlin/com/example/domain/repository/$className.kt")
                        .toString()

                    val content = out.readText()
                    content shouldContain "This has data in it"
                    content shouldNotContain "`data`"
                    content shouldContain "interface $className"
                }
            }
        }

        When("generateDataLayer") {
            Then("it writes to <dataDir>/src/main/kotlin/<packagePath>/<ClassName>Impl.kt and rewrites Koin import") {
                withTempDir("repo_gen_data") { tmp ->
                    val specFactory = io.mockk.mockk<RepositorySpecFactory>()
                    val repo = RepositoryGenerationRepositoryImpl(specFactory)

                    val className = "TestRepository"
                    val dataPackage = "com.example.data.repository"
                    val domainPackage = "com.example.domain.repository"
                    val p = params(tmp, className)

                    val dummySpec = FileSpec.builder(dataPackage, "${className}Impl")
                        .addFileComment("force koin import")
                        .addImport("org.koin.core.`annotation`", "Single")
                        .addType(TypeSpec.classBuilder("${className}Impl").build())
                        .build()

                    io.mockk.every { specFactory.createDataImplementation(dataPackage, domainPackage, p) } returns dummySpec

                    val out = repo.generateDataLayer(p, dataPackage, domainPackage)

                    out.exists() shouldBe true
                    out.toString() shouldBe p.dataDir
                        .resolve("src/main/kotlin/com/example/data/repository/${className}Impl.kt")
                        .toString()

                    val content = out.readText()
                    content shouldContain "class ${className}Impl"
                    content shouldNotContain "`data`"
                    content shouldNotContain "import org.koin.core.`annotation`.Single"
                    content shouldContain "import org.koin.core.annotation.Single"
                }
            }
        }
    }
})
