package com.jkjamies.cammp.feature.repositorygenerator.domain.usecase

import com.jkjamies.cammp.feature.repositorygenerator.data.DiModuleRepositoryImpl
import com.jkjamies.cammp.feature.repositorygenerator.data.DatasourceScaffoldRepositoryImpl
import com.jkjamies.cammp.feature.repositorygenerator.data.ModulePackageRepositoryImpl
import com.jkjamies.cammp.feature.repositorygenerator.data.RepositoryGenerationRepositoryImpl
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceOptions
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository

class RepositoryGenerator(
    private val modulePkgRepo: ModulePackageRepository = ModulePackageRepositoryImpl(),
    private val diRepo: DiModuleRepository = DiModuleRepositoryImpl(),
    private val dsRepo: DatasourceScaffoldRepository = DatasourceScaffoldRepositoryImpl(),
    private val generationRepo: RepositoryGenerationRepository = RepositoryGenerationRepositoryImpl()
) {
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

        val domainOut = generationRepo.generateDomainLayer(params, domainFull, domainDir)
        results += "- Domain: $domainOut (generated)"

        val dataOut = generationRepo.generateDataLayer(params, dataFull, domainFull)
        results += "- Data: $dataOut (generated)"

        // DI module generation (Hilt or Koin). For Koin Annotations, skip manual merge.
        run {
            val diDir = params.dataDir.parent?.resolve("di")
                ?: error("Could not locate sibling di module for ${params.dataDir}")
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

    private fun truncateAt(pkg: String?, marker: String): String {
        if (pkg == null) return ""
        val idx = pkg.indexOf(marker)
        return if (idx >= 0) pkg.substring(0, idx + marker.length) else pkg
    }

    private fun stripRepositorySuffix(name: String): String =
        if (name.endsWith("Repository") && name.length > "Repository".length) name.removeSuffix("Repository") else name
}
