package org.jetbrains.plugins.template.util

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

