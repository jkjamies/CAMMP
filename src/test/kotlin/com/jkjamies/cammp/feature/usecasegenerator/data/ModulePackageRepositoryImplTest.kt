package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.unmockkAll
import java.nio.file.Paths

/**
 * Test for [ModulePackageRepositoryImpl].
 */
class ModulePackageRepositoryImplTest : BehaviorSpec({

    val dataSource = mockk<PackageMetadataDataSource>()
    val repository = ModulePackageRepositoryImpl(dataSource)

    beforeContainer {
        clearAllMocks()
    }

    afterSpec {
        unmockkAll()
    }

    Given("a ModulePackageRepositoryImpl") {
        val modulePath = Paths.get("/path/to/module")

        When("findModulePackage is called") {
            every { dataSource.findModulePackage(modulePath) } returns "com.example.domain.usecase"
            
            val result = repository.findModulePackage(modulePath)
            
            Then("it delegates to dataSource and returns the result") {
                result shouldBe "com.example.domain.usecase"
            }
        }
        
        When("findModulePackage from dataSource returns null") {
            every { dataSource.findModulePackage(modulePath) } returns null
            
            val result = repository.findModulePackage(modulePath)
            
            Then("it returns null") {
                result shouldBe null
            }
        }
    }
})
