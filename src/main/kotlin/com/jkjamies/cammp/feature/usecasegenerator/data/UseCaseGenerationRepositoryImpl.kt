/*
 * Copyright 2025-2026 Jason Jamieson
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jkjamies.cammp.feature.usecasegenerator.data

import com.jkjamies.cammp.domain.codegen.toCleanString
import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.usecasegenerator.data.factory.UseCaseSpecFactory
import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationResult
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
internal class UseCaseGenerationRepositoryImpl(
    private val specFactory: UseCaseSpecFactory
) : UseCaseGenerationRepository {

    override fun generateUseCase(
        params: UseCaseParams,
        packageName: String,
        baseDomainPackage: String,
        apiDir: Path?
    ): UseCaseGenerationResult {
        var interfacePath: Path? = null
        val interfaceFqn = if (apiDir?.toFile()?.exists() == true) {
            val interfacePkg = packageName.replace(".domain.", ".api.")
            val interfaceFileSpec = specFactory.createInterface(interfacePkg, params.className)
            val apiTargetDir = apiDir.resolve("src/main/kotlin").resolve(interfacePkg.replace('.', '/'))
            apiTargetDir.createDirectories()
            val out = apiTargetDir.resolve("${params.className}.kt")
            out.writeText(interfaceFileSpec.toString())
            interfacePath = out
            "$interfacePkg.${params.className}"
        } else null

        val fileSpec = specFactory.create(packageName, params, baseDomainPackage, interfaceFqn)
        val content = fileSpec.toCleanString()

        val targetDir = params.domainDir.resolve("src/main/kotlin").resolve(packageName.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}.kt")
        out.writeText(content)
        return UseCaseGenerationResult(out, interfacePath)
    }
}
