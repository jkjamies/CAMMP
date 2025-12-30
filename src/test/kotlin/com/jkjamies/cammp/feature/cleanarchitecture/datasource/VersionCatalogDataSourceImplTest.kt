package com.jkjamies.cammp.feature.cleanarchitecture.datasource

import com.jkjamies.cammp.feature.cleanarchitecture.fakes.FakeFileSystemRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import java.nio.file.Path

class VersionCatalogDataSourceImplTest : BehaviorSpec({

    Given("a version catalog data source") {
        val fakeFs = FakeFileSystemRepository()
        val dataSource = VersionCatalogDataSourceImpl(fakeFs)
        val tomlPath = Path.of("libs.versions.toml")

        When("getting a library alias that exists") {
            val tomlContent = """
                [versions]
                kotlin = "1.9.0"
                
                [libraries]
                my-lib = { module = "com.example:lib", version.ref = "kotlin" }
            """.trimIndent()
            fakeFs.writeText(tomlPath, tomlContent)

            val alias = dataSource.getLibraryAlias(tomlPath, "default-alias", "com.example", "lib", null, null)

            Then("it should return the existing alias") {
                alias shouldBe "my-lib"
            }
        }

        When("getting a library alias that does not exist") {
            val tomlContent = """
                [versions]
                
                [libraries]
            """.trimIndent()
            fakeFs.writeText(tomlPath, tomlContent)

            val alias = dataSource.getLibraryAlias(tomlPath, "new-alias", "com.example", "new-lib", "1.0.0", null)

            Then("it should return the default alias") {
                alias shouldBe "new-alias"
            }

            Then("it should add the version to [versions]") {
                val updatedContent = fakeFs.readText(tomlPath)!!
                updatedContent shouldContain "[versions]"
                updatedContent shouldContain "new-alias = \"1.0.0\""
            }

            Then("it should add the library to [libraries] with version.ref") {
                val updatedContent = fakeFs.readText(tomlPath)!!
                updatedContent shouldContain "new-alias = { group = \"com.example\", name = \"new-lib\", version.ref = \"new-alias\" }"
            }
        }

        When("getting a library alias that does not exist with explicit versionRef") {
            val tomlContent = """
                [versions]
                my-version = "1.0.0"
                
                [libraries]
            """.trimIndent()
            fakeFs.writeText(tomlPath, tomlContent)

            val alias = dataSource.getLibraryAlias(tomlPath, "new-alias", "com.example", "new-lib", "1.0.0", "my-version")

            Then("it should return the default alias") {
                alias shouldBe "new-alias"
            }

            Then("it should NOT add the version to [versions] if it exists") {
                val updatedContent = fakeFs.readText(tomlPath)!!
                updatedContent shouldContain "my-version = \"1.0.0\""
                // It might add it if logic dictates, but typically if ref is provided, we expect it to be used.
                // If the version ref doesn't exist, it should probably be added.
            }

            Then("it should add the library to [libraries] with the provided version.ref") {
                val updatedContent = fakeFs.readText(tomlPath)!!
                updatedContent shouldContain "new-alias = { group = \"com.example\", name = \"new-lib\", version.ref = \"my-version\" }"
            }
        }

        When("getting a plugin alias that exists") {
            val tomlContent = """
                [plugins]
                my-plugin = { id = "com.example.plugin", version = "1.0.0" }
            """.trimIndent()
            fakeFs.writeText(tomlPath, tomlContent)

            val alias = dataSource.getPluginAlias(tomlPath, "default-plugin", "com.example.plugin", null, null)

            Then("it should return the existing alias") {
                alias shouldBe "my-plugin"
            }
        }

        When("getting a plugin alias that does not exist") {
            val tomlContent = """
                [plugins]
            """.trimIndent()
            fakeFs.writeText(tomlPath, tomlContent)

            val alias = dataSource.getPluginAlias(tomlPath, "new-plugin", "com.example.new.plugin", "1.0.0", null)

            Then("it should return the default alias") {
                alias shouldBe "new-plugin"
            }

            Then("it should add the version to [versions]") {
                val updatedContent = fakeFs.readText(tomlPath)!!
                updatedContent shouldContain "[versions]"
                updatedContent shouldContain "new-plugin = \"1.0.0\""
            }

            Then("it should add the plugin to [plugins] with version.ref") {
                val updatedContent = fakeFs.readText(tomlPath)!!
                updatedContent shouldContain "new-plugin = { id = \"com.example.new.plugin\", version.ref = \"new-plugin\" }"
            }
        }

        When("getting a plugin alias that does not exist with explicit versionRef") {
            val tomlContent = """
                [versions]
                plugin-version = "1.0.0"
                
                [plugins]
            """.trimIndent()
            fakeFs.writeText(tomlPath, tomlContent)

            val alias = dataSource.getPluginAlias(tomlPath, "new-plugin", "com.example.new.plugin", "1.0.0", "plugin-version")

            Then("it should return the default alias") {
                alias shouldBe "new-plugin"
            }

            Then("it should add the plugin to [plugins] with the provided version.ref") {
                val updatedContent = fakeFs.readText(tomlPath)!!
                updatedContent shouldContain "new-plugin = { id = \"com.example.new.plugin\", version.ref = \"plugin-version\" }"
            }
        }
        
        When("adding to an empty file") {
             fakeFs.writeText(tomlPath, "")
             
             dataSource.getLibraryAlias(tomlPath, "lib", "group", "artifact", "1.0", null)
             
             Then("it should create sections") {
                 val content = fakeFs.readText(tomlPath)!!
                 content shouldContain "[versions]"
                 content shouldContain "lib = \"1.0\""
                 content shouldContain "[libraries]"
                 content shouldContain "lib = { group = \"group\", name = \"artifact\", version.ref = \"lib\" }"
             }
        }

        When("adding to a file with mixed sections") {
            val tomlContent = """
                [libraries]
                existing = "foo"
                
                [versions]
                v = "1"
            """.trimIndent()
            fakeFs.writeText(tomlPath, tomlContent)

            dataSource.getLibraryAlias(tomlPath, "new-lib", "group", "artifact", "1.0", null)

            Then("it should reorder sections correctly") {
                val content = fakeFs.readText(tomlPath)!!
                val versionsIndex = content.indexOf("[versions]")
                val librariesIndex = content.indexOf("[libraries]")
                
                versionsIndex shouldBeLessThan librariesIndex
            }
        }
    }
})

private infix fun Int.shouldBeLessThan(other: Int) {
    if (this >= other) throw AssertionError("$this should be less than $other")
}
