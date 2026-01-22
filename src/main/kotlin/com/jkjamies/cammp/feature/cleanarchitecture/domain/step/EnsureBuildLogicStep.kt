package com.jkjamies.cammp.feature.cleanarchitecture.domain.step

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DiStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.DatasourceStrategy
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.BuildLogicScaffoldRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.DiMode
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject

@ContributesIntoSet(AppScope::class)
class EnsureBuildLogicStep(
    private val buildLogicRepo: BuildLogicScaffoldRepository,
) : CleanArchitectureStep {

    override suspend fun execute(params: CleanArchitectureParams): StepResult = runCatching {
        val modules = resolveEnabledModules(params)
        val diMode = resolveDiMode(params)
        val updated = buildLogicRepo.ensureBuildLogic(params, modules, diMode)
        StepResult.BuildLogic(updated = updated, message = "- build-logic updated: $updated")
    }.getOrElse { StepResult.Failure(it) }

    private fun resolveEnabledModules(p: CleanArchitectureParams): List<String> = buildList {
        add("domain")
        add("data")
        if (p.includeApiModule) add("api")
        if (p.includeDiModule) add("di")
        if (p.includePresentation) add("presentation")

        when (p.datasourceStrategy) {
            DatasourceStrategy.None -> Unit
            DatasourceStrategy.Combined -> add("dataSource")
            DatasourceStrategy.RemoteOnly -> add("remoteDataSource")
            DatasourceStrategy.LocalOnly -> add("localDataSource")
            DatasourceStrategy.RemoteAndLocal -> {
                add("remoteDataSource")
                add("localDataSource")
            }
        }
    }

    private fun resolveDiMode(p: CleanArchitectureParams): DiMode = when (val s = p.diStrategy) {
        DiStrategy.Hilt -> DiMode.HILT
        DiStrategy.Metro -> DiMode.HILT // TODO: Metro support (for now generate same as Hilt)
        is DiStrategy.Koin -> if (s.useAnnotations) DiMode.KOIN_ANNOTATIONS else DiMode.KOIN
    }
}
