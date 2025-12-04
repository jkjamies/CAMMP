package com.github.jkjamies.cammp.feature.usecasegenerator.domain.usecase

import com.github.jkjamies.cammp.feature.usecasegenerator.data.FileSystemRepositoryImpl
import com.github.jkjamies.cammp.feature.usecasegenerator.data.ModulePackageRepositoryImpl
import com.github.jkjamies.cammp.feature.usecasegenerator.data.TemplateRepositoryImpl
import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.FileSystemRepository
import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.TemplateRepository
import com.github.jkjamies.cammp.feature.usecasegenerator.data.UseCaseDiModuleRepositoryImpl
import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.github.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardOpenOption

/**
 * Generates a use case file from a template, and optionally merges it into a DI module.
 *
 * @param fs The [FileSystemRepository] to use for file operations.
 * @param templateRepo The [TemplateRepository] to use for loading templates.
 * @param modulePkgRepo The [ModulePackageRepository] to use for finding module packages.
 * @param diRepo The [UseCaseDiModuleRepository] to use for merging DI modules.
 */
class UseCaseGenerator(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val templateRepo: TemplateRepository = TemplateRepositoryImpl(),
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl(),
    private val diRepo: UseCaseDiModuleRepository = UseCaseDiModuleRepositoryImpl(),
) {
    /**
     * @param params The [UseCaseParams] for generating the use case.
     * @return A [Result] containing the path to the generated file, or an exception.
     */
    suspend operator fun invoke(params: UseCaseParams): Result<Path> {
        return runCatching {
            val base = inferBasePackage(params.domainDir)
            val fullPackage = if (base.endsWith(".usecase")) base else "$base.usecase"
            val targetDir = params.domainDir.resolve("src/main/kotlin").resolve(fullPackage.replace('.', '/'))
            val content = renderFromTemplate(fullPackage, params)
            Files.createDirectories(targetDir)
            val out = targetDir.resolve("${params.className}.kt")
            Files.writeString(out, content, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)

            if (!(params.useKoin && params.koinAnnotations)) {
                // Skip DI module merge only for Koin Annotations (bindings via annotations)
                val diDir = params.domainDir.parent?.resolve("di")
                if (diDir != null && Files.exists(diDir)) {
                    val diExisting = modulePkgRepo.findModulePackage(diDir)
                    val diPackage = diExisting?.let { truncateAt(it, ".di") } ?: diExisting ?: ""
                    if (diPackage.isNotBlank()) {
                        val useCaseFqn = "$fullPackage.${params.className}"
                        diRepo.mergeUseCaseModule(
                            diDir = diDir,
                            diPackage = diPackage,
                            useCaseSimpleName = params.className,
                            useCaseFqn = useCaseFqn,
                            repositoryFqns = params.repositories.map { repo ->
                                val existingPkg = modulePkgRepo.findModulePackage(params.domainDir)
                                    ?: base
                                val marker = ".domain"
                                val idx = existingPkg.lastIndexOf(marker)
                                val baseDomain = if (idx >= 0) existingPkg.substring(0, idx + marker.length) else existingPkg
                                "$baseDomain.repository.$repo"
                            },
                            useKoin = params.useKoin,
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

    private fun renderFromTemplate(packageName: String, p: UseCaseParams): String {
        val templatePath = when {
            p.useKoin && p.koinAnnotations -> "templates/usecaseGenerator/koinAnnotations/UseCase.kt"
            p.useKoin -> "templates/usecaseGenerator/koin/UseCase.kt"
            else -> "templates/usecaseGenerator/hilt/UseCase.kt"
        }
        val template = templateRepo.getTemplateText(templatePath)
        val existingPkg = modulePkgRepo.findModulePackage(p.domainDir)
            ?: error("Could not determine existing package for selected domain module")
        val marker = ".domain"
        val idx = existingPkg.lastIndexOf(marker)
        val baseDomain = if (idx >= 0) existingPkg.substring(0, idx + marker.length) else existingPkg
        val imports = if (p.repositories.isNotEmpty()) {
            p.repositories.joinToString(separator = "\n") { repo -> "import $baseDomain.repository.$repo" } + "\n"
        } else ""
        val constructorParams = if (p.repositories.isNotEmpty()) {
            p.repositories.joinToString(separator = ",\n    ") { repo -> "private val ${repo.replaceFirstChar { it.lowercase() }}: $repo" }
        } else ""
        return template
            .replace("\${PACKAGE}", packageName)
            .replace("\${IMPORTS}", imports)
            .replace("\${USECASE_NAME}", p.className)
            .replace("\${CONSTRUCTOR_PARAMS}", constructorParams)
    }

    private fun truncateAt(pkg: String, marker: String): String {
        val idx = pkg.indexOf(marker)
        return if (idx >= 0) pkg.substring(0, idx + marker.length) else pkg
    }
}
