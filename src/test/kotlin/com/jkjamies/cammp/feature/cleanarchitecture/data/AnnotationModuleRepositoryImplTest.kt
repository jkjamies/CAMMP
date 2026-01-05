package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.testutil.TestFiles.withTempDir
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain

/**
 * Tests for [AnnotationModuleRepositoryImpl].
 */
class AnnotationModuleRepositoryImplTest : BehaviorSpec({

    Given("AnnotationModuleRepositoryImpl") {
        val fs = FileSystemRepositoryImpl()
        val repo = AnnotationModuleRepositoryImpl(fs)

        When("generating a Koin annotations module") {
            Then("it should write a class with non-backticked annotation imports") {
                withTempDir("cammp_koin_module") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.feature.di",
                        scanPackage = "com.example.feature",
                        featureName = "profile",
                    )

                    val outFile = tmp.resolve("ProfileAnnotationsModule.kt")
                    fs.readText(outFile)!!.also { content ->
                        content shouldContain "package com.example.feature.di"
                        content shouldContain "class ProfileAnnotationsModule"
                        content shouldContain "@Module"
                        content shouldContain "@ComponentScan(\"com.example.feature\")"

                        // Ensure kotlinpoet's backticked import is normalized
                        content shouldContain "import org.koin.core.annotation.ComponentScan"
                        content shouldContain "import org.koin.core.annotation.Module"
                    }
                }
            }
        }

        When("featureName is already titlecase") {
            Then("it should keep the same capitalization") {
                withTempDir("cammp_koin_module2") { tmp ->
                    repo.generate(
                        outputDirectory = tmp,
                        packageName = "com.example.feature.di",
                        scanPackage = "com.example.feature",
                        featureName = "Profile",
                    )

                    val outFile = tmp.resolve("ProfileAnnotationsModule.kt")
                    fs.readText(outFile)!!.also { content ->
                        content shouldContain "class ProfileAnnotationsModule"
                    }
                }
            }
        }
    }
})
