package com.jkjamies.cammp.feature.usecasegenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.DiModuleDataSource
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.DiStrategy
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseMergeOutcome
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
@Inject
class UseCaseDiModuleRepositoryImpl(
    private val dataSource: DiModuleDataSource
) : UseCaseDiModuleRepository {

    override fun mergeUseCaseModule(
        diDir: Path,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
        diStrategy: DiStrategy,
    ): UseCaseMergeOutcome {
        if (diStrategy !is DiStrategy.Koin) {
            val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
            val out = diTargetDir.resolve("UseCaseModule.kt")
            return UseCaseMergeOutcome(out, "skipped")
        }

        val diTargetDir = diDir.resolve("src/main/kotlin").resolve(diPackage.replace('.', '/'))
        if (!diTargetDir.exists()) diTargetDir.createDirectories()
        val out = diTargetDir.resolve("UseCaseModule.kt")
        val existing = if (out.exists()) out.readText() else null

        val content = dataSource.generateKoinModuleContent(
            existing,
            diPackage,
            useCaseSimpleName,
            useCaseFqn,
            repositoryFqns
        )

        val changed = existing == null || existing != content
        out.writeText(content)
        val status = when {
            existing == null -> "created"
            changed -> "updated"
            else -> "exists"
        }
        return UseCaseMergeOutcome(out, status)
    }
}
