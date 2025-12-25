package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.fakes.FakeFileSystemRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import java.nio.file.Path

/**
 * Test class for [AnnotationModuleRepositoryImpl].
 */
class AnnotationModuleRepositoryImplTest : BehaviorSpec({

    Given("an annotation module repository") {
        val fakeFs = FakeFileSystemRepository()
        val repository = AnnotationModuleRepositoryImpl(fakeFs)

        When("generating annotation module") {
            val outputDir = Path.of("output")
            val packageName = "com.example.di"
            val scanPackage = "com.example.feature"
            val featureName = "myFeature"

            repository.generate(outputDir, packageName, scanPackage, featureName)

            Then("it should generate correct content") {
                val content = fakeFs.writtenFiles.values.first()
                content shouldContain "package com.example.di"
                content shouldContain "import org.koin.core.annotation.ComponentScan"
                content shouldContain "import org.koin.core.annotation.Module"
                content shouldContain "@Module"
                content shouldContain "@ComponentScan(\"com.example.feature\")"
                content shouldContain "class MyFeatureAnnotationsModule"
            }
        }
    }
})
