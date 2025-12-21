package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.TemplateRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.UiStateRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.IntentRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.GenerationStatus
import com.jkjamies.cammp.feature.presentationgenerator.domain.model.FileGenerationResult
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

class PresentationRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val templateRepo: TemplateRepository = TemplateRepositoryImpl(),
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl(),
    private val diRepo: PresentationDiModuleRepository = PresentationDiModuleRepositoryImpl(),
    private val uiStateRepo: UiStateRepository = UiStateRepositoryImpl(),
    private val screenStateHolderRepo: ScreenStateHolderRepository = ScreenStateHolderRepositoryImpl(),
    private val flowStateHolderRepo: FlowStateHolderRepository = FlowStateHolderRepositoryImpl(),
    private val intentRepo: IntentRepository = IntentRepositoryImpl(),
    private val navigationRepo: NavigationRepository = NavigationRepositoryImpl(),
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
        val imports = if (useCaseFqns.isNotEmpty()) useCaseFqns.joinToString(separator = "\n") { fqn -> "import $fqn" } + "\n" else ""
        val constructorParams = if (useCaseFqns.isNotEmpty()) {
            useCaseFqns.joinToString(separator = ",\n    ") { fqn ->
                val simple = fqn.substringAfterLast('.')
                "private val ${simple.replaceFirstChar { it.lowercase() }}: $simple"
            }
        } else ""

        if (p.useFlowStateHolder) {
            val moduleName = deriveModuleNameForFlowHolder(pkg)
            val result = flowStateHolderRepo.generateFlowStateHolder(
                targetDir = pkgDir,
                packageName = pkg,
                flowName = "${moduleName}FlowStateHolder"
            )
            processResult(result, created, skipped, outputs, resultsLines)
        }

        val files = buildList {
            val screenTemplatePath = if (p.diKoin) {
                "templates/presentationGenerator/koin/Screen.kt"
            } else {
                "templates/presentationGenerator/hilt/Screen.kt"
            }
            add(
                RenderSpec(
                    fileName = "$sanitizedName.kt",
                    templatePath = screenTemplatePath,
                    tokens = mapOf(
                        "PACKAGE" to screenPackage,
                        "SCREEN_NAME" to sanitizedName,
                    )
                )
            )
            val viewModelTemplatePath = when {
                p.diKoin && p.diKoinAnnotations -> "templates/presentationGenerator/koinAnnotations/ViewModel.kt"
                p.diKoin -> "templates/presentationGenerator/koin/ViewModel.kt"
                else -> "templates/presentationGenerator/hilt/ViewModel.kt"
            }
            add(
                RenderSpec(
                    fileName = "${sanitizedName}ViewModel.kt",
                    templatePath = viewModelTemplatePath,
                    tokens = mapOf(
                        "PACKAGE" to screenPackage,
                        "SCREEN_NAME" to sanitizedName,
                        "IMPORTS" to imports,
                        "CONSTRUCTOR_PARAMS" to constructorParams,
                        "VIEW_MODEL_INTENT_HANDLER" to if (p.patternMVI) {
                            val mvi = templateRepo.getTemplateText("templates/presentationGenerator/helpers/MviViewModelIntentHandler.kt")
                            replaceTokens(mvi, mapOf("SCREEN_NAME" to sanitizedName))
                        } else ""
                    )
                )
            )
        }

        files.forEach { spec ->
            val target = targetDir.resolve(spec.fileName)
            val existed = target.exists()
            val raw = templateRepo.getTemplateText(spec.templatePath)
            val content = replaceTokens(raw, spec.tokens)
            fs.writeText(target, content, overwriteIfExists = false)
            val status = if (existed) {
                skipped.add(spec.fileName)
                "exists"
            } else {
                created.add(spec.fileName)
                "created"
            }
            outputs.add(target)
            resultsLines += "- ${spec.fileName}: ${target} (${status})"
        }

        // Generate UiState using KotlinPoet
        run {
            val result = uiStateRepo.generateUiState(
                targetDir = targetDir,
                packageName = screenPackage,
                screenName = sanitizedName
            )
            processResult(result, created, skipped, outputs, resultsLines)
        }

        // Generate Intent using KotlinPoet if needed
        if (p.patternMVI) {
            val result = intentRepo.generateIntent(
                targetDir = targetDir,
                packageName = screenPackage,
                screenName = sanitizedName
            )
            processResult(result, created, skipped, outputs, resultsLines)
        }

        // Generate ScreenStateHolder using KotlinPoet if needed
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
                processResult(result, created, skipped, outputs, resultsLines, "navigation/destinations/${result.fileName}")
            }
        }

        if (p.diKoin) {
            val diDir = p.moduleDir.parent?.resolve("di")
            if (diDir != null && diDir.exists()) {
                val diExisting = modulePkgRepo.findModulePackage(diDir)
                val diPackage = diExisting?.let { truncateAt(it, ".di") } ?: diExisting ?: ""
                if (diPackage.isNotBlank()) {
                    if (!p.diKoinAnnotations) {
                        val viewModelSimpleName = "${sanitizedName}ViewModel"
                        val viewModelFqn = "$screenPackage.${sanitizedName}ViewModel"
                        val mergeOutcome = diRepo.mergeViewModelModule(
                            diDir = diDir,
                            diPackage = diPackage,
                            viewModelSimpleName = viewModelSimpleName,
                            viewModelFqn = viewModelFqn,
                            dependencyCount = useCaseFqns.size,
                            useKoin = true,
                        )
                        resultsLines += "- DI ViewModel: ${mergeOutcome.outPath} (${mergeOutcome.status})"
                    }
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

    private fun replaceTokens(text: String, tokens: Map<String, String>): String {
        var out = text
        tokens.forEach { (k, v) -> out = out.replace("\${${k}}", v) }
        tokens.forEach { (k, v) -> out = out.replace("/*${k}*/", v) }
        return out
    }

    private fun truncateAt(pkg: String, marker: String): String {
        val idx = pkg.indexOf(marker)
        return if (idx >= 0) pkg.substring(0, idx) else pkg
    }

    private data class RenderSpec(
        val fileName: String,
        val templatePath: String,
        val tokens: Map<String, String>,
    )
}
