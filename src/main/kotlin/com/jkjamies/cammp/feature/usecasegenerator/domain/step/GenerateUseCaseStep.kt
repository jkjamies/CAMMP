package com.jkjamies.cammp.feature.usecasegenerator.domain.step

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class GenerateUseCaseStep @Inject constructor(
    private val repository: UseCaseGenerationRepository,
    private val modulePackageRepository: ModulePackageRepository
) : UseCaseStep {

    override suspend fun execute(params: UseCaseParams): StepResult {
        return try {
            val basePackage = modulePackageRepository.findModulePackage(params.domainDir)
                ?: throw IllegalStateException("Could not determine package for ${params.domainDir}")
            
            val targetPackage = if (basePackage.endsWith(".usecase")) {
                basePackage
            } else {
                "$basePackage.usecase"
            }

            // Calculate base domain package(e.g. com.example.domain)
            val marker = ".domain"
            val idx = basePackage.lastIndexOf(marker)
            val baseDomainPackage = if (idx >= 0) basePackage.substring(0, idx + marker.length) else basePackage

            val path = repository.generateUseCase(params, targetPackage, baseDomainPackage)
            StepResult.Success(path)
        } catch (e: Throwable) {
            StepResult.Failure(e)
        }
    }
}