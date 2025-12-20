package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository

class TemplateRepositoryImpl : TemplateRepository {
    override fun getTemplateText(resourcePath: String): String {
        val url = this::class.java.classLoader.getResource(resourcePath)
            ?: error("Template not found: $resourcePath")
        return url.readText()
    }
}
