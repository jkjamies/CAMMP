package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import java.nio.file.Path

/**
 * Tests for [ModulePackageRepositoryImpl].
 */
class ModulePackageRepositoryImplTest : BehaviorSpec({

    fun newRepo(): Pair<PackageMetadataDataSource, ModulePackageRepositoryImpl> {
        val ds = mockk<PackageMetadataDataSource>()
        return ds to ModulePackageRepositoryImpl(ds)
    }

    Given("ModulePackageRepositoryImpl") {
        val modulePath = Path.of("/path/to/module")

        When("data source returns a package") {
            Then("it should delegate and return it") {
                val (ds, repo) = newRepo()
                every { ds.findModulePackage(modulePath) } returns "com.example.domain.usecase"

                repo.findModulePackage(modulePath) shouldBe "com.example.domain.usecase"
            }
        }

        When("data source returns null") {
            Then("it should return null") {
                val (ds, repo) = newRepo()
                every { ds.findModulePackage(modulePath) } returns null

                repo.findModulePackage(modulePath) shouldBe null
            }
        }
    }
})
