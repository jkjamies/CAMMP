package com.jkjamies.cammp.feature.repositorygenerator.data

import io.kotest.assertions.throwables.shouldThrow
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

/**
 * Test class for [ModulePackageRepositoryImpl].
 */
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

    beforeContainer {
        clearAllMocks()
        every { LocalFileSystem.getInstance() } returns mockLocalFileSystem
    }

    afterSpec {
        unmockkAll()
    }

    // Helper to create mock files
    fun mockFile(name: String, content: String = ""): VirtualFile {
        val f = mockk<VirtualFile>()
        every { f.name } returns name
        every { f.isDirectory } returns false
        every { f.contentsToByteArray(any()) } returns content.toByteArray()
        return f
    }

    // Helper to create mock directories
    fun mockDir(name: String, vararg children: VirtualFile): VirtualFile {
        val d = mockk<VirtualFile>()
        every { d.name } returns name
        every { d.isDirectory } returns true
        every { d.children } returns children as Array<VirtualFile>
        return d
    }

    Given("a ModulePackageRepositoryImpl") {
        val repository = ModulePackageRepositoryImpl()
        val modulePath = Paths.get("/path/to/module")

        When("findModulePackage is called and module dir is not found") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns null

            Then("it should throw an error") {
                shouldThrow<IllegalStateException> {
                    repository.findModulePackage(modulePath)
                }
            }
        }

        When("findModulePackage is called and src/main/kotlin is not found") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns null

            Then("it should throw an error") {
                shouldThrow<IllegalStateException> {
                    repository.findModulePackage(modulePath)
                }
            }
        }

        When("findModulePackage is called and no packages are found") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile
            every { mockSrcFile.isDirectory } returns true
            every { mockSrcFile.children } returns emptyArray()

            Then("it should throw an error") {
                shouldThrow<IllegalStateException> {
                    repository.findModulePackage(modulePath)
                }
            }
        }

        When("findModulePackage finds a valid package in root") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile
            every { mockSrcFile.isDirectory } returns true
            
            val file = mockFile("Test.kt", "package com.example.test")
            every { mockSrcFile.children } returns arrayOf(file)
            
            every { mockVirtualFile.name } returns "module"

            val result = repository.findModulePackage(modulePath)

            Then("it should return the package name") {
                result shouldBe "com.example.test"
            }
        }
        
        When("findModulePackage finds a valid package in nested directory") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile
            every { mockSrcFile.isDirectory } returns true
            
            val nestedFile = mockFile("Nested.kt", "package com.example.nested")
            val nestedDir = mockDir("nested", nestedFile)
            
            every { mockSrcFile.children } returns arrayOf(nestedDir)
            
            every { mockVirtualFile.name } returns "module"

            val result = repository.findModulePackage(modulePath)

            Then("it should return the package name from nested file") {
                result shouldBe "com.example.nested"
            }
        }

        When("findModulePackage ignores non-kt files and directories without kt files") {
             every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile
            every { mockSrcFile.isDirectory } returns true
            
            val txtFile = mockFile("readme.txt", "package com.ignore.me")
            val emptyDir = mockDir("empty")
            val validFile = mockFile("Valid.kt", "package com.example.valid")
            
            every { mockSrcFile.children } returns arrayOf(txtFile, emptyDir, validFile)
            every { mockVirtualFile.name } returns "module"
            
            val result = repository.findModulePackage(modulePath)
            
            Then("it should return the valid package") {
                result shouldBe "com.example.valid"
            }
        }

        When("findModulePackage finds multiple packages and module name is 'data'") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile
            every { mockSrcFile.isDirectory } returns true
            
            val file1 = mockFile("A.kt", "package com.example.other")
            val file2 = mockFile("B.kt", "package com.example.data")
            
            every { mockSrcFile.children } returns arrayOf(file1, file2)
            
            val dataPath = Paths.get("/path/to/data")
            every { mockLocalFileSystem.refreshAndFindFileByPath(dataPath.toString()) } returns mockVirtualFile
            every { mockVirtualFile.name } returns "data" 
            
            val result = repository.findModulePackage(dataPath)

            Then("it should prefer the package ending with .data") {
                result shouldBe "com.example.data"
            }
        }
        
        When("findModulePackage finds multiple packages and module name is 'domain'") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile
            every { mockSrcFile.isDirectory } returns true
            
            val file1 = mockFile("A.kt", "package com.example.other")
            val file2 = mockFile("B.kt", "package com.example.domain.something")
            
            every { mockSrcFile.children } returns arrayOf(file1, file2)
            
            val domainPath = Paths.get("/path/to/domain")
            every { mockLocalFileSystem.refreshAndFindFileByPath(domainPath.toString()) } returns mockVirtualFile
            every { mockVirtualFile.name } returns "domain"
            
            val result = repository.findModulePackage(domainPath)

            Then("it should prefer the package containing .domain") {
                result shouldBe "com.example.domain"
            }
        }

        When("findModulePackage finds multiple packages and module name is 'di'") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile
            every { mockSrcFile.isDirectory } returns true
            
            val file1 = mockFile("A.kt", "package com.example.other")
            val file2 = mockFile("B.kt", "package com.example.di")
            
            every { mockSrcFile.children } returns arrayOf(file1, file2)
            
            val diPath = Paths.get("/path/to/di")
            every { mockLocalFileSystem.refreshAndFindFileByPath(diPath.toString()) } returns mockVirtualFile
            every { mockVirtualFile.name } returns "di"
            
            val result = repository.findModulePackage(diPath)

            Then("it should prefer the package ending with .di") {
                result shouldBe "com.example.di"
            }
        }

        When("findModulePackage finds multiple packages, module name is 'data' but no package matches .data") {
            every { mockLocalFileSystem.refreshAndFindFileByPath(any()) } returns mockVirtualFile
            every { VfsUtil.findRelativeFile("src/main/kotlin", mockVirtualFile) } returns mockSrcFile
            every { mockSrcFile.isDirectory } returns true
            
            val file1 = mockFile("A.kt", "package com.example.longname")
            val file2 = mockFile("B.kt", "package com.example.short")
            
            every { mockSrcFile.children } returns arrayOf(file1, file2)
            
            val dataPath = Paths.get("/path/to/data")
            every { mockLocalFileSystem.refreshAndFindFileByPath(dataPath.toString()) } returns mockVirtualFile
            every { mockVirtualFile.name } returns "data" 
            
            val result = repository.findModulePackage(dataPath)

            Then("it should fallback to the shortest package") {
                result shouldBe "com.example.short"
            }
        }
    }
})
