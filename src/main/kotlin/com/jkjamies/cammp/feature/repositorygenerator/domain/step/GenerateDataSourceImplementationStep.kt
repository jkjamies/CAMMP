package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class GenerateDataSourceImplementationStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val scaffoldRepo: DatasourceScaffoldRepository
) : RepositoryStep {

    override suspend fun execute(params: RepositoryParams): StepResult {
        if (!params.includeDatasource) return StepResult.Success(null)

        val results = mutableListOf<String>()
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

        for ((moduleName, suffix) in entries) {
            val moduleDir = params.dataDir.parent?.resolve(moduleName)
                ?: return StepResult.Failure(IllegalStateException("Sibling module $moduleName not found"))
            
            if (!moduleDir.toFile().exists()) continue

            val modulePkg = modulePkgRepo.findModulePackage(moduleDir)
            val implDir = moduleDir.resolve("src/main/kotlin").resolve(modulePkg.replace('.', '/'))
            
            val ifaceName = params.className + suffix
            val ifacePkg = "$dataBasePkg.$moduleName" // Assuming subpackage matches module name
            val implName = "${ifaceName}Impl"

            try {
                val out = scaffoldRepo.generateImplementation(
                    directory = implDir,
                    packageName = modulePkg,
                    className = implName,
                    interfacePackage = ifacePkg,
                    interfaceName = ifaceName,
                    useKoin = useKoin
                )
                results += "- DataSource Impl: $out (generated)"
            } catch (e: Exception) {
                return StepResult.Failure(e)
            }
        }

        return StepResult.Success(results.joinToString("\n"))
    }
}