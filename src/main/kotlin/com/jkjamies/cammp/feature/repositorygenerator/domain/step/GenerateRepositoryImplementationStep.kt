package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class GenerateRepositoryImplementationStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val generationRepo: RepositoryGenerationRepository
) : RepositoryStep {

    override suspend fun execute(params: RepositoryParams): StepResult {
        return try {
            val dataExisting = modulePkgRepo.findModulePackage(params.dataDir)
            val dataBase = truncateAt(dataExisting, ".data")

            val domainDir = params.dataDir.parent?.resolve("domain")
                ?: error("Could not locate sibling domain module for ${params.dataDir}")
            val domainExisting = modulePkgRepo.findModulePackage(domainDir)
            val domainBase = truncateAt(domainExisting, ".domain")

            val domainFull = "$domainBase.repository"
            val dataFull = "$dataBase.repository"

            val dataOut = generationRepo.generateDataLayer(params, dataFull, domainFull)
            StepResult.Success("- Data Implementation: $dataOut (generated)")
        } catch (e: Exception) {
            StepResult.Failure(e)
        }
    }

    private fun truncateAt(pkg: String?, marker: String): String {
        if (pkg == null) return ""
        val idx = pkg.indexOf(marker)
        return if (idx >= 0) pkg.substring(0, idx + marker.length) else pkg
    }
}
