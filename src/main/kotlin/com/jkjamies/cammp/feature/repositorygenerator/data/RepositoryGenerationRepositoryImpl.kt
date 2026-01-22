package com.jkjamies.cammp.feature.repositorygenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.repositorygenerator.data.factory.RepositorySpecFactory
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
class RepositoryGenerationRepositoryImpl(
    private val specFactory: RepositorySpecFactory
) : RepositoryGenerationRepository {

    override fun generateDomainLayer(params: RepositoryParams, packageName: String, domainDir: Path): Path {
        val fileSpec = specFactory.createDomainInterface(packageName, params)
        val targetDir = domainDir.resolve("src/main/kotlin").resolve(packageName.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}.kt")
        out.writeText(fileSpec.toString().replace("`data`", "data"))
        return out
    }

    override fun generateDataLayer(params: RepositoryParams, dataPackage: String, domainPackage: String): Path {
        val fileSpec = specFactory.createDataImplementation(dataPackage, domainPackage, params)
        val targetDir = params.dataDir.resolve("src/main/kotlin").resolve(dataPackage.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}Impl.kt")
        out.writeText(
            fileSpec.toString().replace("`data`", "data")
                .replace("import org.koin.core.`annotation`.Single", "import org.koin.core.annotation.Single")
        )
        return out
    }
}
