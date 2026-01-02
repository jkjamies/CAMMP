package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class UpdateDiStep @Inject constructor(
    private val diRepository: UseCaseDiModuleRepository,
    private val modulePackageRepository: ModulePackageRepository
) : UseCaseStep {

    override suspend fun execute(params: UseCaseParams): StepResult {
        return try {
            val diDir = params.domainDir.parent?.resolve("di")
                ?: return StepResult.Success(null)

            if (!diDir.toFile().exists()) {
                 return StepResult.Success(null)
            }

            val diPackage = modulePackageRepository.findModulePackage(diDir)
                ?: return StepResult.Success(null)

            val packageFound = modulePackageRepository.findModulePackage(params.domainDir) ?: ""
            // The package repo returns the usecase package (e.g. ...domain.usecase). Strip it to find the sibling.
            val domainBase = if (packageFound.endsWith(".usecase")) packageFound.removeSuffix(".usecase") else packageFound
            val useCasePackage = "$domainBase.usecase"
            val useCaseFqn = "$useCasePackage.${params.className}"

            val repoFqns = params.repositories.map { simpleName ->
                "$domainBase.repository.$simpleName"
            }

            val outcome = diRepository.mergeUseCaseModule(
                diDir = diDir,
                diPackage = diPackage,
                useCaseSimpleName = params.className,
                useCaseFqn = useCaseFqn,
                repositoryFqns = repoFqns,
                diStrategy = params.diStrategy
            )

            StepResult.Success(outcome.outPath)
        } catch (e: Throwable) {
            StepResult.Failure(e)
        }
    }
}