package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.data.datasource.VersionCatalogDataSource
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.fakes.FakeFileSystemRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import java.nio.file.Path

/**
 * Test class for [AliasesRepositoryImpl].
 */
class AliasesRepositoryImplTest : BehaviorSpec({

    Given("an aliases repository") {
        val fakeFs = FakeFileSystemRepository()
        val tomlPath = Path.of("gradle/libs.versions.toml")
        val outputDir = Path.of("output")
        val packageName = "com.example.convention.core"

        When("generating Aliases file with no pre-existing aliases in toml") {
            val fakeDataSource = FakeVersionCatalogDataSource(useDefault = true)
            val repository = AliasesRepositoryImpl(fakeFs, fakeDataSource)
            repository.generateAliases(outputDir, packageName, DiMode.HILT, tomlPath)
            val content = fakeFs.writtenFiles.values.first()

            Then("it should use default alias names") {
                content shouldContain "const val ANDROID_LIBRARY: String = \"android-library\""
                content shouldContain "const val KOTLINX_SERIALIZATION: String = \"kotlinx-serialization\""
                content shouldContain "const val HILT: String = \"hilt\""
                content shouldContain "const val COMPOSE_BOM: String = \"androidx-compose-bom\""
                content shouldContain "const val KOTEST_RUNNER: String = \"kotest-runner\""
                content shouldContain "const val ANDROIDX_TEST_RUNNER: String = \"androidx-test-runner\""
            }
        }

        When("generating Aliases file with pre-existing aliases in toml") {
            val fakeDataSource = FakeVersionCatalogDataSource(useDefault = false)
            val repository = AliasesRepositoryImpl(fakeFs, fakeDataSource)
            repository.generateAliases(outputDir, packageName, DiMode.HILT, tomlPath)
            val content = fakeFs.writtenFiles.values.first()

            Then("it should use the custom alias names from the toml") {
                content shouldContain "const val ANDROID_LIBRARY: String = \"custom-android-library\""
                content shouldContain "const val KOTLINX_SERIALIZATION: String = \"custom-kotlinx-serialization\""
                content shouldContain "const val HILT: String = \"custom-hilt\""
                content shouldContain "const val COMPOSE_BOM: String = \"custom-androidx-compose-bom\""
                content shouldContain "const val KOTEST_RUNNER: String = \"custom-kotest-runner\""
                content shouldContain "const val ANDROIDX_TEST_RUNNER: String = \"custom-androidx-test-runner\""
            }
        }

        When("generating Aliases file for Koin") {
            val fakeDataSource = FakeVersionCatalogDataSource(useDefault = true)
            val repository = AliasesRepositoryImpl(fakeFs, fakeDataSource)
            repository.generateAliases(outputDir, packageName, DiMode.KOIN, tomlPath)
            val content = fakeFs.writtenFiles.values.first()

            Then("it should contain Koin but not Hilt or Koin Annotations dependencies") {
                content shouldContain "const val KOIN: String = \"koin\""
                content shouldNotContain "const val HILT: String = \"hilt\""
                content shouldNotContain "const val KOIN_CORE: String = \"koin-core\""
            }
        }

        When("generating Aliases file for Koin Annotations") {
            val fakeDataSource = FakeVersionCatalogDataSource(useDefault = true)
            val repository = AliasesRepositoryImpl(fakeFs, fakeDataSource)
            repository.generateAliases(outputDir, packageName, DiMode.KOIN_ANNOTATIONS, tomlPath)
            val content = fakeFs.writtenFiles.values.first()

            Then("it should contain Koin Annotations and KSP but not Hilt or standard Koin") {
                content shouldContain "const val KOIN_CORE: String = \"koin-core\""
                content shouldContain "const val KOIN_ANNOTATIONS: String = \"koin-annotations\""
                content shouldContain "const val KSP: String = \"ksp\""
                content shouldNotContain "const val HILT: String = \"hilt\""
                content shouldNotContain "const val KOIN: String = \"koin\""
            }
        }
    }
})

private class FakeVersionCatalogDataSource(private val useDefault: Boolean) : VersionCatalogDataSource {
    override fun getLibraryAlias(
        tomlPath: Path,
        alias: String,
        group: String,
        artifact: String,
        version: String?
    ): String = if (useDefault) alias else "custom-$alias"

    override fun getPluginAlias(
        tomlPath: Path,
        alias: String,
        id: String,
        version: String?
    ): String = if (useDefault) alias else "custom-$alias"
}
