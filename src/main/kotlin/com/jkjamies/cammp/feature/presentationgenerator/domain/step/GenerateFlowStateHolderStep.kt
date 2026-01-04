package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@ContributesIntoSet(AppScope::class)
@Inject
class GenerateFlowStateHolderStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val flowStateHolderRepo: FlowStateHolderRepository
) : PresentationStep {

    override suspend fun execute(params: PresentationParams): StepResult {
        if (!params.useFlowStateHolder) {
            return StepResult.Success(null)
        }

        return try {
            val pkg = inferPresentationPackage(params.moduleDir)
            val kotlinDir = params.moduleDir.resolve("src/main/kotlin").also { if (!it.exists()) it.createDirectories() }
            val pkgDir = kotlinDir.resolve(pkg.replace('.', '/')).also { if (!it.exists()) it.createDirectories() }
            
            val moduleName = deriveModuleNameForFlowHolder(pkg)
            val result = flowStateHolderRepo.generateFlowStateHolder(
                targetDir = pkgDir,
                packageName = pkg,
                flowName = "${moduleName}FlowStateHolder",
                params = params
            )
            StepResult.Success("- ${result.fileName}: ${result.path} (${result.status})")
        } catch (e: Exception) {
            StepResult.Failure(e)
        }
    }

    private fun inferPresentationPackage(moduleDir: java.nio.file.Path): String {
        val existing = modulePkgRepo.findModulePackage(moduleDir)
        return existing ?: "com.example.presentation"
    }

    private fun deriveModuleNameForFlowHolder(pkg: String): String {
        val parts = pkg.split('.')
        val idx = parts.indexOf("presentation")
        if (idx > 0) {
            return parts.getOrNull(idx - 1)?.replaceFirstChar { it.uppercase() } ?: "App"
        }
        return parts.lastOrNull()?.replaceFirstChar { it.uppercase() } ?: "App"
    }
}
