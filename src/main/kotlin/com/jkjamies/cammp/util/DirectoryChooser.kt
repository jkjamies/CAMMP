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

package com.jkjamies.cammp.util

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import java.nio.file.Paths

/**
 * Opens a single-folder chooser scoped to the project base and returns the selected path, or null.
 * If currentPath is non-blank, it is used as the initial selection.
 */
fun chooseDirectoryPath(project: Project, currentPath: String?): String? {
    val descriptor = FileChooserDescriptorFactory.createSingleFolderDescriptor()
    val basePath = project.basePath
    if (basePath != null) {
        val baseVf = LocalFileSystem.getInstance().refreshAndFindFileByPath(basePath)
        if (baseVf != null) descriptor.withRoots(baseVf)
    }
    val toSelect = if (!currentPath.isNullOrBlank()) VfsUtil.findFile(Paths.get(currentPath).normalize(), true) else null
    val file = FileChooser.chooseFile(descriptor, project, toSelect)
    return file?.path
}
