package com.jkjamies.cammp.feature.cleanarchitecture.domain.repository

import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureParams
import com.jkjamies.cammp.feature.cleanarchitecture.domain.model.CleanArchitectureResult

/**
 * Repository that performs IO + template application to scaffold a clean-architecture feature.
 *
 * Steps decide what should be generated and when. Repositories do the actual reading/writing/merging.
 */
interface CleanArchitectureScaffoldRepository {
    suspend fun generateModules(params: CleanArchitectureParams): CleanArchitectureResult
}

