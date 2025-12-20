package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

interface TemplateRepository {
    fun getTemplateText(resourcePath: String): String
}
