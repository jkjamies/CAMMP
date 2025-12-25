package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.nio.file.Path

class AnnotationModuleRepositoryImplTest : BehaviorSpec({

    Given("an annotation module repository") {
        val mockFs = mockk<FileSystemRepository>(relaxed = true)
        val repository = AnnotationModuleRepositoryImpl(mockFs)

        When("generating annotation module") {
            val outputDir = Path.of("output")
            val packageName = "com.example.di"
            val scanPackage = "com.example.feature"
            val featureName = "myFeature"

            val contentSlot = slot<String>()
            every { mockFs.writeText(any(), capture(contentSlot), any()) } returns Unit

            repository.generate(outputDir, packageName, scanPackage, featureName)

            Then("it should generate correct content") {
                val content = contentSlot.captured
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
