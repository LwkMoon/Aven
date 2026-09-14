package com.aven.app.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Insights
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class AvenDestination(
    val route: String,
    val title: String,
    val icon: ImageVector? = null,
    val testTag: String = ""
) {
    data object Onboarding : AvenDestination("onboarding", "Onboarding", testTag = "nav_onboarding")
    data object Home : AvenDestination("home", "Home", Icons.Outlined.Home, testTag = "nav_home")
    data object Garden : AvenDestination("garden", "Garden", Icons.Outlined.Eco, testTag = "nav_garden")
    data object Insights : AvenDestination("insights", "Insights", Icons.Outlined.Insights, testTag = "nav_insights")
    data object Settings : AvenDestination("settings", "Settings", Icons.Outlined.Settings, testTag = "nav_settings")

    data object Intervention : AvenDestination(
        route = "intervention/{packageName}/{appName}",
        title = "Intervention",
        testTag = "nav_intervention"
    ) {
        fun createRoute(packageName: String, appName: String): String {
            return "intervention/$packageName/$appName"
        }
    }

    companion object {
        val bottomNavDestinations: List<AvenDestination>
            get() = listOf(Home, Garden, Insights, Settings)
    }
}
