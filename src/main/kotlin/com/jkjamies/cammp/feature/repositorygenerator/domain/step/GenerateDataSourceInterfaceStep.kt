package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class GenerateDataSourceInterfaceStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val scaffoldRepo: DatasourceScaffoldRepository
) : RepositoryStep {

    override suspend fun execute(params: RepositoryParams): StepResult {
        if (!params.includeDatasource) return StepResult.Success(null)

        val results = mutableListOf<String>()
        val dataPkg = modulePkgRepo.findModulePackage(params.dataDir)
        val dataBasePkg = if (dataPkg.contains(".data")) dataPkg.substringBefore(".data") + ".data" else dataPkg

        val entries = mutableListOf<Pair<String, String>>() // subDir, suffix
        if (params.datasourceCombined) {
            entries += "dataSource" to "DataSource"
        } else {
            if (params.datasourceRemote) entries += "remoteDataSource" to "RemoteDataSource"
            if (params.datasourceLocal) entries += "localDataSource" to "LocalDataSource"
        }

        for ((subDir, suffix) in entries) {
            val ifacePkg = "$dataBasePkg.$subDir"
            val ifaceDir = params.dataDir.resolve("src/main/kotlin").resolve(ifacePkg.replace('.', '/'))
            val className = params.className + suffix

            try {
                val out = scaffoldRepo.generateInterface(
                    directory = ifaceDir,
                    packageName = ifacePkg,
                    className = className
                )
                results += "- DataSource Interface: $out (generated)"
            } catch (e: Exception) {
                return StepResult.Failure(e)
            }
        }

        return StepResult.Success(results.joinToString("\n"))
    }
}