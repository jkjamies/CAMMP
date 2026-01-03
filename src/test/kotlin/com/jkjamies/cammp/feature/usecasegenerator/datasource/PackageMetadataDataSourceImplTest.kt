package com.jkjamies.cammp.feature.usecasegenerator.datasource

import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import java.nio.file.Paths

/**
 * Test for [PackageMetadataDataSourceImpl].
 */
class PackageMetadataDataSourceImplTest : BehaviorSpec({

    val mockLocalFileSystem = mockk<LocalFileSystem>()
    val mockVirtualFile = mockk<VirtualFile>()
    val mockSrcFile = mockk<VirtualFile>()

    beforeSpec {
        mockkStatic("com.intellij.openapi.vfs.LocalFileSystem")
        mockkStatic("com.intellij.openapi.vfs.VfsUtil")
        mockkStatic("com.intellij.openapi.vfs.VfsUtilCore")
        every { LocalFileSystem.getInstance() } returns mockLocalFileSystem
    }

    afterSpec {
        unmockkAll()
    }

    beforeContainer {
        clearAllMocks()
        every { LocalFileSystem.getInstance() } returns mockLocalFileSystem
    }

    Given("a PackageMetadataDataSourceImpl") {
        val dataSource = PackageMetadataDataSourceImpl()
        val modulePath = Paths.get("/path/to/module")

        fun setupFileSystem(
            moduleFound: Boolean = true,
            srcFound: Boolean = true,
            files: List<Pair<String, String>> = emptyList()
        ) {
            if (!moduleFound) {
                every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns null
                return
            }
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile

            if (!srcFound) {
                every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns null
                return
            }
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile

            every { mockSrcFile.isDirectory } returns true

            val children = files.map { (name, content) ->
                val file = mockk<VirtualFile>()
                every { file.isDirectory } returns false
                every { file.name } returns name
                every { file.contentsToByteArray(false) } returns content.toByteArray()
                file
            }.toTypedArray()

            every { mockSrcFile.children } returns children
        }

        When("findModulePackage is called and module dir is not found") {
            setupFileSystem(moduleFound = false)
            val result = dataSource.findModulePackage(modulePath)
            Then("it returns null") {
                result shouldBe null
            }
        }

        When("findModulePackage is called and src/main/kotlin is not found") {
            setupFileSystem(srcFound = false)
            val result = dataSource.findModulePackage(modulePath)
            Then("it returns null") {
                result shouldBe null
            }
        }

        When("findModulePackage is called and no packages are found") {
            setupFileSystem(files = emptyList())
            val result = dataSource.findModulePackage(modulePath)
            Then("it returns null") {
                result shouldBe null
            }
        }

        When("findModulePackage finds exact .domain.usecase package") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.domain.usecase",
                "B.kt" to "package com.example.other"
            ))
            val result = dataSource.findModulePackage(modulePath)
            Then("it returns the exact match") {
                result shouldBe "com.example.domain.usecase"
            }
        }

        When("findModulePackage finds exact .domain package") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.domain",
                "B.kt" to "package com.example.other"
            ))
            val result = dataSource.findModulePackage(modulePath)
            Then("it appends .usecase") {
                result shouldBe "com.example.domain.usecase"
            }
        }

        When("findModulePackage finds package containing .domain") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.domain.something",
                "B.kt" to "package com.example.other"
            ))
            val result = dataSource.findModulePackage(modulePath)
            Then("it truncates to .domain and appends .usecase") {
                result shouldBe "com.example.domain.usecase"
            }
        }

        When("findModulePackage finds multiple packages, none with domain") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.feature.long",
                "B.kt" to "package com.example.feat"
            ))
            val result = dataSource.findModulePackage(modulePath)
            Then("it chooses the shortest and appends .usecase") {
                result shouldBe "com.example.feat.usecase"
            }
        }

        When("findModulePackage finds shortest package that already ends with .usecase") {
             setupFileSystem(files = listOf(
                "A.kt" to "package com.example.feature.usecase"
            ))
            val result = dataSource.findModulePackage(modulePath)
            Then("it returns it as is") {
                result shouldBe "com.example.feature.usecase"
            }
        }
    }
})
