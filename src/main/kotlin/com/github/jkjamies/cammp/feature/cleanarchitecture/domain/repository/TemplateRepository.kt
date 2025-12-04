package com.github.jkjamies.cammp.feature.cleanarchitecture.domain.repository

interface TemplateRepository {
    fun getTemplateText(resourcePath: String): String
}
