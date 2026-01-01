package com.jkjamies.cammp.feature.cleanarchitecture.data

import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class TemplateRepositoryImpl : TemplateRepository {
    override fun getTemplateText(resourcePath: String): String {
        val url = this::class.java.classLoader.getResource(resourcePath)
            ?: error("Template not found: $resourcePath")
        return url.readText()
    }
}