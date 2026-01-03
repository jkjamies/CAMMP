package com.jkjamies.cammp.feature.presentationgenerator.datasource

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
 * Tests for [PackageMetadataDataSourceImpl].
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

        When("findModulePackage finds exact .presentation package") {
            setupFileSystem(files = listOf("A.kt" to "package com.example.presentation"))
            val result = dataSource.findModulePackage(modulePath)
            Then("it returns the exact match") {
                result shouldBe "com.example.presentation"
            }
        }

        When("findModulePackage finds package ending with .domain") {
            setupFileSystem(files = listOf("A.kt" to "package com.example.domain"))
            val result = dataSource.findModulePackage(modulePath)
            Then("it replaces .domain with .presentation") {
                result shouldBe "com.example.presentation"
            }
        }

        When("findModulePackage finds multiple packages, none with domain or data") {
            setupFileSystem(files = listOf("A.kt" to "package com.example.feature.long", "B.kt" to "package com.example.feat"))
            val result = dataSource.findModulePackage(modulePath)
            Then("it chooses the shortest and appends .presentation") {
                result shouldBe "com.example.feat.presentation"
            }
        }
    }
})