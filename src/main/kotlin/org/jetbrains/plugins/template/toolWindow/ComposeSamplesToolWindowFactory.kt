package org.jetbrains.plugins.template.toolWindow

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.github.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesIntent
import com.github.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesUiState
import com.github.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesViewModel
import com.github.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesScreen
import com.github.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationGeneratorScreen
import com.github.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationUiState
import com.github.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationIntent
import com.github.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationViewModel
import com.github.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryGeneratorScreen
import com.github.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryUiState
import com.github.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryIntent
import com.github.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryViewModel
import com.github.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseGeneratorScreen
import com.github.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseUiState
import com.github.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseIntent
import com.github.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseViewModel
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import java.nio.file.Paths
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.plugins.template.CoroutineScopeHolder
import org.jetbrains.plugins.template.chatApp.ChatAppSample
import org.jetbrains.plugins.template.chatApp.repository.ChatRepository
import org.jetbrains.plugins.template.chatApp.viewmodel.ChatViewModel
import org.jetbrains.plugins.template.weatherApp.model.Location
import org.jetbrains.plugins.template.weatherApp.services.LocationsProvider
import org.jetbrains.plugins.template.weatherApp.services.WeatherForecastService
import org.jetbrains.plugins.template.weatherApp.ui.WeatherAppSample
import org.jetbrains.plugins.template.weatherApp.ui.WeatherAppViewModel
import org.jetbrains.plugins.template.util.chooseDirectoryPath
import org.jetbrains.plugins.template.util.findRepositoriesGroupedByPackage
import org.jetbrains.plugins.template.util.refreshUseCases

class ComposeSamplesToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
//        weatherApp(project, toolWindow)
//        chatApp(project, toolWindow)
        cleanArchitecture(project, toolWindow)
    }

    private fun weatherApp(project: Project, toolWindow: ToolWindow) {
        // create ViewModel once per tool window
        val viewModel = WeatherAppViewModel(
            listOf(Location("Munich", "Germany")),
            project.service<CoroutineScopeHolder>()
                .createScope(::WeatherAppViewModel.name),
            WeatherForecastService()
        )
        Disposer.register(toolWindow.disposable, viewModel)

        toolWindow.addComposeTab("Weather App") {
            LaunchedEffect(Unit) {
                viewModel.onReloadWeatherForecast()
            }

            WeatherAppSample(
                viewModel,
                viewModel,
                service<LocationsProvider>()
            )
        }
    }

    private fun chatApp(project: Project, toolWindow: ToolWindow) {
        val viewModel = ChatViewModel(
            project.service<CoroutineScopeHolder>()
                .createScope(ChatViewModel::class.java.simpleName),
            service<ChatRepository>()
        )
        Disposer.register(toolWindow.disposable, viewModel)

        toolWindow.addComposeTab("Chat App") { ChatAppSample(viewModel) }
    }

    private fun cleanArchitecture(project: Project, toolWindow: ToolWindow) {
        val basePath = project.basePath
        val initial = GenerateModulesUiState(
            projectBasePath = basePath,
            root = basePath ?: "",
            orgCenter = basePath?.substringAfterLast('/') ?: "cammp",
        )
        val vm = GenerateModulesViewModel(initial)
        Disposer.register(toolWindow.disposable) { /* no-op; simple VM without resources */ }

        // Hoist generator ViewModels to persist across recompositions
        val presentationInitial = PresentationUiState(directory = project.basePath ?: "")
        val presentationVm = PresentationViewModel(presentationInitial)

        val repositoryInitial = RepositoryUiState(domainPackage = project.basePath ?: "")
        val repositoryVm = RepositoryViewModel(repositoryInitial)

        val useCaseInitial = UseCaseUiState(domainPackage = project.basePath ?: "")
        val useCaseVm = UseCaseViewModel(useCaseInitial)

        toolWindow.addComposeTab("Clean Architecture") {
            val state by vm.state.collectAsState()
            val onBrowseRoot: () -> Unit = {
                val selected = chooseDirectoryPath(project, state.root)
                if (selected != null) {
                    vm.handleIntent(GenerateModulesIntent.SetRoot(selected))
                }
            }
            GenerateModulesScreen(state = state, onIntent = vm::handleIntent, onBrowseRoot = onBrowseRoot)
        }

        toolWindow.addComposeTab("Repository Generator") {
            val state by repositoryVm.state.collectAsState()
            val onBrowseDataDir: () -> Unit = {
                val selected = chooseDirectoryPath(project, state.domainPackage)
                if (selected != null) {
                    repositoryVm.handleIntent(RepositoryIntent.SetDomainPackage(selected))
                }
            }
            RepositoryGeneratorScreen(state = state, onIntent = repositoryVm::handleIntent, onBrowseDataDir = onBrowseDataDir)
        }

        toolWindow.addComposeTab("Use Case Generator") {
            val state by useCaseVm.state.collectAsState()
            val onBrowseDomainDir: () -> Unit = {
                val selected = chooseDirectoryPath(project, state.domainPackage)
                if (selected != null) {
                    useCaseVm.handleIntent(UseCaseIntent.SetDomainPackage(selected))
                }
            }
            UseCaseGeneratorScreen(state = state, onIntent = useCaseVm::handleIntent, onBrowseDomainDir = onBrowseDomainDir)
        }

        toolWindow.addComposeTab("Presentation Generator") {
            val state by presentationVm.state.collectAsState()
            val onBrowseDir: () -> Unit = {
                val selected = chooseDirectoryPath(project, state.directory)
                if (selected != null) {
                    presentationVm.handleIntent(PresentationIntent.SetDirectory(selected))
                    refreshUseCases(project)?.let { presentationVm.handleIntent(PresentationIntent.SetUseCasesByModule(it)) }
                }
            }

            LaunchedEffect(Unit) {
                if (state.useCasesByModule.isEmpty()) {
                    refreshUseCases(project)?.let { presentationVm.handleIntent(PresentationIntent.SetUseCasesByModule(it)) }
                }
            }

            PresentationGeneratorScreen(state = state, onIntent = presentationVm::handleIntent, onBrowseDirectory = onBrowseDir)
        }
    }
}
