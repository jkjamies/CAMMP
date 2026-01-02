package com.jkjamies.cammp.feature.usecasegenerator.data.factory

import com.jkjamies.cammp.feature.usecasegenerator.domain.model.UseCaseParams
import com.squareup.kotlinpoet.FileSpec

interface UseCaseSpecFactory {
    fun create(packageName: String, params: UseCaseParams, baseDomainPackage: String): FileSpec
}