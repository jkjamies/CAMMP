package com.jkjamies.cammp.feature.usecasegenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.PackageMetadataDataSource
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

@ContributesBinding(AppScope::class)
@Inject
class RepositoryDiscoveryRepositoryImpl(
    private val packageMetadataDataSource: PackageMetadataDataSource
) : RepositoryDiscoveryRepository {

    override fun loadRepositories(domainModulePath: String): List<String> {
        return try {
            val moduleDir = Paths.get(domainModulePath)
            val useCasePackage = packageMetadataDataSource.findModulePackage(moduleDir)
            val repoPackage = useCasePackage?.let { pkg ->
                if (pkg.endsWith(".usecase")) pkg.removeSuffix(".usecase") + ".repository" else pkg + ".repository"
            } ?: return emptyList()

            val packagePath = repoPackage.replace('.', '/')
            val kotlinRoot = moduleDir.resolve("src/main/kotlin")
            val repoDir = kotlinRoot.resolve(packagePath)
            if (!Files.exists(repoDir) || !Files.isDirectory(repoDir)) return emptyList()

            Files.walk(repoDir)
                .use { stream ->
                    stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
                        .map { it.fileName.toString().removeSuffix(".kt") }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList())
                }
        } catch (t: Throwable) {
            emptyList()
        }
    }
}
