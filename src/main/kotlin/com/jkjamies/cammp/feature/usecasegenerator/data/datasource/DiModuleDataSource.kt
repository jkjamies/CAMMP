package com.jkjamies.cammp.feature.usecasegenerator.data.datasource

interface DiModuleDataSource {
    fun generateKoinModuleContent(
        existingContent: String?,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        repositoryFqns: List<String>,
        useCaseInterfaceFqn: String? = null
    ): String

    fun generateHiltModuleContent(
        existingContent: String?,
        diPackage: String,
        useCaseSimpleName: String,
        useCaseFqn: String,
        useCaseInterfaceFqn: String
    ): String
}