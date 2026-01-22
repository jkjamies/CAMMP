package com.jkjamies.cammp.feature.usecasegenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.usecasegenerator.data.factory.UseCaseSpecFactory
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationResult
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
class UseCaseGenerationRepositoryImpl(
    private val specFactory: UseCaseSpecFactory
) : UseCaseGenerationRepository {

    override fun generateUseCase(
        params: UseCaseParams,
        packageName: String,
        baseDomainPackage: String,
        apiDir: Path?
    ): UseCaseGenerationResult {
        var interfacePath: Path? = null
        val interfaceFqn = if (apiDir?.toFile()?.exists() == true) {
            val interfacePkg = packageName.replace(".domain.", ".api.")
            val interfaceFileSpec = specFactory.createInterface(interfacePkg, params.className)
            val apiTargetDir = apiDir.resolve("src/main/kotlin").resolve(interfacePkg.replace('.', '/'))
            apiTargetDir.createDirectories()
            val out = apiTargetDir.resolve("${params.className}.kt")
            out.writeText(interfaceFileSpec.toString())
            interfacePath = out
            "$interfacePkg.${params.className}"
        } else null

        val fileSpec = specFactory.create(packageName, params, baseDomainPackage, interfaceFqn)
        val content = fileSpec.toString()
            .replace("import org.koin.core.`annotation`.Single", "import org.koin.core.annotation.Single")
        
        val targetDir = params.domainDir.resolve("src/main/kotlin").resolve(packageName.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}.kt")
        out.writeText(content)
        return UseCaseGenerationResult(out, interfacePath)
    }
}
