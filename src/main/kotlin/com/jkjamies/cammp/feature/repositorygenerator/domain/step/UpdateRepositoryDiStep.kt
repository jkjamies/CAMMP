package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
@Inject
class UpdateRepositoryDiStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val diRepo: DiModuleRepository
) : RepositoryStep {

    override suspend fun execute(params: RepositoryParams): StepResult {
        // Skip logic: If no DI strategy is chosen, this does nothing.
        if (params.diStrategy !is DiStrategy.Hilt && params.diStrategy !is DiStrategy.Metro && params.diStrategy !is DiStrategy.Koin) {
            return StepResult.Success(null)
        }

        return try {
            val diDir = params.dataDir.parent?.resolve("di")
                ?: return StepResult.Success("- DI: Skipped (no di module found)")

            val diPackage = modulePkgRepo.findModulePackage(diDir)

            val dataBase = modulePkgRepo.findModulePackage(params.dataDir)

            val domainDir = params.dataDir.parent?.resolve("domain")
                ?: error("Could not locate sibling domain module for ${params.dataDir}")
            val domainBase = modulePkgRepo.findModulePackage(domainDir)

            val domainFqn = "$domainBase.repository"
            val dataFqn = "$dataBase.repository"

            val outcome = diRepo.mergeRepositoryModule(
                diDir = diDir,
                diPackage = diPackage,
                className = params.className,
                domainFqn = domainFqn,
                dataFqn = dataFqn,
                useKoin = params.diStrategy is DiStrategy.Koin
            )

            StepResult.Success("- DI: ${outcome.outPath} (${outcome.status})")
        } catch (e: Exception) {
            StepResult.Failure(e)
        }
    }
}