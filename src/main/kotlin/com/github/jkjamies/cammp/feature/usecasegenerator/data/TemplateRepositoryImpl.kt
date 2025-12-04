package com.github.jkjamies.cammp.feature.usecasegenerator.data

import com.github.jkjamies.cammp.feature.usecasegenerator.domain.repository.TemplateRepository

class TemplateRepositoryImpl : TemplateRepository {
    override fun getTemplateText(resourcePath: String): String {
        val url = this::class.java.classLoader.getResource(resourcePath)
            ?: error("Template not found: $resourcePath")
        return url.readText()
    }
}
