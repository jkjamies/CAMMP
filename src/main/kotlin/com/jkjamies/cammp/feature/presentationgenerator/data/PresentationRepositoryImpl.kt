package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.UiStateRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.IntentRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ViewModelRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

/**
 * Orchestrates the generation of presentation layer files.
 *
 * This repository coordinates with other more specialized repositories to generate the various
 * components of a screen, such as the ViewModel, Screen, UIState, etc.
 *
 * @param fs The [FileSystemRepository] for file operations.
 * @param modulePkgRepo The [ModulePackageRepository] for inferring package names.
 * @param diRepo The [PresentationDiModuleRepository] for DI module generation.
 * @param uiStateRepo The [UiStateRepository] for UI state generation.
 * @param screenStateHolderRepo The [ScreenStateHolderRepository] for screen state holder generation.
 * @param flowStateHolderRepo The [FlowStateHolderRepository] for flow state holder generation.
 * @param intentRepo The [IntentRepository] for MVI intent generation.
 * @param navigationRepo The [NavigationRepository] for navigation component generation.
 * @param screenRepo The [ScreenRepository] for screen composable generation.
 * @param viewModelRepo The [ViewModelRepository] for ViewModel generation.
 */
class PresentationRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl(),
    private val diRepo: PresentationDiModuleRepository = PresentationDiModuleRepositoryImpl(),
    private val uiStateRepo: UiStateRepository = UiStateRepositoryImpl(),
    private val screenStateHolderRepo: ScreenStateHolderRepository = ScreenStateHolderRepositoryImpl(),
    private val flowStateHolderRepo: FlowStateHolderRepository = FlowStateHolderRepositoryImpl(),
    private val intentRepo: IntentRepository = IntentRepositoryImpl(),
    private val navigationRepo: NavigationRepository = NavigationRepositoryImpl(),
    private val screenRepo: ScreenRepository = ScreenRepositoryImpl(),
    private val viewModelRepo: ViewModelRepository = ViewModelRepositoryImpl(),
) : PresentationRepository {

    override fun generate(params: PresentationParams): PresentationResult {
        val p = params

        val created = mutableListOf<String>()
        val skipped = mutableListOf<String>()
        val outputs = mutableListOf<Path>()
        val resultsLines = mutableListOf<String>()

        val sanitizedName = sanitizeScreenName(p.screenName)
        val pkg = inferPresentationPackage(p.moduleDir)
        val kotlinDir = p.moduleDir.resolve("src/main/kotlin").also { if (!it.exists()) it.createDirectories() }
        val pkgDir = kotlinDir.resolve(pkg.replace('.', '/')).also { if (!it.exists()) it.createDirectories() }
        val folder = sanitizedName.replaceFirstChar { it.lowercase() }
        val targetDir = pkgDir.resolve(folder).also { if (!it.exists()) it.createDirectories() }
        val screenPackage = "$pkg.$folder"

        val useCaseFqns = p.selectedUseCases.distinct().sorted()

        if (p.useFlowStateHolder) {
            val moduleName = deriveModuleNameForFlowHolder(pkg)
            val result = flowStateHolderRepo.generateFlowStateHolder(
                targetDir = pkgDir,
                packageName = pkg,
                flowName = "${moduleName}FlowStateHolder"
            )
            processResult(result, created, skipped, outputs, resultsLines)
        }

        val screenResult = screenRepo.generateScreen(
            targetDir = targetDir,
            packageName = screenPackage,
            screenName = sanitizedName,
            diHilt = p.diHilt,
            diKoin = p.diKoin
        )
        processResult(screenResult, created, skipped, outputs, resultsLines)

        val viewModelResult = viewModelRepo.generateViewModel(
            targetDir = targetDir,
            packageName = screenPackage,
            screenName = sanitizedName,
            diHilt = p.diHilt,
            diKoin = p.diKoin,
            diKoinAnnotations = p.diKoinAnnotations,
            patternMVI = p.patternMVI,
            useCaseFqns = useCaseFqns
        )
        processResult(viewModelResult, created, skipped, outputs, resultsLines)

        run {
            val result = uiStateRepo.generateUiState(
                targetDir = targetDir,
                packageName = screenPackage,
                screenName = sanitizedName
            )
            processResult(result, created, skipped, outputs, resultsLines)
        }

        if (p.patternMVI) {
            val result = intentRepo.generateIntent(
                targetDir = targetDir,
                packageName = screenPackage,
                screenName = sanitizedName
            )
            processResult(result, created, skipped, outputs, resultsLines)
        }

        if (p.useScreenStateHolder) {
            val result = screenStateHolderRepo.generateScreenStateHolder(
                targetDir = targetDir,
                packageName = screenPackage,
                screenName = sanitizedName
            )
            processResult(result, created, skipped, outputs, resultsLines)
        }

        if (p.includeNavigation) {
            val navDir = pkgDir.resolve("navigation").also { if (!it.exists()) it.createDirectories() }
            val navPkg = "$pkg.navigation"
            val navHostName = "${deriveModuleNameForFlowHolder(pkg)}NavigationHost"

            run {
                val result = navigationRepo.generateNavigationHost(
                    targetDir = navDir,
                    packageName = navPkg,
                    navHostName = navHostName
                )
                processResult(result, created, skipped, outputs, resultsLines, "navigation/${result.fileName}")
            }

            run {
                val destDir = navDir.resolve("destination").also { if (!it.exists()) it.createDirectories() }
                val result = navigationRepo.generateDestination(
                    targetDir = destDir,
                    packageName = pkg,
                    screenName = sanitizedName,
                    screenFolder = folder
                )
                processResult(
                    result,
                    created,
                    skipped,
                    outputs,
                    resultsLines,
                    "navigation/destinations/${result.fileName}"
                )
            }
        }

        if (p.diKoin && !p.diKoinAnnotations) {
            val diDir = p.moduleDir.parent?.resolve("di")
            if (diDir != null && diDir.exists()) {
                val diPackage = if (pkg.endsWith(".presentation")) {
                    pkg.replace(".presentation", ".di")
                } else {
                    val found = modulePkgRepo.findModulePackage(diDir)
                    found ?: "$pkg.di"
                }

                if (diPackage.isNotBlank()) {
                    val viewModelSimpleName = "${sanitizedName}ViewModel"
                    val viewModelFqn = "$screenPackage.${sanitizedName}ViewModel"
                    val mergeOutcome = diRepo.mergeViewModelModule(
                        diDir = diDir,
                        diPackage = diPackage,
                        viewModelSimpleName = viewModelSimpleName,
                        viewModelFqn = viewModelFqn,
                        dependencyCount = useCaseFqns.size,
                    )
                    resultsLines += "- DI ViewModel: ${mergeOutcome.outPath} (${mergeOutcome.status})"
                }
            }
        }

        val title = "Presentation generation completed:"
        return PresentationResult(
            created = created,
            skipped = skipped,
            message = (sequenceOf(title) + resultsLines.asSequence()).joinToString("\n"),
            outputPaths = outputs
        )
    }

    private fun processResult(
        result: FileGenerationResult,
        created: MutableList<String>,
        skipped: MutableList<String>,
        outputs: MutableList<Path>,
        resultsLines: MutableList<String>,
        relativePath: String? = null
    ) {
        val name = relativePath ?: result.fileName
        if (result.status == GenerationStatus.CREATED) {
            created.add(name)
            resultsLines += "- $name: ${result.path} (created)"
        } else {
            skipped.add(name)
            resultsLines += "- $name: ${result.path} (exists)"
        }
        outputs.add(result.path)
    }

    private fun sanitizeScreenName(raw: String): String {
        val base = raw.trim().replace(Regex("[^A-Za-z0-9_]"), "")
        if (base.isEmpty()) return "Screen"
        return base.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }

    private fun inferPresentationPackage(moduleDir: Path): String {
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
