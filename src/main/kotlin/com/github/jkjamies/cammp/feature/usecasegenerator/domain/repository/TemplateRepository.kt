package com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository

interface TemplateRepository {
    fun getTemplateText(resourcePath: String): String
}
