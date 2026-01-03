package com.jkjamies.cammp.feature.presentationgenerator.domain.step

import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

@ContributesIntoSet(AppScope::class)
@Inject
class GenerateScreenStateHolderStep(
    private val modulePkgRepo: ModulePackageRepository,
    private val screenStateHolderRepo: ScreenStateHolderRepository
) : PresentationStep {

    override suspend fun execute(params: PresentationParams): StepResult {
        if (!params.useScreenStateHolder) {
            return StepResult.Success(null)
        }

        return try {
            val sanitizedName = sanitizeScreenName(params.screenName)
            val pkg = inferPresentationPackage(params.moduleDir)
            val kotlinDir = params.moduleDir.resolve("src/main/kotlin").also { if (!it.exists()) it.createDirectories() }
            val pkgDir = kotlinDir.resolve(pkg.replace('.', '/')).also { if (!it.exists()) it.createDirectories() }
            val folder = sanitizedName.replaceFirstChar { it.lowercase() }
            val targetDir = pkgDir.resolve(folder).also { if (!it.exists()) it.createDirectories() }
            val screenPackage = "$pkg.$folder"

            val result = screenStateHolderRepo.generateScreenStateHolder(
                targetDir = targetDir,
                packageName = screenPackage,
                params = params
            )
            StepResult.Success("- ${result.fileName}: ${result.path} (${result.status})")
        } catch (e: Exception) {
            StepResult.Failure(e)
        }
    }

    private fun sanitizeScreenName(raw: String): String {
        val base = raw.trim().replace(Regex("[^A-Za-z0-9_]"), "")
        if (base.isEmpty()) return "Screen"
        return base.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun inferPresentationPackage(moduleDir: java.nio.file.Path): String {
        val existing = modulePkgRepo.findModulePackage(moduleDir)
        return existing ?: "com.example.presentation"
    }
}
