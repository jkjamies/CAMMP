package com.github.jkjamies.cammp.feature.presentationgenerator.data

import com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository
import com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository
import com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationRepository
import com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository.TemplateRepository
import com.github.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationParams
import com.github.jkjamies.cammp.feature.presentationgenerator.domain.model.PresentationResult
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists

class PresentationRepositoryImpl(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val templateRepo: TemplateRepository = TemplateRepositoryImpl(),
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl(),
    private val diRepo: PresentationDiModuleRepository = PresentationDiModuleRepositoryImpl(),
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

        val useCaseFqns = p.selectedUseCases.distinct().sorted()
        val imports = if (useCaseFqns.isNotEmpty()) useCaseFqns.joinToString(separator = "\n") { fqn -> "import $fqn" } + "\n" else ""
        val constructorParams = if (useCaseFqns.isNotEmpty()) {
            useCaseFqns.joinToString(separator = ",\n    ") { fqn ->
                val simple = fqn.substringAfterLast('.')
                "private val ${simple.replaceFirstChar { it.lowercase() }}: $simple"
            }
        } else ""

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
                        "PACKAGE" to pkg,
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
                        "PACKAGE" to pkg,
                        "SCREEN_NAME" to sanitizedName,
                        "IMPORTS" to imports,
                        "CONSTRUCTOR_PARAMS" to constructorParams,
                        "VIEW_MODEL_INTENT_HANDLER" to if (p.patternMVI) {
                            templateRepo.getTemplateText("templates/presentationGenerator/helpers/MviViewModelIntentHandler.kt")
                        } else ""
                    )
                )
            )
            add(
                RenderSpec(
                    fileName = "${sanitizedName}UiState.kt",
                    templatePath = "templates/presentationGenerator/UiState.kt",
                    tokens = mapOf(
                        "PACKAGE" to pkg,
                        "SCREEN_NAME" to sanitizedName,
                    )
                )
            )
            if (p.patternMVI) {
                add(
                    RenderSpec(
                        fileName = "${sanitizedName}Intent.kt",
                        templatePath = "templates/presentationGenerator/Intent.kt",
                        tokens = mapOf(
                            "PACKAGE" to pkg,
                            "SCREEN_NAME" to sanitizedName,
                        )
                    )
                )
            }
            if (p.useFlowStateHolder) {
                val moduleName = deriveModuleNameForFlowHolder(pkg)
                add(
                    RenderSpec(
                        fileName = "${sanitizedName}FlowStateHolder.kt",
                        templatePath = "templates/presentationGenerator/FlowStateHolder.kt",
                        tokens = mapOf(
                            "PACKAGE" to pkg,
                            "SCREEN_NAME" to sanitizedName,
                            "MODULE_NAME" to moduleName,
                        )
                    )
                )
            }
            if (p.useScreenStateHolder) {
                add(
                    RenderSpec(
                        fileName = "${sanitizedName}ScreenStateHolder.kt",
                        templatePath = "templates/presentationGenerator/ScreenStateHolder.kt",
                        tokens = mapOf(
                            "PACKAGE" to pkg,
                            "SCREEN_NAME" to sanitizedName,
                        )
                    )
                )
            }
        }

        files.forEach { spec ->
            val target = targetDir.resolve(spec.fileName)
            val existed = target.exists()
            val raw = templateRepo.getTemplateText(spec.templatePath)
            val content = replaceTokens(raw, spec.tokens)
            fs.writeText(target, content, overwriteIfExists = p.overwrite)
            val status = if (existed && !p.overwrite) {
                skipped.add(spec.fileName)
                "exists"
            } else {
                created.add(spec.fileName)
                "created"
            }
            outputs.add(target)
            resultsLines += "- ${spec.fileName}: ${target} (${status})"
        }

        if (p.includeNavigation) {
            val basePkg = pkg.substringBeforeLast('.').substringBeforeLast('.')
            val navDir = p.moduleDir.resolve("src/main/kotlin").resolve(basePkg.replace('.', '/')).resolve("navigation").also { if (!it.exists()) it.createDirectories() }

            run {
                val fileName = "NavigationHost.kt"
                val target = navDir.resolve(fileName)
                val existed = target.exists()
                val raw = templateRepo.getTemplateText("templates/presentationGenerator/navigation/NavigationHost.kt")
                val content = replaceTokens(raw, mapOf("PACKAGE" to "$basePkg.navigation"))
                fs.writeText(target, content, overwriteIfExists = p.overwrite)
                val status = if (existed && !p.overwrite) {
                    skipped.add("navigation/$fileName")
                    "exists"
                } else {
                    created.add("navigation/$fileName")
                    "created"
                }
                outputs.add(target)
                resultsLines += "- NavigationHost: ${target} (${status})"
            }

            run {
                val fileName = "${sanitizedName}Destination.kt"
                val target = navDir.resolve("destination").also { if (!it.exists()) it.createDirectories() }.resolve(fileName)
                val existed = target.exists()
                val raw = templateRepo.getTemplateText("templates/presentationGenerator/navigation/destination/Destination.kt")
                val content = replaceTokens(
                    raw,
                    mapOf(
                        "PACKAGE" to basePkg,
                        "SCREEN_NAME" to sanitizedName,
                        "ROUTE" to sanitizedName,
                    )
                )
                fs.writeText(target, content, overwriteIfExists = p.overwrite)
                val status = if (existed && !p.overwrite) {
                    skipped.add("navigation/destinations/$fileName")
                    "exists"
                } else {
                    created.add("navigation/destinations/$fileName")
                    "created"
                }
                outputs.add(target)
                resultsLines += "- Destination: ${target} (${status})"
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
                        val viewModelFqn = "$pkg.${sanitizedName}ViewModel"
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
        tokens.forEach { (k, v) -> out = out.replace("{{${k}}}", v) }
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
