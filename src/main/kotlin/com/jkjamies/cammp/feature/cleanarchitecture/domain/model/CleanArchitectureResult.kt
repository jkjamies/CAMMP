package com.jkjamies.cammp.feature.cleanarchitecture.domain.model

data class CleanArchitectureResult(
    val created: List<String>,
    val skipped: List<String>,
    val settingsUpdated: Boolean,
    val buildLogicCreated: Boolean,
    val message: String,
)
