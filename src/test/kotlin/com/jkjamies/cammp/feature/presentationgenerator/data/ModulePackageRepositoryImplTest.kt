package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.data.datasource.PackageMetadataDataSource
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import java.nio.file.Path

/**
 * Tests for [ModulePackageRepositoryImpl].
 */
class ModulePackageRepositoryImplTest : BehaviorSpec({

    // Factory returns *fresh* instances per call so it remains safe under spec/test concurrency.
    fun newRepo(): Pair<PackageMetadataDataSource, ModulePackageRepositoryImpl> {
        val ds = mockk<PackageMetadataDataSource>()
        return ds to ModulePackageRepositoryImpl(ds)
    }

    Given("ModulePackageRepositoryImpl") {

        When("data source returns a package") {
            Then("it should return it and forward the moduleDir") {
                val (ds, repo) = newRepo()

                val dir = Path.of("/tmp/module")
                every { ds.findModulePackage(dir) } returns "com.example.presentation"

                repo.findModulePackage(dir) shouldBe "com.example.presentation"
                verify(exactly = 1) { ds.findModulePackage(dir) }
            }
        }

        When("data source returns null") {
            Then("it should return null") {
                val (ds, repo) = newRepo()

                val dir = Path.of("/tmp/module")
                every { ds.findModulePackage(dir) } returns null

                repo.findModulePackage(dir) shouldBe null
                verify(exactly = 1) { ds.findModulePackage(dir) }
            }
        }
    }
})
