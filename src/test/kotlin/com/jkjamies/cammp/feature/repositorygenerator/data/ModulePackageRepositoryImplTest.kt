package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.data.datasource.PackageMetadataDataSource
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Paths

/**
 * Tests for [ModulePackageRepositoryImpl].
 */
class ModulePackageRepositoryImplTest : BehaviorSpec({

    Given("ModulePackageRepositoryImpl") {

        When("findModulePackage is called and datasource returns a package") {
            Then("it delegates and returns the package") {
                val dataSource = mockk<PackageMetadataDataSource>()
                val repository = ModulePackageRepositoryImpl(dataSource)

                val path = Paths.get("some/path")
                every { dataSource.findModulePackage(path) } returns "com.example"

                val result = repository.findModulePackage(path)
                result shouldBe "com.example"
            }
        }

        When("findModulePackage is called and datasource returns null") {
            Then("it throws an error") {
                val dataSource = mockk<PackageMetadataDataSource>()
                val repository = ModulePackageRepositoryImpl(dataSource)

                val path = Paths.get("some/path")
                every { dataSource.findModulePackage(path) } returns null

                shouldThrow<IllegalStateException> {
                    repository.findModulePackage(path)
                }
            }
        }
    }
})