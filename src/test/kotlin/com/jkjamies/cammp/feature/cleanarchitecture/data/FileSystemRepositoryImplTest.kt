package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles
import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe

/**
 * Tests for [FileSystemRepositoryImpl].
 */
class FileSystemRepositoryImplTest : BehaviorSpec({

    Given("FileSystemRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()

        When("writing and reading a file") {
            Then("it should create parent directories and read back content") {
                withTempDir("cammp_fs_repo") { tmp ->
                    val file = tmp.resolve("a/b/c.txt")

                    fs.exists(file) shouldBe false
                    fs.isDirectory(tmp) shouldBe true

                    fs.writeText(file, "hello", overwriteIfExists = false)

                    fs.exists(file) shouldBe true
                    fs.readText(file) shouldBe "hello"

                    // should not overwrite when overwriteIfExists=false
                    fs.writeText(file, "changed", overwriteIfExists = false)
                    fs.readText(file) shouldBe "hello"

                    // should overwrite when overwriteIfExists=true
                    fs.writeText(file, "changed", overwriteIfExists = true)
                    fs.readText(file) shouldBe "changed"
                }
            }
        }

        When("reading a missing file") {
            Then("it should return null") {
                withTempDir("cammp_fs_repo_missing") { tmp ->
                    val file = tmp.resolve("missing.txt")
                    fs.readText(file) shouldBe null
                }
            }
        }
    }
})
