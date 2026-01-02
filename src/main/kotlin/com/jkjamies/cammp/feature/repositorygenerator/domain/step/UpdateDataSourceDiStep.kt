package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceBinding
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class UpdateDataSourceDiStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val diRepo: DiModuleRepository
) : RepositoryStep {

    override suspend fun execute(params: RepositoryParams): StepResult {
        if (!params.includeDatasource) return StepResult.Success(null)
        if (params.diStrategy is DiStrategy.Koin && params.diStrategy.useAnnotations) return StepResult.Success(null)

        val diDir = params.dataDir.parent?.resolve("di") ?: return StepResult.Success(null)
        if (!diDir.toFile().exists()) return StepResult.Success(null)

        val diPackage = modulePkgRepo.findModulePackage(diDir)
        val dataPkg = modulePkgRepo.findModulePackage(params.dataDir)
        val dataBasePkg = if (dataPkg.contains(".data")) dataPkg.substringBefore(".data") + ".data" else dataPkg

        val entries = mutableListOf<Pair<String, String>>() // moduleName, suffix
        if (params.datasourceCombined) {
            entries += "dataSource" to "DataSource"
        } else {
            if (params.datasourceRemote) entries += "remoteDataSource" to "RemoteDataSource"
            if (params.datasourceLocal) entries += "localDataSource" to "LocalDataSource"
        }

        val useKoin = params.diStrategy is DiStrategy.Koin
        val bindings = mutableListOf<DataSourceBinding>()

        for ((moduleName, suffix) in entries) {
            val moduleDir = params.dataDir.parent?.resolve(moduleName) ?: continue
            if (!moduleDir.toFile().exists()) continue

            val modulePkg = modulePkgRepo.findModulePackage(moduleDir)
            val className = params.className + suffix
            val ifacePkg = "$dataBasePkg.$moduleName"
            
            val ifaceImport = "import $ifacePkg.$className"
            val implImport = "import $modulePkg.${className}Impl"

            if (!useKoin) {
                val signature = "abstract fun bind${className}(impl: ${className}Impl): ${className}"
                val block = buildString {
                    appendLine("    @Binds")
                    append("    ").append(signature)
                }
                bindings.add(DataSourceBinding(ifaceImport, implImport, signature, block))
            } else {
                val signature = "single<$className> { ${className}Impl(get()) }"
                val block = "    $signature"
                bindings.add(DataSourceBinding(ifaceImport, implImport, signature, block))
            }
        }

        if (bindings.isEmpty()) return StepResult.Success(null)

        return try {
            val outcome = diRepo.mergeDataSourceModule(
                diDir = diDir,
                diPackage = diPackage,
                desiredBindings = bindings,
                useKoin = useKoin
            )
            StepResult.Success("- DI: ${outcome.outPath} (${outcome.status})")
        } catch (e: Exception) {
            StepResult.Failure(e)
        }
    }
}