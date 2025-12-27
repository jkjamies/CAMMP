package com.jkjamies.cammp.feature.presentationgenerator.data

import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import java.nio.file.Paths

class ModulePackageRepositoryImplTest : BehaviorSpec({

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

    Given("a ModulePackageRepositoryImpl") {
        val repository = ModulePackageRepositoryImpl()
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
            val result = repository.findModulePackage(modulePath)
            Then("it returns null") {
                result shouldBe null
            }
        }

        When("findModulePackage is called and src/main/kotlin is not found") {
            setupFileSystem(srcFound = false)
            val result = repository.findModulePackage(modulePath)
            Then("it returns null") {
                result shouldBe null
            }
        }

        When("findModulePackage is called and no packages are found") {
            setupFileSystem(files = emptyList())
            val result = repository.findModulePackage(modulePath)
            Then("it returns null") {
                result shouldBe null
            }
        }

        When("findModulePackage finds exact .presentation package") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.presentation",
                "B.kt" to "package com.example.other"
            ))
            val result = repository.findModulePackage(modulePath)
            Then("it returns the exact match") {
                result shouldBe "com.example.presentation"
            }
        }

        When("findModulePackage finds package ending with .domain") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.domain",
                "B.kt" to "package com.example.other"
            ))
            val result = repository.findModulePackage(modulePath)
            Then("it replaces .domain with .presentation") {
                result shouldBe "com.example.presentation"
            }
        }
        
        When("findModulePackage finds package containing .domain.") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.domain.something",
                "B.kt" to "package com.example.other"
            ))
            val result = repository.findModulePackage(modulePath)
            Then("it truncates to .domain and replaces with .presentation") {
                result shouldBe "com.example.presentation"
            }
        }

        When("findModulePackage finds package ending with .data") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.data",
                "B.kt" to "package com.example.other"
            ))
            val result = repository.findModulePackage(modulePath)
            Then("it replaces .data with .presentation") {
                result shouldBe "com.example.presentation"
            }
        }
        
        When("findModulePackage finds package containing .data.") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.data.something",
                "B.kt" to "package com.example.other"
            ))
            val result = repository.findModulePackage(modulePath)
            Then("it truncates to .data and replaces with .presentation") {
                result shouldBe "com.example.presentation"
            }
        }

        When("findModulePackage finds multiple packages, none with domain or data") {
            setupFileSystem(files = listOf(
                "A.kt" to "package com.example.feature.long",
                "B.kt" to "package com.example.feat"
            ))
            val result = repository.findModulePackage(modulePath)
            Then("it chooses the shortest and appends .presentation") {
                result shouldBe "com.example.feat.presentation"
            }
        }
        
        When("findModulePackage finds shortest package that already ends with .presentation") {
             setupFileSystem(files = listOf(
                "A.kt" to "package com.example.feature.presentation"
            ))
            val result = repository.findModulePackage(modulePath)
            Then("it returns it as is") {
                result shouldBe "com.example.feature.presentation"
            }
        }
    }
})
