package com.jkjamies.cammp.feature.repositorygenerator.data.factory

import com.jkjamies.cammp.feature.repositorygenerator.domain.model.RepositoryParams
import com.squareup.kotlinpoet.FileSpec

interface RepositorySpecFactory {
    fun createDomainInterface(packageName: String, params: RepositoryParams): FileSpec
    fun createDataImplementation(dataPackage: String, domainPackage: String, params: RepositoryParams): FileSpec
}
