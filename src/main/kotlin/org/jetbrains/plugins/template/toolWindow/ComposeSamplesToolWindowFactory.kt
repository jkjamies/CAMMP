package org.jetbrains.plugins.template.toolWindow

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesIntent
import com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesScreen
import com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesUiState
import com.jkjamies.cammp.feature.cleanarchitecture.presentation.GenerateModulesViewModel
import com.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationGeneratorScreen
import com.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationIntent
import com.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationUiState
import com.jkjamies.cammp.feature.presentationgenerator.presentation.PresentationViewModel
import com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryGeneratorScreen
import com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryIntent
import com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryUiState
import com.jkjamies.cammp.feature.repositorygenerator.presentation.RepositoryViewModel
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseGeneratorScreen
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseIntent
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseUiState
import com.jkjamies.cammp.feature.usecasegenerator.presentation.UseCaseViewModel
import com.jkjamies.cammp.feature.welcome.presentation.WelcomeScreen
import org.jetbrains.jewel.bridge.addComposeTab
import org.jetbrains.plugins.template.util.chooseDirectoryPath
import org.jetbrains.plugins.template.util.refreshUseCases

class ComposeSamplesToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun shouldBeAvailable(project: Project) = true

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        cleanArchitecture(project, toolWindow)
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

        toolWindow.addComposeTab("Welcome") {
            WelcomeScreen()
        }

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
