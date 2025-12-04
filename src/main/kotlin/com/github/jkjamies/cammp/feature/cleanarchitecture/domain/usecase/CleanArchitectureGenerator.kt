package com.github.jkjamies.cammp.feature.cleanarchitecture.domain.usecase

import com.github.jkjamies.cammp.feature.cleanarchitecture.data.FileSystemRepositoryImpl
import com.github.jkjamies.cammp.feature.cleanarchitecture.data.GradleSettingsRepositoryImpl
import com.github.jkjamies.cammp.feature.cleanarchitecture.data.TemplateRepositoryImpl
import com.github.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.github.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.github.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.github.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsRepository
import com.github.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.isDirectory

/**
 * Generates a Clean Architecture module structure for a feature.
 *
 * @param fs The [FileSystemRepository] to use for file operations.
 * @param templateRepo The [TemplateRepository] to use for loading templates.
 * @param settingsRepo The [GradleSettingsRepository] to use for updating Gradle settings.
 */
class CleanArchitectureGenerator(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val templateRepo: TemplateRepository = TemplateRepositoryImpl(),
    private val settingsRepo: GradleSettingsRepository = GradleSettingsRepositoryImpl(),
) {

    /**
     * @param p The [CleanArchitectureParams] for generating the modules.
     * @return A [Result] containing the [CleanArchitectureResult], or an exception.
     */
    operator fun invoke(p: CleanArchitectureParams): Result<CleanArchitectureResult> = runCatching {
        require(p.projectBasePath.isDirectory()) { "Project base path does not exist or is not a directory: ${p.projectBasePath}" }

        val rootDir = p.projectBasePath.resolve(p.root).also { if (!it.exists()) it.createDirectories() }
        val featureDir = rootDir.resolve(p.feature).also { if (!it.exists()) it.createDirectories() }

        val modules = buildList {
            add("domain")
            add("data")
            add("di")
            if (p.includePresentation) add("presentation")
            if (p.includeDatasource) {
                if (p.datasourceCombined) add("dataSource") else {
                    if (p.datasourceRemote) add("remoteDataSource")
                    if (p.datasourceLocal) add("localDataSource")
                }
            }
        }

        val created = mutableListOf<String>()
        val skipped = mutableListOf<String>()

        for (m in modules) {
            val moduleDir = featureDir.resolve(m)
            if (moduleDir.exists()) {
                skipped += m
            } else {
                moduleDir.createDirectories()
                scaffoldModuleBuildFile(moduleDir, p, m)
                scaffoldModuleSourceSkeleton(moduleDir, p, m)
                created += m
            }
        }

        val settingsUpdated = settingsRepo.ensureIncludes(p.projectBasePath, p.root, p.feature, modules)
        settingsRepo.ensureIncludeBuild(p.projectBasePath, "build-logic")
        settingsRepo.ensureVersionCatalogPluginAliases(p.projectBasePath, p.orgCenter, modules)
        val buildLogicCreated = scaffoldBuildLogic(p.projectBasePath, p.orgCenter, modules, p)

        val msg = if (created.isEmpty()) {
            "No modules created (all existed). Settings and build-logic ensured."
        } else {
            "Created modules: ${created.joinToString()}. Settings and build-logic updated."
        }

        CleanArchitectureResult(
            created = created,
            skipped = skipped,
            settingsUpdated = settingsUpdated,
            buildLogicCreated = buildLogicCreated,
            message = msg,
        )
    }

    private fun sanitizeOrgCenter(input: String): String {
        val trimmed = input.trim()
        val unwrapped = if (trimmed.startsWith("\${" ) && trimmed.endsWith("}")) {
            trimmed.removePrefix("\${").removeSuffix("}")
        } else trimmed
        val withoutLeading = unwrapped.removePrefix("com.").removePrefix("org.")
        val cleaned = withoutLeading.replace(Regex("[^A-Za-z0-9_.]"), "").trim('.')
        return cleaned.ifBlank { "cammp" }
    }

    private fun replacePackageTokens(text: String, orgCenter: String): String {
        val org = sanitizeOrgCenter(orgCenter)
        val orgPath = org.replace('.', '/')
        var out = text.replace(Regex("\\$\\{\\s*PACKAGE\\s*}"), org)
        out = out.replace("com.PACKAGE.", "com.$org.")
        out = out.replace("com/PACKAGE/", "com/$orgPath/")
        out = out.replace("PACKAGE", org)
        return out
    }

    private fun scaffoldModuleBuildFile(moduleDir: Path, p: CleanArchitectureParams, moduleName: String) {
        val templatePath = when (moduleName) {
            "domain" -> "templates/cleanArchitecture/module/domain.gradle.kts"
            "data" -> "templates/cleanArchitecture/module/data.gradle.kts"
            "di" -> "templates/cleanArchitecture/module/di.gradle.kts"
            "presentation" -> "templates/cleanArchitecture/module/presentation.gradle.kts"
            "dataSource" -> "templates/cleanArchitecture/module/dataSource.gradle.kts"
            "remoteDataSource" -> "templates/cleanArchitecture/module/remoteDataSource.gradle.kts"
            "localDataSource" -> "templates/cleanArchitecture/module/localDataSource.gradle.kts"
            else -> "templates/cleanArchitecture/module/domain.gradle.kts"
        }
        val raw = templateRepo.getTemplateText(templatePath)
        val safeOrg = sanitizeOrgCenter(p.orgCenter)
        val namespace = "com.$safeOrg.${p.root}.${p.feature}.$moduleName"
        val content = raw
            .replace("\${'$'}{NAMESPACE}", namespace)
            .replace("NAMESPACE", namespace)
            .let { replacePackageTokens(it, safeOrg) }
        fs.writeText(moduleDir.resolve("build.gradle.kts"), content)
    }

    private fun scaffoldModuleSourceSkeleton(moduleDir: Path, p: CleanArchitectureParams, moduleName: String) {
        val safeOrg = sanitizeOrgCenter(p.orgCenter)
        val pkg = "com.$safeOrg.${p.root}.${p.feature}.$moduleName"
        val srcMainKotlin = moduleDir.resolve("src/main/kotlin").also { if (!it.exists()) it.createDirectories() }
        moduleDir.resolve("src/test/kotlin").also { if (!it.exists()) it.createDirectories() }
        val pkgDir = srcMainKotlin.resolve(pkg.replace('.', '/')).also { if (!it.exists()) it.createDirectories() }
        val placeholder = """
            package $pkg

            class Placeholder
        """.trimIndent()
        val placeholderFile = pkgDir.resolve("Placeholder.kt")
        if (!placeholderFile.exists()) fs.writeText(placeholderFile, placeholder)

        // Create standard subpackages
        when (moduleName) {
            "domain" -> {
                listOf("repository", "model", "usecase").forEach { sub ->
                    val d = pkgDir.resolve(sub); if (!d.exists()) d.createDirectories()
                }
            }
            "data" -> {
                val repoDir = pkgDir.resolve("repository"); if (!repoDir.exists()) repoDir.createDirectories()
                if (p.includeDatasource) {
                    if (p.datasourceCombined) {
                        val d = pkgDir.resolve("dataSource"); if (!d.exists()) d.createDirectories()
                    } else {
                        if (p.datasourceRemote) { val d = pkgDir.resolve("remoteDataSource"); if (!d.exists()) d.createDirectories() }
                        if (p.datasourceLocal) { val d = pkgDir.resolve("localDataSource"); if (!d.exists()) d.createDirectories() }
                    }
                }
            }
            "di" -> {
                // When Koin Annotations are selected, create a ComponentScan module here.
                if (p.diKoin && p.diKoinAnnotations) {
                    val template = templateRepo.getTemplateText("templates/cleanArchitecture/koinAnnotations/AnnotationsModule.kt")
                    val diPackage = pkg // di module's package
                    val scanBase = "com.${sanitizeOrgCenter(p.orgCenter)}.${p.root}.${p.feature}"
                    val featureName = p.feature.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    val content = template
                        .replace("\${DI_PACKAGE}", diPackage)
                        .replace("\${PACKAGE_NAME}", scanBase)
                        .replace("\${FEATURE_NAME}", featureName)
                    val out = pkgDir.resolve("${featureName}AnnotationsModule.kt")
                    if (!out.exists()) fs.writeText(out, content) else fs.writeText(out, content) // overwrite to keep in sync
                }
            }
        }
    }

    private fun scaffoldBuildLogic(projectBase: Path, orgCenter: String, enabledModules: List<String>, p: CleanArchitectureParams): Boolean {
        val buildLogicDir = projectBase.resolve("build-logic")
        var changed = false
        if (!buildLogicDir.exists()) {
            buildLogicDir.createDirectories()
            changed = true
        }
        // settings.gradle.kts
        val settingsPath = buildLogicDir.resolve("settings.gradle.kts")
        if (!settingsPath.exists()) {
            val settings = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/settings.gradle.kts")
            fs.writeText(settingsPath, settings)
            changed = true
        }
        // build.gradle.kts
        val safeOrg = sanitizeOrgCenter(orgCenter)
        val buildRaw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/build.gradle.kts")
        val buildText = replacePackageTokens(buildRaw, safeOrg)
        val buildPath = buildLogicDir.resolve("build.gradle.kts")
        if (!buildPath.exists()) {
            fs.writeText(buildPath, buildText)
            changed = true
        }

        // src/main/kotlin/com/<org>/convention and helpers
        val srcMainKotlin = buildLogicDir.resolve("src/main/kotlin").also { if (!it.exists()) it.createDirectories() }
        val basePkg = srcMainKotlin.resolve("com/${safeOrg.replace('.', '/')}/convention").also { if (!it.exists()) it.createDirectories() }
        val helpersDir = basePkg.resolve("helpers").also { if (!it.exists()) it.createDirectories() }

        // helpers
        listOf("AndroidLibraryDefaults.kt", "StandardTestDependencies.kt", "TestOptions.kt").forEach { fname ->
            val raw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/conventionPlugins/helpers/$fname")
            val rewritten = raw.replace(Regex("(?m)^package\\s+.*$"), "package com.$safeOrg.convention.helpers")
            val target = helpersDir.resolve(fname)
            if (!target.exists()) {
                fs.writeText(target, rewritten)
                changed = true
            }
        }

        fun copyConvDiAware(name: String) {
            // Prefer DI-flavored convention plugin templates.
            val diFlavor = when {
                p.diKoin && p.diKoinAnnotations -> "koinAnnotations"
                p.diKoin -> "koin"
                else -> "hilt"
            }
            val path = "templates/cleanArchitecture/buildLogic/conventionPlugins/$diFlavor/$name"
            val raw = templateRepo.getTemplateText(path)
            val replaced = replacePackageTokens(raw, safeOrg)
            val target = basePkg.resolve(name)
            if (!target.exists()) {
                fs.writeText(target, replaced)
                changed = true
            }
        }

        if ("data" in enabledModules) copyConvDiAware("DataConventionPlugin.kt")
        if ("di" in enabledModules) copyConvDiAware("DIConventionPlugin.kt")
        if ("domain" in enabledModules) copyConvDiAware("DomainConventionPlugin.kt")
        if ("presentation" in enabledModules) copyConvDiAware("PresentationConventionPlugin.kt")
        if ("dataSource" in enabledModules) copyConvDiAware("DataSourceConventionPlugin.kt")
        if ("remoteDataSource" in enabledModules) copyConvDiAware("RemoteDataSourceConventionPlugin.kt")
        if ("localDataSource" in enabledModules) copyConvDiAware("LocalDataSourceConventionPlugin.kt")

        return changed
    }
}
