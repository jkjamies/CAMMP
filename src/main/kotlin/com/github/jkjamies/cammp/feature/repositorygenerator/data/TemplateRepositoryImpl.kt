package com.github.jkjamies.cammp.feature.repositorygenerator.data

import com.github.jkjamies.cammp.feature.repositorygenerator.domain.repository.TemplateRepository

class TemplateRepositoryImpl : TemplateRepository {
    override fun getTemplateText(resourcePath: String): String {
        val stream = this::class.java.getResourceAsStream("/$resourcePath")
            ?: error("Template not found: $resourcePath")
        return stream.bufferedReader(Charsets.UTF_8).use { it.readText() }
    }
}

