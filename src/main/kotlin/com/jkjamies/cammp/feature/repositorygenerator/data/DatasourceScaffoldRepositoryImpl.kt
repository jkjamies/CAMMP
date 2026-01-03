package com.jkjamies.cammp.feature.repositorygenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.repositorygenerator.data.factory.DataSourceSpecFactory
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
@Inject
class DatasourceScaffoldRepositoryImpl(
    private val specFactory: DataSourceSpecFactory
) : DatasourceScaffoldRepository {

    override fun generateInterface(
        directory: Path,
        packageName: String,
        className: String
    ): Path {
        directory.createDirectories()
        val fileSpec = specFactory.createInterface(packageName, className)
        val outFile = directory.resolve("$className.kt")
        outFile.writeText(fileSpec.toString())
        return outFile
    }

    override fun generateImplementation(
        directory: Path,
        packageName: String,
        className: String,
        interfacePackage: String,
        interfaceName: String,
        useKoin: Boolean
    ): Path {
        directory.createDirectories()
        val fileSpec = specFactory.createImplementation(
            packageName = packageName,
            className = className,
            interfacePackage = interfacePackage,
            interfaceName = interfaceName,
            useKoin = useKoin
        )
        val outFile = directory.resolve("$className.kt")
        outFile.writeText(fileSpec.toString())
        return outFile
    }
}
