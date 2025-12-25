package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

import java.nio.file.Path

interface AnnotationModuleRepository {
    fun generate(
        outputDirectory: Path,
        packageName: String,
        scanPackage: String,
        featureName: String
    )
}
