package com.jkjamies.cammp.feature.cleanarchitecture.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import java.nio.file.Files
import kotlin.io.path.createTempDirectory
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/**
 * Test class for [FileSystemRepositoryImpl].
 */
class FileSystemRepositoryImplTest : BehaviorSpec({

    Given("a FileSystemRepositoryImpl") {
        val repository = FileSystemRepositoryImpl()
        val tempDir = createTempDirectory("test_fs_repo")

        afterSpec {
            tempDir.toFile().deleteRecursively()
        }

        When("checking if a file exists") {
            val file = tempDir.resolve("test.txt")
            file.writeText("content")

            Then("it should return true if file exists") {
                repository.exists(file) shouldBe true
            }

            Then("it should return false if file does not exist") {
                repository.exists(tempDir.resolve("nonexistent.txt")) shouldBe false
            }
        }

        When("checking if a path is a directory") {
            val dir = tempDir.resolve("subdir")
            Files.createDirectories(dir)

            Then("it should return true for a directory") {
                repository.isDirectory(dir) shouldBe true
            }

            Then("it should return false for a file") {
                val file = tempDir.resolve("file.txt")
                file.writeText("content")
                repository.isDirectory(file) shouldBe false
            }
        }

        When("creating directories") {
            val dir = tempDir.resolve("new/nested/dir")

            Then("it should create the directory structure") {
                repository.createDirectories(dir)
                dir.exists() shouldBe true
            }
        }

        When("writing text to a file") {
            val file = tempDir.resolve("write_test.txt")

            Then("it should write content to the file") {
                repository.writeText(file, "Hello World", overwriteIfExists = true)
                file.readText() shouldBe "Hello World"
            }

            Then("it should not overwrite if overwriteIfExists is false and file exists") {
                repository.writeText(file, "New Content", overwriteIfExists = false)
                file.readText() shouldBe "Hello World"
            }

            Then("it should overwrite if overwriteIfExists is true") {
                repository.writeText(file, "New Content", overwriteIfExists = true)
                file.readText() shouldBe "New Content"
            }

            Then("it should create parent directories if they don't exist") {
                val nestedFile = tempDir.resolve("nested/write/test.txt")
                repository.writeText(nestedFile, "Nested Content", overwriteIfExists = true)
                nestedFile.exists() shouldBe true
                nestedFile.readText() shouldBe "Nested Content"
            }
        }

        When("reading text from a file") {
            val file = tempDir.resolve("read_test.txt")
            file.writeText("Read Me")

            Then("it should return the content if file exists") {
                repository.readText(file) shouldBe "Read Me"
            }

            Then("it should return null if file does not exist") {
                repository.readText(tempDir.resolve("missing.txt")) shouldBe null
            }
        }
    }
})
