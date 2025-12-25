package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

interface TemplateRepository {
    /** Get the text from the template at [resourcePath] */
    fun getTemplateText(resourcePath: String): String
}
