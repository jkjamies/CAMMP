package com.github.jkjamies.cammp.feature.repositorygenerator.data

import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceOptions
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.FileSystemRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.TemplateRepository
import java.nio.file.Path

class DatasourceScaffoldRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val templates: TemplateRepository = TemplateRepositoryImpl(),
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl(),
    private val diRepo: DiModuleRepository = DiModuleRepositoryImpl(),
) : DatasourceScaffoldRepository {

    override fun generate(
        dataDir: Path,
        dataBasePkg: String,
        repositoryBaseName: String,
        diDir: Path?,
        diPackage: String?,
        options: DatasourceOptions,
    ): List<String> {
        val results = mutableListOf<String>()
        if (!options.include) return results

        data class Entry(val subDir: String, val classSuffix: String)

        val entries = mutableListOf<Entry>()
        if (options.combined) {
            entries += Entry("dataSource", "DataSource")
        } else {
            if (options.remote) entries += Entry("remoteDataSource", "RemoteDataSource")
            if (options.local) entries += Entry("localDataSource", "LocalDataSource")
        }

        if (entries.isEmpty()) return results

        val dataSrcMainKotlin = dataDir.resolve("src/main/kotlin")
        for (e in entries) {
            val ifacePkg = "$dataBasePkg.${e.subDir}"
            val ifaceDir = dataSrcMainKotlin.resolve(ifacePkg.replace('.', '/'))
            fs.createDirectories(ifaceDir)
            val className = repositoryBaseName + e.classSuffix

            // Interface (in data module)
            run {
                val ifaceTemplate = templates.getTemplateText("templates/dataSourceGenerator/DataSource.kt")
                val ifaceContent = ifaceTemplate
                    .replace("\${PACKAGE}", ifacePkg)
                    .replace("\${CLASS_NAME}", className)
                val ifaceOut = ifaceDir.resolve("$className.kt")
                val oldIface = fs.readFile(ifaceOut)
                fs.writeFile(ifaceOut, ifaceContent)
                val status = if (oldIface == null) "created" else if (oldIface != ifaceContent) "updated" else "exists"
                results += "- DataSource Interface: ${ifaceOut} (${status})"
            }

            // Implementation (in sibling datasource module)
            run {
                val moduleName = when (e.subDir) {
                    "dataSource" -> "dataSource"
                    "remoteDataSource" -> "remoteDataSource"
                    "localDataSource" -> "localDataSource"
                    else -> e.subDir
                }
                val moduleDir = dataDir.parent?.resolve(moduleName)
                    ?: error("Could not locate sibling $moduleName module for ${dataDir}")
                val modulePkg = modulePkgRepo.findModulePackage(moduleDir)
                val implPkg = modulePkg
                val implDir = moduleDir.resolve("src/main/kotlin").resolve(implPkg.replace('.', '/'))
                fs.createDirectories(implDir)

                val implTemplate = templates.getTemplateText("templates/dataSourceGenerator/DataSourceImpl.kt")
                val implContent = implTemplate
                    .replace("\${PACKAGE}", implPkg)
                    .replace("\${CLASS_NAME}", className)
                val implOut = implDir.resolve("${className}Impl.kt")
                val oldImpl = fs.readFile(implOut)
                fs.writeFile(implOut, implContent)
                val status = if (oldImpl == null) "created" else if (oldImpl != implContent) "updated" else "exists"
                results += "- DataSource Impl: ${implOut} (${status})"
            }
        }

        if (diDir != null && diPackage != null) {
            val desired: List<DataSourceBinding> = entries.map { e ->
                val className = repositoryBaseName + e.classSuffix
                val ifacePkg = "$dataBasePkg.${e.subDir}"
                val moduleName = when (e.subDir) {
                    "dataSource" -> "dataSource"
                    "remoteDataSource" -> "remoteDataSource"
                    "localDataSource" -> "localDataSource"
                    else -> e.subDir
                }
                val moduleDir = dataDir.parent?.resolve(moduleName)
                    ?: error("Could not locate sibling $moduleName module for ${dataDir}")
                val implPkg = modulePkgRepo.findModulePackage(moduleDir)

                val ifaceImport = "import $ifacePkg.$className"
                val implImport = "import $implPkg.${className}Impl"
                if (!options.useKoin) {
                    val signature = "abstract fun bind${className}(impl: ${className}Impl): ${className}"
                    val block = buildString {
                        appendLine("    @Binds")
                        append("    ").append(signature)
                    }
                    DataSourceBinding(ifaceImport, implImport, signature, block)
                } else {
                    val signature = "single<$className> { ${className}Impl(get()) }"
                    val block = "    $signature"
                    DataSourceBinding(ifaceImport, implImport, signature, block)
                }
            }

            if (!(options.useKoin && options.koinAnnotations)) {
                val outcome = diRepo.mergeDataSourceModule(diDir, diPackage, desired, options.useKoin)
                results += "- DI: ${outcome.outPath} (${outcome.status})"
            }
        }

        return results
    }
}
