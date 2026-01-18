package com.jkjamies.cammp.feature.welcome.presentation

import androidx.compose.runtime.Composable
import com.jkjamies.cammp.feature.ComposeBasedTestCase
import com.jkjamies.cammp.feature.welcome.presentation.robot.WelcomeScreenRobot
import org.junit.Test

internal class WelcomeScreenTest : ComposeBasedTestCase() {

    override val contentUnderTest: @Composable () -> Unit = {
        WelcomeScreen()
    }

    @Test
    fun `verify welcome title is displayed`() = runComposeTest {
        val robot = WelcomeScreenRobot(this)
        robot.verifyTextIsDisplayed("Welcome to CAMMP")
        robot.verifyTextIsDisplayed("Clean Architecture Multiplatform Modular Plugin")
    }

    @Test
    fun `verify all sections expand and show content`() = runComposeTest {
        val robot = WelcomeScreenRobot(this)

        // Recommended Workflow
        robot.clickOnTag("RecommendedWorkflow")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("To get the most out of CAMMP, recommended is the following workflow:")

        // Clean Architecture Generator
        robot.clickOnTag("CleanArchitectureGenerator")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("Generates a full feature module structure including data, domain, and presentation layers. It also generates convention plugins and updates your settings.gradle.kts and build-logic.")
        
        robot.clickOnTag("CleanArch:WhatIsGenerated")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("Feature module structure (data, domain, presentation)")
        robot.verifyTextIsDisplayed("Optional 'api' module for Use Case interfaces")
        
        robot.clickOnTag("CleanArch:ConventionPlugins")
        composableRule.waitForIdle()
        robot.verifyTextContains("This generator adds convention plugins to your project and updates the version catalog (libs.versions.toml) with the generated convention plugins. It assumes the existence of a version catalog.")
        robot.verifyTextContains("AGP Version: ")
        robot.verifyTextContains("Kotlin/JDK Version: ")
        
        robot.clickOnTag("CleanArch:VersionCatalogUpdates")
        composableRule.waitForIdle()
        robot.verifyTextContains("Dependencies in the libs.versions.toml file will be used if they exist. If they do not exist, they will be added automatically.")

        robot.clickOnTag("CleanArch:Plugins")
        composableRule.waitForIdle()
        robot.verifyTextContains("android-library")

        robot.clickOnTag("CleanArch:Dependencies")
        composableRule.waitForIdle()
        robot.verifyTextContains("kotlinx-serialization")

        robot.clickOnTag("CleanArch:TestDependencies")
        composableRule.waitForIdle()
        robot.verifyTextContains("kotest-runner")

        robot.clickOnTag("CleanArch:UITestDependencies")
        composableRule.waitForIdle()
        robot.verifyTextContains("androidx-test-runner")

        robot.clickOnTag("CleanArch:RootConfig")
        composableRule.waitForIdle()
        robot.verifyTextContains("Please ensure the following plugins are added to your root build.gradle.kts if not already present:")

        robot.clickOnTag("CleanArch:DI")
        composableRule.waitForIdle()
        robot.verifyTextContains("Supports Hilt, Koin, and Metro (coming soon). For Koin, you can choose between standard DSL and Koin Annotations.")
        robot.verifyTextContains("Hilt Setup: ")

        // Repository Generator
        robot.clickOnTag("RepositoryGenerator")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("Generates a Repository interface in the domain layer and an implementation in the data layer. Can also generate Datasources.")
        
        robot.clickOnTag("Repo:WhatIsGenerated")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("Repository Interface (in domain)")
        
        robot.clickOnTag("Repo:Assumptions")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("You select a 'data' module directory.")

        // Use Case Generator
        robot.clickOnTag("UseCaseGenerator")
        composableRule.waitForIdle()
        robot.verifyTextContains("Generates a UseCase class in the domain layer.")
        
        robot.clickOnTag("UseCase:WhatIsGenerated")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("UseCase class (in domain)")
        robot.verifyTextIsDisplayed("UseCase interface (in 'api' module, if it exists)")
        
        robot.clickOnTag("UseCase:Assumptions")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("You select a 'domain' module directory.")
        
        // Presentation Generator
        robot.clickOnTag("PresentationGenerator")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("Generates a Screen, ViewModel, and related components (State, Intent/Event). Supports MVI and MVVM patterns.")
        
        robot.clickOnTag("Presentation:WhatIsGenerated")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("Screen Composable")
        
        robot.clickOnTag("Presentation:Assumptions")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("You select a 'presentation' module directory.")
        robot.verifyTextContains("If a feature has both 'api' and 'domain' modules, UseCases from the 'api' module are preferred")
        robot.verifyTextContains("Navigation: ")
        
        robot.clickOnTag("Presentation:UIPatterns")
        composableRule.waitForIdle()
        robot.verifyTextIsDisplayed("Choose between MVI (Model-View-Intent), MVVM (Model-View-ViewModel), or Circuit (coming soon) architectures.")
    }
}
