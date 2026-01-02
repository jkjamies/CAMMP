package com.jkjamies.cammp.feature.usecasegenerator.domain.repository

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import java.nio.file.Path

data class UseCaseMergeOutcome(val outPath: Path, val status: String)

interface UseCaseDiModuleRepository {
    /** Create or merge the UseCase DI module (Hilt or Koin) and write it. */
    fun mergeUseCaseModule(
        diDir: Path,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
        diStrategy: DiStrategy,
    ): UseCaseMergeOutcome
}
