package com.jkjamies.cammp.feature.presentationgenerator.data

import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.TemplateRepository

class TemplateRepositoryImpl : TemplateRepository {
    override fun getTemplateText(resourcePath: String): String {
        val url = this::class.java.classLoader.getResource(resourcePath)
            ?: error("Template not found: $resourcePath")
        return url.readText()
    }
}
