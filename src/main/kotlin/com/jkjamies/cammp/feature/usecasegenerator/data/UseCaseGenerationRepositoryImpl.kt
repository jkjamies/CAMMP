package com.jkjamies.cammp.feature.usecasegenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.usecasegenerator.data.factory.UseCaseSpecFactory
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
@Inject
class UseCaseGenerationRepositoryImpl(
    private val specFactory: UseCaseSpecFactory
) : UseCaseGenerationRepository {

    override fun generateUseCase(params: UseCaseParams, packageName: String, baseDomainPackage: String): Path {
        val fileSpec = specFactory.create(packageName, params, baseDomainPackage)
        val content = fileSpec.toString()
            .replace("import org.koin.core.`annotation`.Single", "import org.koin.core.annotation.Single")
        
        val targetDir = params.domainDir.resolve("src/main/kotlin").resolve(packageName.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}.kt")
        out.writeText(content)
        return out
    }
}
