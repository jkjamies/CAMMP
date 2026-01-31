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

package com.jkjamies.cammp.feature.repositorygenerator.data

import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.feature.repositorygenerator.data.factory.RepositorySpecFactory
import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
class RepositoryGenerationRepositoryImpl(
    private val specFactory: RepositorySpecFactory
) : RepositoryGenerationRepository {

    override fun generateDomainLayer(params: RepositoryParams, packageName: String, domainDir: Path): Path {
        val fileSpec = specFactory.createDomainInterface(packageName, params)
        val targetDir = domainDir.resolve("src/main/kotlin").resolve(packageName.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}.kt")
        out.writeText(fileSpec.toString().replace("`data`", "data"))
        return out
    }

    override fun generateDataLayer(params: RepositoryParams, dataPackage: String, domainPackage: String): Path {
        val fileSpec = specFactory.createDataImplementation(dataPackage, domainPackage, params)
        val targetDir = params.dataDir.resolve("src/main/kotlin").resolve(dataPackage.replace('.', '/'))
        targetDir.createDirectories()
        val out = targetDir.resolve("${params.className}Impl.kt")
        out.writeText(
            fileSpec.toString().replace("`data`", "data")
                .replace("import org.koin.core.`annotation`.Single", "import org.koin.core.annotation.Single")
        )
        return out
    }
}
