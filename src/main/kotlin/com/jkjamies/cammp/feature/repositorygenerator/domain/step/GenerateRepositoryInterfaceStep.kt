package com.jkjamies.cammp.feature.repositorygenerator.domain.step

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class GenerateRepositoryInterfaceStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val generationRepo: RepositoryGenerationRepository
) : RepositoryStep {

    override suspend fun execute(params: RepositoryParams): StepResult {
        return try {
            val domainDir = params.dataDir.parent?.resolve("domain")
                ?: error("Could not locate sibling domain module for ${params.dataDir}")
            
            val domainExisting = modulePkgRepo.findModulePackage(domainDir)
            // Simple heuristic to ensure we are in the base package
            val domainBase = if (domainExisting.contains(".domain")) {
                 domainExisting.substringBefore(".domain") + ".domain"
            } else {
                 domainExisting
            }
            
            val domainFull = "$domainBase.repository"

            val out = generationRepo.generateDomainLayer(params, domainFull, domainDir)
            StepResult.Success("- Domain Interface: $out (generated)")
        } catch (e: Exception) {
            StepResult.Failure(e)
        }
    }
}