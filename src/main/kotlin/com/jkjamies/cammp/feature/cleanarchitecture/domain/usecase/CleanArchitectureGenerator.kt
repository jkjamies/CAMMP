package com.jkjamies.cammp.feature.cleanarchitecture.domain.usecase

import com.jkjamies.cammp.feature.cleanarchitecture.data.AliasesRepositoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.data.AnnotationModuleRepositoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.data.ConventionPluginRepositoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.data.FileSystemRepositoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.data.GradleSettingsRepositoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.data.TemplateRepositoryImpl
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AliasesRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.ConventionPluginRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.PluginType
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import java.nio.file.Path

/**
 * Generates a Clean Architecture module structure for a feature.
 *
 * @param fs The [FileSystemRepository] to use for file operations.
 * @param templateRepo The [TemplateRepository] to use for loading templates.
 * @param settingsRepo The [GradleSettingsRepository] to use for updating Gradle settings.
 * @param annotationModuleRepo The [AnnotationModuleRepository] to use for generating annotation modules.
 * @param conventionPluginRepo The [ConventionPluginRepository] to use for generating convention plugins.
 * @param aliasesRepo The [AliasesRepository] to use for generating the Aliases.kt file.
 */
class CleanArchitectureGenerator(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val templateRepo: TemplateRepository = TemplateRepositoryImpl(),
    private val settingsRepo: GradleSettingsRepository = GradleSettingsRepositoryImpl(),
    private val annotationModuleRepo: AnnotationModuleRepository = AnnotationModuleRepositoryImpl(fs),
    private val conventionPluginRepo: ConventionPluginRepository = ConventionPluginRepositoryImpl(fs),
    private val aliasesRepo: AliasesRepository = AliasesRepositoryImpl(fs),
) {

    /**
     * @param p The [CleanArchitectureParams] for generating the modules.
     * @return A [Result] containing the [CleanArchitectureResult], or an exception.
     */
    operator fun invoke(p: CleanArchitectureParams): Result<CleanArchitectureResult> = runCatching {
        require(fs.isDirectory(p.projectBasePath)) { "Project base path does not exist or is not a directory: ${p.projectBasePath}" }

        val featureName = p.feature.split('-')
            .mapIndexed { index, s -> if (index > 0) s.replaceFirstChar { it.titlecase() } else s }
            .joinToString("")
            .replaceFirstChar { it.lowercase() }
        val featureDirName = p.feature // Keep original for directory structure

        val rootDir = p.projectBasePath.resolve(p.root).also { if (!fs.exists(it)) fs.createDirectories(it) }
        val featureDir = rootDir.resolve(featureDirName).also { if (!fs.exists(it)) fs.createDirectories(it) }

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
            if (fs.exists(moduleDir)) {
                skipped += m
            } else {
                fs.createDirectories(moduleDir)
                scaffoldModuleBuildFile(moduleDir, p, m, featureName, modules)
                scaffoldModuleSourceSkeleton(moduleDir, p, m, featureName)
                created += m
            }
        }

        val settingsUpdated = settingsRepo.ensureIncludes(p.projectBasePath, p.root, featureDirName, modules)
        settingsRepo.ensureIncludeBuild(p.projectBasePath, "build-logic")
        settingsRepo.ensureVersionCatalogPluginAliases(p.projectBasePath, p.orgCenter, modules)
        val appDependencyUpdated = settingsRepo.ensureAppDependency(p.projectBasePath, p.root, featureDirName)
        val buildLogicCreated = scaffoldBuildLogic(p.projectBasePath, p.orgCenter, modules, p)

        val msg = if (created.isEmpty()) {
            "No modules created (all existed). Settings and build-logic ensured."
        } else {
            "Created modules: ${created.joinToString()}. Settings and build-logic updated."
        }

        CleanArchitectureResult(
            created = created,
            skipped = skipped,
            settingsUpdated = settingsUpdated || appDependencyUpdated,
            buildLogicCreated = buildLogicCreated,
            message = msg,
        )
    }

    private fun sanitizeOrgCenter(input: String): String {
        val trimmed = input.trim()
        val unwrapped = if (trimmed.startsWith("\${") && trimmed.endsWith("}")) {
            trimmed.removePrefix("\${").removeSuffix("}")
        } else trimmed
        val withoutLeading = unwrapped.removePrefix("com.").removePrefix("org.")
        val cleaned = withoutLeading.replace(Regex("[^A-Za-z0-9_.]"), "").trim('.')
        return cleaned.ifBlank { "cammp" }.replaceFirstChar { it.lowercase() }
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

    private fun scaffoldModuleBuildFile(
        moduleDir: Path,
        p: CleanArchitectureParams,
        moduleName: String,
        featureName: String,
        enabledModules: List<String>
    ) {
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
        val namespace = "com.$safeOrg.${p.root}.$featureName.$moduleName"

        val projectPrefix = buildString {
            append(':')
            val rootSegments = p.root.replace('\\', '/').split('/').filter { it.isNotBlank() }
            if (rootSegments.isNotEmpty()) {
                append(rootSegments.joinToString(":"))
                append(':')
            }
            append(p.feature)
        }

        val dependencies = buildString {
            when (moduleName) {
                "data" -> {
                    appendLine("    implementation(project(\"$projectPrefix:domain\"))")
                }

                "domain" -> {
                    // domain pulls nothing
                }

                "di" -> {
                    enabledModules.filter { it != "di" }.forEach { dep ->
                        appendLine("    implementation(project(\"$projectPrefix:$dep\"))")
                    }
                }

                "presentation" -> {
                    appendLine("    implementation(project(\"$projectPrefix:domain\"))")
                }

                "dataSource", "remoteDataSource", "localDataSource" -> {
                    appendLine("    implementation(project(\"$projectPrefix:data\"))")
                }
            }
        }

        val content = raw
            .replace(Regex("\\$\\{\\s*NAMESPACE\\s*}"), namespace)
            .replace(Regex("\\$\\{\\s*DEPENDENCIES\\s*}"), dependencies.trimEnd())
            .let { replacePackageTokens(it, safeOrg) }
        fs.writeText(moduleDir.resolve("build.gradle.kts"), content)
    }

    private fun scaffoldModuleSourceSkeleton(
        moduleDir: Path,
        p: CleanArchitectureParams,
        moduleName: String,
        featureName: String
    ) {
        val safeOrg = sanitizeOrgCenter(p.orgCenter)
        val pkg = "com.$safeOrg.${p.root}.$featureName.$moduleName"
        val srcMainKotlin = moduleDir.resolve("src/main/kotlin").also { if (!fs.exists(it)) fs.createDirectories(it) }
        moduleDir.resolve("src/test/kotlin").also { if (!fs.exists(it)) fs.createDirectories(it) }
        val pkgDir = srcMainKotlin.resolve(pkg.replace('.', '/')).also { if (!fs.exists(it)) fs.createDirectories(it) }
        val placeholder = """
            package $pkg

            class Placeholder
        """.trimIndent()
        val placeholderFile = pkgDir.resolve("Placeholder.kt")
        if (!fs.exists(placeholderFile)) fs.writeText(placeholderFile, placeholder)

        // Create standard subpackages
        when (moduleName) {
            "domain" -> {
                listOf("repository", "model", "usecase").forEach { sub ->
                    val d = pkgDir.resolve(sub); if (!fs.exists(d)) fs.createDirectories(d)
                }
            }

            "data" -> {
                val repoDir = pkgDir.resolve("repository"); if (!fs.exists(repoDir)) fs.createDirectories(repoDir)
                if (p.includeDatasource) {
                    if (p.datasourceCombined) {
                        val d = pkgDir.resolve("dataSource"); if (!fs.exists(d)) fs.createDirectories(d)
                    } else {
                        if (p.datasourceRemote) {
                            val d = pkgDir.resolve("remoteDataSource"); if (!fs.exists(d)) fs.createDirectories(d)
                        }
                        if (p.datasourceLocal) {
                            val d = pkgDir.resolve("localDataSource"); if (!fs.exists(d)) fs.createDirectories(d)
                        }
                    }
                }
            }

            "di" -> {
                // When Koin Annotations are selected, create a ComponentScan module here.
                if (p.diKoin && p.diKoinAnnotations) {
                    val scanBase = "com.${sanitizeOrgCenter(p.orgCenter)}.${p.root}.$featureName"
                    annotationModuleRepo.generate(
                        outputDirectory = pkgDir,
                        packageName = pkg,
                        scanPackage = scanBase,
                        featureName = featureName
                    )
                }
            }
        }
    }

    private fun scaffoldBuildLogic(
        projectBase: Path,
        orgCenter: String,
        enabledModules: List<String>,
        p: CleanArchitectureParams
    ): Boolean {
        val buildLogicDir = projectBase.resolve("build-logic")
        var changed = false
        if (!fs.exists(buildLogicDir)) {
            fs.createDirectories(buildLogicDir)
            changed = true
        }
        // settings.gradle.kts
        val settingsPath = buildLogicDir.resolve("settings.gradle.kts")
        if (!fs.exists(settingsPath)) {
            val settings = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/settings.gradle.kts")
            fs.writeText(settingsPath, settings)
            changed = true
        }
        // build.gradle.kts
        val safeOrg = sanitizeOrgCenter(orgCenter)
        val buildRaw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/build.gradle.kts")
        val buildText = replacePackageTokens(buildRaw, safeOrg)
        val buildPath = buildLogicDir.resolve("build.gradle.kts")
        if (!fs.exists(buildPath)) {
            fs.writeText(buildPath, buildText)
            changed = true
        }

        // src/main/kotlin/com/<org>/convention and helpers
        val srcMainKotlin = buildLogicDir.resolve("src/main/kotlin").also { if (!fs.exists(it)) fs.createDirectories(it) }
        val basePkg = srcMainKotlin.resolve("com/${safeOrg.replace('.', '/')}/convention")
            .also { if (!fs.exists(it)) fs.createDirectories(it) }
        val helpersDir = basePkg.resolve("helpers").also { if (!fs.exists(it)) fs.createDirectories(it) }
        val coreDir = basePkg.resolve("core").also { if (!fs.exists(it)) fs.createDirectories(it) }

        val conventionPackage = "com.$safeOrg.convention"

        // helpers
        if (!fs.exists(helpersDir.resolve("AndroidLibraryDefaults.kt"))) {
            val raw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/conventionPlugins/helpers/AndroidLibraryDefaults.kt")
            val content = replacePackageTokens(raw, safeOrg)
            fs.writeText(helpersDir.resolve("AndroidLibraryDefaults.kt"), content)
            changed = true
        }
        if (!fs.exists(helpersDir.resolve("StandardTestDependencies.kt"))) {
            val raw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/conventionPlugins/helpers/StandardTestDependencies.kt")
            val content = replacePackageTokens(raw, safeOrg)
            fs.writeText(helpersDir.resolve("StandardTestDependencies.kt"), content)
            changed = true
        }
        if (!fs.exists(helpersDir.resolve("TestOptions.kt"))) {
            val raw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/conventionPlugins/helpers/TestOptions.kt")
            val content = replacePackageTokens(raw, safeOrg)
            fs.writeText(helpersDir.resolve("TestOptions.kt"), content)
            changed = true
        }

        val diMode = when {
            p.diKoin && p.diKoinAnnotations -> DiMode.KOIN_ANNOTATIONS
            p.diKoin -> DiMode.KOIN
            else -> DiMode.HILT
        }

        // core
        if (!fs.exists(coreDir.resolve("Aliases.kt"))) {
            val tomlPath = projectBase.resolve("gradle/libs.versions.toml")
            aliasesRepo.generateAliases(coreDir, "$conventionPackage.core", diMode, tomlPath)
            changed = true
        }
        if (!fs.exists(coreDir.resolve("Dependencies.kt"))) {
            val raw = templateRepo.getTemplateText("templates/cleanArchitecture/buildLogic/conventionPlugins/core/Dependencies.kt")
            val content = replacePackageTokens(raw, safeOrg)
            fs.writeText(coreDir.resolve("Dependencies.kt"), content)
            changed = true
        }

        fun generatePlugin(type: PluginType, fileName: String) {
            val target = basePkg.resolve(fileName)
            if (!fs.exists(target)) {
                conventionPluginRepo.generate(basePkg, conventionPackage, diMode, type)
                changed = true
            }
        }

        if ("data" in enabledModules) generatePlugin(PluginType.DATA, "DataConventionPlugin.kt")
        if ("di" in enabledModules) generatePlugin(PluginType.DI, "DIConventionPlugin.kt")
        if ("domain" in enabledModules) generatePlugin(PluginType.DOMAIN, "DomainConventionPlugin.kt")
        if ("presentation" in enabledModules) generatePlugin(PluginType.PRESENTATION, "PresentationConventionPlugin.kt")
        if ("dataSource" in enabledModules) generatePlugin(PluginType.DATA_SOURCE, "DataSourceConventionPlugin.kt")
        if ("remoteDataSource" in enabledModules) generatePlugin(PluginType.REMOTE_DATA_SOURCE, "RemoteDataSourceConventionPlugin.kt")
        if ("localDataSource" in enabledModules) generatePlugin(PluginType.LOCAL_DATA_SOURCE, "LocalDataSourceConventionPlugin.kt")

        return changed
    }
}
