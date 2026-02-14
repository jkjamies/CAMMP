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

import com.intellij.openapi.diagnostic.Logger
import dev.zacsweers.metro.AppScope
import com.jkjamies.cammp.domain.codegen.PackageSuffixes
import com.jkjamies.cammp.feature.usecasegenerator.data.datasource.PackageMetadataDataSource
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import java.nio.file.Files
import java.nio.file.Paths
import java.util.stream.Collectors

@ContributesBinding(AppScope::class)
internal class RepositoryDiscoveryRepositoryImpl(
    private val packageMetadataDataSource: PackageMetadataDataSource
) : RepositoryDiscoveryRepository {

    private val log = Logger.getInstance(RepositoryDiscoveryRepositoryImpl::class.java)

    override fun loadRepositories(domainModulePath: String): List<String> {
        return try {
            val moduleDir = Paths.get(domainModulePath)
            val useCasePackage = packageMetadataDataSource.findModulePackage(moduleDir)
            val repoPackage = useCasePackage?.let { pkg ->
                if (pkg.endsWith(PackageSuffixes.USE_CASE)) pkg.removeSuffix(PackageSuffixes.USE_CASE) + PackageSuffixes.REPOSITORY else pkg + PackageSuffixes.REPOSITORY
            } ?: return emptyList()

            val packagePath = repoPackage.replace('.', '/')
            val kotlinRoot = moduleDir.resolve("src/main/kotlin")
            val repoDir = kotlinRoot.resolve(packagePath)
            if (!Files.exists(repoDir) || !Files.isDirectory(repoDir)) return emptyList()

            Files.walk(repoDir)
                .use { stream ->
                    stream.filter { Files.isRegularFile(it) && it.fileName.toString().endsWith(".kt") }
                        .map { it.fileName.toString().removeSuffix(".kt") }
                        .filter { it.isNotBlank() }
                        .distinct()
                        .sorted()
                        .collect(Collectors.toList())
                }
        } catch (t: Throwable) {
            log.warn("Failed to discover repositories in $domainModulePath", t)
            emptyList()
        }
    }
}
