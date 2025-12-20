package com.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.jkjamies.cammp.feature.usecasegenerator.data.ModulePackageRepositoryImpl
import com.jkjamies.cammp.feature.usecasegenerator.data.UseCaseDiModuleRepositoryImpl
import com.jkjamies.cammp.feature.usecasegenerator.data.UseCaseGenerationRepositoryImpl
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import java.nio.file.Path
import kotlin.io.path.exists

class UseCaseGenerator(
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl(),
    private val diRepo: UseCaseDiModuleRepository = UseCaseDiModuleRepositoryImpl(),
    private val generationRepo: UseCaseGenerationRepository = UseCaseGenerationRepositoryImpl()
) {
    suspend operator fun invoke(params: UseCaseParams): Result<Path> {
        return runCatching {
            val className = if (params.className.endsWith("UseCase")) params.className else "${params.className}UseCase"
            val updatedParams = params.copy(className = className)

            val base = inferBasePackage(updatedParams.domainDir)
            val fullPackage = if (base.endsWith(".usecase")) base else "$base.usecase"

            val out = generationRepo.generateUseCase(updatedParams, fullPackage)

            if (!(updatedParams.useKoin && updatedParams.koinAnnotations)) {
                val diDir = updatedParams.domainDir.parent?.resolve("di")
                if (diDir != null && diDir.exists()) {
                    val diExisting = modulePkgRepo.findModulePackage(diDir)
                    val diPackage = diExisting?.removeSuffix(".usecase") ?: ""
                    if (diPackage.isNotBlank()) {
                        val useCaseFqn = "$fullPackage.${updatedParams.className}"
                        diRepo.mergeUseCaseModule(
                            diDir = diDir,
                            diPackage = diPackage,
                            useCaseSimpleName = updatedParams.className,
                            useCaseFqn = useCaseFqn,
                            repositoryFqns = updatedParams.repositories.map { repo ->
                                val existingPkg = modulePkgRepo.findModulePackage(updatedParams.domainDir)
                                    ?: base
                                val marker = ".domain"
                                val idx = existingPkg.lastIndexOf(marker)
                                val baseDomain = if (idx >= 0) existingPkg.substring(0, idx + marker.length) else existingPkg
                                "$baseDomain.repository.$repo"
                            },
                            useKoin = updatedParams.useKoin,
                        )
                    }
                }
            }
            out
        }
    }

    private fun inferBasePackage(moduleDir: Path): String {
        val existing = modulePkgRepo.findModulePackage(moduleDir)
        require(!existing.isNullOrBlank()) { "Could not determine existing package for selected domain module" }
        return existing
    }
}
