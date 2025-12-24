package com.jkjamies.cammp.feature.repositorygenerator.data

import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceOptions
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.TypeSpec
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

class DatasourceScaffoldRepositoryImpl(
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
            ifaceDir.createDirectories()
            val className = repositoryBaseName + e.classSuffix

            // Interface (in data module)
            run {
                val fileSpec = FileSpec.builder(ifacePkg, className)
                    .addType(TypeSpec.interfaceBuilder(className).build())
                    .build()
                val ifaceOut = ifaceDir.resolve("$className.kt")
                ifaceOut.writeText(fileSpec.toString().replace("`data`", "data"))
                results += "- DataSource Interface: $ifaceOut (generated)"
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
                    ?: error("Could not locate sibling $moduleName module for $dataDir")
                val modulePkg = modulePkgRepo.findModulePackage(moduleDir)
                val implDir = moduleDir.resolve("src/main/kotlin").resolve(modulePkg.replace('.', '/'))
                implDir.createDirectories()

                val ifaceClassName = ClassName(ifacePkg, className)
                val implClassName = "${className}Impl"
                val constructorBuilder = FunSpec.constructorBuilder()
                if (!options.useKoin) {
                    constructorBuilder.addAnnotation(ClassName("javax.inject", "Inject"))
                }
                val fileSpec = FileSpec.builder(modulePkg, implClassName)
                    .addType(
                        TypeSpec.classBuilder(implClassName)
                            .addSuperinterface(ifaceClassName)
                            .primaryConstructor(constructorBuilder.build())
                            .build()
                    )
                    .build()
                val implOut = implDir.resolve("$implClassName.kt")
                implOut.writeText(fileSpec.toString().replace("`data`", "data"))
                results += "- DataSource Impl: $implOut (generated)"
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
                    ?: error("Could not locate sibling $moduleName module for $dataDir")
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
