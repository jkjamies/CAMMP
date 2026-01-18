package com.jkjamies.cammp.feature.usecasegenerator.testutil

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationResult
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseMergeOutcome
import com.jkjamies.cammp.feature.usecasegenerator.domain.step.StepResult
import com.jkjamies.cammp.feature.usecasegenerator.domain.step.UseCaseStep
import java.nio.file.Path

internal class UseCaseStepFake(
    private val results: ArrayDeque<StepResult> = ArrayDeque(),
) : UseCaseStep {
    val calls = mutableListOf<UseCaseParams>()

    override suspend fun execute(params: UseCaseParams): StepResult {
        calls.add(params)
        return results.removeFirstOrNull() ?: StepResult.Success(null)
    }
}

internal class ModulePackageRepositoryFake(
    private val mapping: Map<Path, String?> = emptyMap(),
    private val defaultPkg: String? = null,
) : ModulePackageRepository {
    val calls = mutableListOf<Path>()

    override fun findModulePackage(moduleDir: Path): String? {
        calls.add(moduleDir)
        return mapping[moduleDir] ?: defaultPkg
    }
}

internal class UseCaseGenerationRepositoryFake(
    private val onGenerate: ((UseCaseParams, String, String, Path?) -> UseCaseGenerationResult)? = null,
) : UseCaseGenerationRepository {
    data class Call(val params: UseCaseParams, val packageName: String, val baseDomainPackage: String, val apiDir: Path?)

    val calls = mutableListOf<Call>()

    override fun generateUseCase(params: UseCaseParams, packageName: String, baseDomainPackage: String, apiDir: Path?): UseCaseGenerationResult {
        calls.add(Call(params, packageName, baseDomainPackage, apiDir))
        return onGenerate?.invoke(params, packageName, baseDomainPackage, apiDir)
            ?: UseCaseGenerationResult(params.domainDir.resolve("${params.className}.kt"))
    }
}

internal class UseCaseDiModuleRepositoryFake(
    private val outcome: UseCaseMergeOutcome = UseCaseMergeOutcome(Path.of("Module.kt"), "updated"),
) : UseCaseDiModuleRepository {
    data class Call(
        val diDir: Path,
        val diPackage: String,
        val useCaseSimpleName: String,
        val useCaseFqn: String,
        val repositoryFqns: List<String>,
        val diStrategy: DiStrategy,
        val useCaseInterfaceFqn: String?,
    )

    val calls = mutableListOf<Call>()

    override fun mergeUseCaseModule(
        diDir: Path,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
        diStrategy: DiStrategy,
        useCaseInterfaceFqn: String?,
    ): UseCaseMergeOutcome {
        calls.add(Call(diDir, diPackage, useCaseSimpleName, useCaseFqn, repositoryFqns, diStrategy, useCaseInterfaceFqn))
        return outcome.copy(outPath = diDir.resolve(outcome.outPath.fileName.toString()))
    }
}
