package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@ContributesIntoSet(AppScope::class)
@Inject
class GenerateNavigationHostStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val navigationRepo: NavigationRepository
) : PresentationStep {

    override suspend fun execute(params: PresentationParams): StepResult {
        if (!params.includeNavigation) {
            return StepResult.Success(null)
        }

        return try {
            val pkg = inferPresentationPackage(params.moduleDir)
            val kotlinDir = params.moduleDir.resolve("src/main/kotlin").also { if (!it.exists()) it.createDirectories() }
            val pkgDir = kotlinDir.resolve(pkg.replace('.', '/')).also { if (!it.exists()) it.createDirectories() }
            
            val navDir = pkgDir.resolve("navigation").also { if (!it.exists()) it.createDirectories() }
            val navPkg = "$pkg.navigation"
            val navHostName = "${deriveModuleNameForFlowHolder(pkg)}NavigationHost"

            val result = navigationRepo.generateNavigationHost(
                targetDir = navDir,
                packageName = navPkg,
                navHostName = navHostName
            )
            StepResult.Success("- navigation/${result.fileName}: ${result.path} (${result.status})")
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
