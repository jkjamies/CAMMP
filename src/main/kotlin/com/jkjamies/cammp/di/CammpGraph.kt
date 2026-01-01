package com.jkjamies.cammp.di

import com.jkjamies.cammp.feature.cleanarchitecture.data.datasource.VersionCatalogDataSource
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AliasesRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.AnnotationModuleRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.ConventionPluginRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.GradleSettingsRepository
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.TemplateRepository
import com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesViewModel
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FlowStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.IntentRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.NavigationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationDiModuleRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.PresentationRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ScreenStateHolderRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.UiStateRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ViewModelRepository
import com.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationViewModel
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DataSourceDiscoveryRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DatasourceScaffoldRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.DiModuleRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.RepositoryGenerationRepository
import com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryViewModel
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.RepositoryDiscoveryRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseDiModuleRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.UseCaseGenerationRepository
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseViewModel
import dev.zacsweers.metro.AppScope
import dev.zacsweers.metro.DependencyGraph

// Aliases for duplicated names
import com.jkjamies.cammp.feature.cleanarchitecture.domain.repository.FileSystemRepository as CleanFileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.FileSystemRepository as PresentationFileSystemRepository
import com.jkjamies.cammp.feature.presentationgenerator.domain.repository.ModulePackageRepository as PresentationModulePackageRepository
import com.jkjamies.cammp.feature.repositorygenerator.domain.repository.ModulePackageRepository as RepositoryModulePackageRepository
import com.jkjamies.cammp.feature.usecasegenerator.domain.repository.ModulePackageRepository as UseCaseModulePackageRepository

@DependencyGraph(AppScope::class)
interface CammpGraph {
    // ViewModels
    val generateModulesViewModelFactory: GenerateModulesViewModel.Factory
    val presentationViewModelFactory: PresentationViewModel.Factory
    val repositoryViewModelFactory: RepositoryViewModel.Factory
    val useCaseViewModelFactory: UseCaseViewModel.Factory

    // Clean Architecture Feature
    val cleanFileSystemRepository: CleanFileSystemRepository
    val templateRepository: TemplateRepository
    val gradleSettingsRepository: GradleSettingsRepository
    val annotationModuleRepository: AnnotationModuleRepository
    val conventionPluginRepository: ConventionPluginRepository
    val aliasesRepository: AliasesRepository
    val versionCatalogDataSource: VersionCatalogDataSource

    // Presentation Generator Feature
    val presentationFileSystemRepository: PresentationFileSystemRepository
    val intentRepository: IntentRepository
    val screenRepository: ScreenRepository
    val uiStateRepository: UiStateRepository
    val viewModelRepository: ViewModelRepository
    val navigationRepository: NavigationRepository
    val presentationModulePackageRepository: PresentationModulePackageRepository
    val flowStateHolderRepository: FlowStateHolderRepository
    val screenStateHolderRepository: ScreenStateHolderRepository
    val presentationDiModuleRepository: PresentationDiModuleRepository
    val presentationRepository: PresentationRepository

    // Repository Generator Feature
    val repositoryModulePackageRepository: RepositoryModulePackageRepository
    val diModuleRepository: DiModuleRepository
    val datasourceScaffoldRepository: DatasourceScaffoldRepository
    val dataSourceDiscoveryRepository: DataSourceDiscoveryRepository
    val repositoryGenerationRepository: RepositoryGenerationRepository

    // Use Case Generator Feature
    val useCaseModulePackageRepository: UseCaseModulePackageRepository
    val useCaseDiModuleRepository: UseCaseDiModuleRepository
    val useCaseGenerationRepository: UseCaseGenerationRepository
    val repositoryDiscoveryRepository: RepositoryDiscoveryRepository
}
