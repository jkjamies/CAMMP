package com.jkjamies.cammp.feature.repositorygenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.repositorygenerator.data.datasource.PackageMetadataDataSource
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceDiscoveryRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

@ContributesBinding(AppScope::class)
@Inject
class DataSourceDiscoveryRepositoryImpl(
    private val packageMetadataDataSource: PackageMetadataDataSource
) : DataSourceDiscoveryRepository {
    override fun loadDataSourcesByType(dataModulePath: String): Map<String, List<String>> {
        return try {
            val moduleDir = Paths.get(dataModulePath)
            val kotlinRoot = moduleDir.resolve("src/main/kotlin")
            if (!kotlinRoot.exists()) return emptyMap()

            val basePkg = packageMetadataDataSource.findModulePackage(moduleDir)

            val combinedPkg = "$basePkg.dataSource"
            val remotePkg = "$basePkg.remoteDataSource"
            val localPkg = "$basePkg.localDataSource"

            fun packageToDir(pkg: String) = kotlinRoot.resolve(pkg.replace('.', '/'))

            fun listFqnsInPackage(pkg: String): List<String> {
                val dir = packageToDir(pkg)
                if (!dir.exists() || !dir.isDirectory()) return emptyList()
                return Files.walk(dir).use { stream ->
                    stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
                        .map { path ->
                            val simple = path.fileName.toString().removeSuffix(".kt")
                            "$pkg.$simple"
                        }
                        .filter { it.endsWith("DataSource") }
                        .distinct()
                        .sorted()
                        .toList()
                }
            }

            linkedMapOf(
                "Combined" to listFqnsInPackage(combinedPkg),
                "Remote" to listFqnsInPackage(remotePkg),
                "Local" to listFqnsInPackage(localPkg),
            ).filterValues { it.isNotEmpty() }
        } catch (_: Throwable) {
            emptyMap()
        }
    }
}
