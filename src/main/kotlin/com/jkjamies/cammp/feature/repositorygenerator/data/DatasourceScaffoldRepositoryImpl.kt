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
import com.jkjamies.cammp.feature.repositorygenerator.data.factory.DataSourceSpecFactory
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.writeText

@ContributesBinding(AppScope::class)
class DatasourceScaffoldRepositoryImpl(
    private val specFactory: DataSourceSpecFactory
) : DatasourceScaffoldRepository {

    override fun generateInterface(
        directory: Path,
        packageName: String,
        className: String
    ): Path {
        directory.createDirectories()
        val fileSpec = specFactory.createInterface(packageName, className)
        val outFile = directory.resolve("$className.kt")
        outFile.writeText(fileSpec.toString())
        return outFile
    }

    override fun generateImplementation(
        directory: Path,
        packageName: String,
        className: String,
        interfacePackage: String,
        interfaceName: String,
        useKoin: Boolean
    ): Path {
        directory.createDirectories()
        val fileSpec = specFactory.createImplementation(
            packageName = packageName,
            className = className,
            interfacePackage = interfacePackage,
            interfaceName = interfaceName,
            useKoin = useKoin
        )
        val outFile = directory.resolve("$className.kt")
        outFile.writeText(fileSpec.toString())
        return outFile
    }
}
