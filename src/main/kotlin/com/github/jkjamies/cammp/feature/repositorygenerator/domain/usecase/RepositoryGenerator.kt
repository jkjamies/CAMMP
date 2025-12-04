package com.github.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.github.jkjamies.cammp.feature.repositorygenerator.data.FileSystemRepositoryImpl
import com.github.jkjamies.cammp.feature.repositorygenerator.data.ModulePackageRepositoryImpl
import com.github.jkjamies.cammp.feature.repositorygenerator.data.TemplateRepositoryImpl
import com.github.jkjamies.cammp.feature.repositorygenerator.data.DiModuleRepositoryImpl
import com.github.jkjamies.cammp.feature.repositorygenerator.data.DatasourceScaffoldRepositoryImpl
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.FileSystemRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.TemplateRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceOptions
import com.github.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams

/**
 * Generates a repository interface and implementation, and optionally datasources and DI modules.
 *
 * @param fs The [FileSystemRepository] to use for file operations.
 * @param templates The [TemplateRepository] to use for loading templates.
 * @param modulePkgRepo The [ModulePackageRepository] to use for finding module packages.
 * @param diRepo The [DiModuleRepository] to use for merging DI modules.
 * @param dsRepo The [DatasourceScaffoldRepository] to use for scaffolding datasources.
 */
class RepositoryGenerator(
    private val fs: FileSystemRepository = FileSystemRepositoryImpl(),
    private val templates: TemplateRepository = TemplateRepositoryImpl(),
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl(),
    private val diRepo: DiModuleRepository = DiModuleRepositoryImpl(),
    private val dsRepo: DatasourceScaffoldRepository = DatasourceScaffoldRepositoryImpl(),
) {
    /**
     * @param params The [RepositoryParams] for generating the repository.
     * @return A [Result] containing a summary of the generation, or an exception.
     */
    suspend operator fun invoke(params: RepositoryParams): Result<String> = runCatching {
        val results = mutableListOf<String>()
        val dataExisting = modulePkgRepo.findModulePackage(params.dataDir)
        val dataBase = truncateAt(dataExisting, ".data")

        val domainDir = params.dataDir.parent?.resolve("domain")
            ?: error("Could not locate sibling domain module for ${params.dataDir}")
        val domainExisting = modulePkgRepo.findModulePackage(domainDir)
        val domainBase = truncateAt(domainExisting, ".domain")

        val domainFull = "$domainBase.repository"
        val dataFull = "$dataBase.repository"

        val domainContent = renderDomainTemplate(domainFull, params)
        val dataContent = renderDataTemplate(dataFull, domainFull, params)

        // Write domain interface first
        val domainTargetDir = domainDir.resolve("src/main/kotlin").resolve(domainFull.replace('.', '/'))
        fs.createDirectories(domainTargetDir)
        val domainOut = domainTargetDir.resolve("${params.className}.kt")
        val domainOld = fs.readFile(domainOut)
        fs.writeFile(domainOut, domainContent)
        val domainStatus = when {
            domainOld == null -> "created"
            domainOld != domainContent -> "updated"
            else -> "exists"
        }
        results += "- Domain: $domainOut ($domainStatus)"

        // Write data implementation
        val dataTargetDir = params.dataDir.resolve("src/main/kotlin").resolve(dataFull.replace('.', '/'))
        fs.createDirectories(dataTargetDir)
        val dataOut = dataTargetDir.resolve("${params.className}Impl.kt")
        val dataOld = fs.readFile(dataOut)
        fs.writeFile(dataOut, dataContent)
        val dataStatus = when {
            dataOld == null -> "created"
            dataOld != dataContent -> "updated"
            else -> "exists"
        }
        results += "- Data: $dataOut ($dataStatus)"

        // DI module generation (Hilt or Koin). For Koin Annotations, skip manual merge.
        run {
            val diDir = params.dataDir.parent?.resolve("di") ?: error("Could not locate sibling di module for ${params.dataDir}")
            val diExisting = modulePkgRepo.findModulePackage(diDir)
            val diBase = truncateAt(diExisting, ".di")
            if (!(params.useKoin && params.koinAnnotations)) {
                val outcome = diRepo.mergeRepositoryModule(
                    diDir = diDir,
                    diPackage = diBase,
                    className = params.className,
                    domainFqn = domainFull,
                    dataFqn = dataFull,
                    useKoin = params.useKoin,
                )
                results += "- DI: ${outcome.outPath} (${outcome.status})"
            }
        }

        if (params.includeDatasource) {
            val diDir = params.dataDir.parent?.resolve("di")
            val diExisting = diDir?.let { modulePkgRepo.findModulePackage(it) }
            val diBase = diExisting?.let { truncateAt(it, ".di") }
            val options = DatasourceOptions(
                include = params.includeDatasource,
                combined = params.datasourceCombined,
                remote = params.datasourceRemote,
                local = params.datasourceLocal,
                useKoin = params.useKoin,
                koinAnnotations = params.koinAnnotations,
            )
            val baseName = stripRepositorySuffix(params.className)
            val msgs = dsRepo.generate(
                dataDir = params.dataDir,
                dataBasePkg = dataBase,
                repositoryBaseName = baseName,
                diDir = diDir,
                diPackage = diBase,
                options = options,
            )
            results += msgs
        }

        val title = "Repository generation completed:"
        (sequenceOf(title) + results.asSequence()).joinToString("\n")
    }

    private fun truncateAt(pkg: String, marker: String): String {
        val idx = pkg.indexOf(marker)
        return if (idx >= 0) pkg.substring(0, idx + marker.length) else pkg
    }

    private fun renderDataTemplate(dataPackage: String, domainPackage: String, p: RepositoryParams): String {
        val template = templates.getTemplateText("templates/repositoryGenerator/RepositoryImpl.kt")

        val baseName = stripRepositorySuffix(p.className)
        val dataBasePkg = dataPackage.substringBeforeLast(".repository")

        val generatedFqns = buildList {
            if (p.includeDatasource) {
                if (p.datasourceCombined) {
                    add("$dataBasePkg.dataSource.${baseName}DataSource")
                } else {
                    if (p.datasourceRemote) add("$dataBasePkg.remoteDataSource.${baseName}RemoteDataSource")
                    if (p.datasourceLocal) add("$dataBasePkg.localDataSource.${baseName}LocalDataSource")
                }
            }
        }
        val allFqns: List<String> = (p.selectedDataSources + generatedFqns).distinct()

        val datasourceImports = allFqns.joinToString(separator = "\n") { fqn -> "import $fqn" }.trimEnd()

        fun toParamName(typeName: String): String = typeName.replaceFirstChar { it.lowercase() }
        val constructorParams = allFqns.joinToString(separator = ",\n    ") { fqn ->
            val simple = fqn.substringAfterLast('.')
            "private val ${toParamName(simple)}: $simple"
        }

        val diImport = if (p.useKoin && p.koinAnnotations) "import org.koin.core.annotation.Single" else ""
        val diAnnotation = if (p.useKoin && p.koinAnnotations) "@Single" else ""
        return template
            .replace("\${PACKAGE}", dataPackage)
            .replace("\${DI_IMPORT}", diImport)
            .replace("\${DI_ANNOTATION}", diAnnotation)
            .replace("\${CLASS_NAME}", p.className)
            .replace("\${DOMAIN_FQN}", domainPackage)
            .replace("\${DATASOURCE_IMPORTS}", if (datasourceImports.isBlank()) "" else datasourceImports + "\n")
            .replace("\${CONSTRUCTOR_PARAMS}", constructorParams)
    }

    private fun renderDomainTemplate(domainPackage: String, p: RepositoryParams): String {
        val template = templates.getTemplateText("templates/repositoryGenerator/Repository.kt")
        return template
            .replace("\${PACKAGE}", domainPackage)
            .replace("\${CLASS_NAME}", p.className)
    }


    private fun stripRepositorySuffix(name: String): String =
        if (name.endsWith("Repository") && name.length > "Repository".length) name.removeSuffix("Repository") else name
}
