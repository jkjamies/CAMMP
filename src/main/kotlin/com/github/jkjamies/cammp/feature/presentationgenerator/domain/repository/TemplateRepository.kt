package com.github.jkjamies.cammp.feature.presentationgenerator.domain.repository

interface TemplateRepository {
    fun getTemplateText(resourcePath: String): String
}
