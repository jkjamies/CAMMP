package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DatasourceStrategy
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
        if (params.datasourceStrategy is DatasourceStrategy.None) return StepResult.Success(null)

        val results = mutableListOf<String>()
        val dataPkg = modulePkgRepo.findModulePackage(params.dataDir)
        val dataBasePkg = if (dataPkg.contains(".data")) dataPkg.substringBefore(".data") + ".data" else dataPkg

        val entries = when (params.datasourceStrategy) {
            DatasourceStrategy.None -> emptyList()
            DatasourceStrategy.Combined -> listOf("dataSource" to "DataSource")
            DatasourceStrategy.RemoteOnly -> listOf("remoteDataSource" to "RemoteDataSource")
            DatasourceStrategy.LocalOnly -> listOf("localDataSource" to "LocalDataSource")
            DatasourceStrategy.RemoteAndLocal -> listOf(
                "remoteDataSource" to "RemoteDataSource",
                "localDataSource" to "LocalDataSource",
            )
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