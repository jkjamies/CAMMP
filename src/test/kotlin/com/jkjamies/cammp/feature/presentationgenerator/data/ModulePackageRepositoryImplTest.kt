package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Paths

class ModulePackageRepositoryImplTest : BehaviorSpec({

    val dataSource = mockk<PackageMetadataDataSource>()
    val repository = ModulePackageRepositoryImpl(dataSource)

    Given("a ModulePackageRepositoryImpl") {
        When("findModulePackage is called") {
            val path = Paths.get("some/path")
            every { dataSource.findModulePackage(path) } returns "com.example.presentation"
            val result = repository.findModulePackage(path)
            Then("it should return the result from the data source") {
                result shouldBe "com.example.presentation"
            }
        }
    }
})
