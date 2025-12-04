package com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository

interface TemplateRepository {
    fun getTemplateText(resourcePath: String): String
}

