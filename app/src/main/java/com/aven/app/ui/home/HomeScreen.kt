package com.aven.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowForward
import androidx.compose.material.icons.outlined.Eco
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.aven.app.androidintegration.permissions.SystemPermissionHelper
import com.aven.app.core.garden.GardenEngine
import com.aven.app.data.local.entities.MonitoredAppEntity
import com.aven.app.ui.components.AvenLogoMark
import com.aven.app.ui.components.EnvironmentalInspectorDialog
import com.aven.app.ui.components.GardenCanvas
import com.aven.app.ui.components.StatCard
import com.aven.app.ui.theme.LocalAvenExtendedColors

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToGarden: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onSimulateLaunch: (String, String) -> Unit
) {
    val state by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    var inspectedElement by remember { mutableStateOf<GardenEngine.EnvironmentalElement?>(null) }
    var showAppPickerModal by remember { mutableStateOf(false) }

    val extendedColors = LocalAvenExtendedColors.current
    val currentStage = GardenEngine.GardenStage.fromName(state.gardenState.stage)

    if (inspectedElement != null) {
        EnvironmentalInspectorDialog(
            element = inspectedElement!!,
            onDismiss = { inspectedElement = null }
        )
    }

    if (showAppPickerModal) {
        AppSimulationDialog(
            apps = state.monitoredApps,
            onDismiss = { showAppPickerModal = false },
            onSelectApp = { app ->
                showAppPickerModal = false
                onSimulateLaunch(app.packageName, app.displayName)
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(scrollState)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .testTag("home_screen")
    ) {
        // Top Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = state.greeting,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Light,
                        letterSpacing = 0.5.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Aven",
                    style = MaterialTheme.typography.displayMedium.copy(
                        fontWeight = FontWeight.Normal,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }

            // Quick Simulate Launch button
            OutlinedButton(
                onClick = { showAppPickerModal = true },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.testTag("home_simulate_launch_button")
            ) {
                Icon(
                    imageVector = Icons.Outlined.PlayArrow,
                    contentDescription = "Simulate app launch",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("Simulate launch", style = MaterialTheme.typography.labelMedium)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // System Settings Guidance Card
        val context = LocalContext.current
        val isAccessibilityActive = SystemPermissionHelper.isAccessibilityServiceEnabled(context)
        val isOverlayActive = SystemPermissionHelper.canDrawOverlays(context)

        if (!isAccessibilityActive || !isOverlayActive) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.15f),
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.error.copy(alpha = 0.3f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("system_settings_guidance_card")
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Outlined.Tune,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "System Configuration Required",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Text(
                        text = "To automatically open the conscious pause screen when launch habits (like Instagram), Aven needs you to configure two essential Android settings.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (!isAccessibilityActive) {
                            OutlinedButton(
                                onClick = { SystemPermissionHelper.openAccessibilitySettings(context) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Accessibility", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                        if (!isOverlayActive) {
                            OutlinedButton(
                                onClick = { SystemPermissionHelper.openOverlaySettings(context) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Text("Display Over Apps", style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
            }
        }

        // Warning state: No monitored apps configured
        if (!state.hasMonitoredApps) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .testTag("no_apps_warning")
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "No monitored apps selected",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Choose which apps you want to pause before opening.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Button(
                        onClick = onNavigateToSettings,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    ) {
                        Text("Configure")
                    }
                }
            }
        }

        // Section: "Your world"
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Your world",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onNavigateToGarden() }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "${currentStage.displayName} \u2022 ${state.gardenState.environmentState}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowForward,
                    contentDescription = "Open garden details",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        // Dominant Garden View Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .clip(RoundedCornerShape(22.dp))
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    shape = RoundedCornerShape(22.dp)
                )
                .clickable { onNavigateToGarden() }
                .testTag("home_garden_card")
        ) {
            GardenCanvas(
                gardenState = state.gardenState,
                isReducedMotion = state.isReducedMotion,
                onElementTapped = { inspectedElement = it }
            )

            // Stage badge overlay
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(16.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.90f))
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Text(
                    text = "Stage ${currentStage.ordinal + 1}: ${currentStage.displayName}",
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Section: "Today"
        Text(
            text = "Today",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(10.dp))

        if (state.isFirstDayEmpty) {
            // Day 1 / Empty State per Section 9.2:
            // "garden shows Seed stage; stats show clear zero-state copy, not blank white space or N/A."
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("home_empty_state")
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "A quiet beginning",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Your garden begins with a single seed. Whenever you reach for a monitored app, a moment of conscious pause helps it take root.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try tapping 'Simulate launch' above to experience your first checkpoint.",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        } else {
            // Today's Stats grid
            val summary = state.todaySummary!!
            val total = summary.interventions
            val intentionalCount = summary.intentionalLaunches
            val mindlessCount = summary.mindlessLaunches
            val avoidedCount = summary.avoidedLaunches

            val totalDecisions = intentionalCount + mindlessCount + avoidedCount
            val intentionalPct = if (totalDecisions > 0) ((intentionalCount + avoidedCount) * 100) / totalDecisions else 0
            val mindlessPct = if (totalDecisions > 0) (mindlessCount * 100) / totalDecisions else 0

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Intentional",
                        value = "$intentionalPct%",
                        subtitle = "$intentionalCount purposeful opens",
                        badgeColor = extendedColors.intentionalTag,
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Mindless",
                        value = "$mindlessPct%",
                        subtitle = "$mindlessCount automatic opens",
                        badgeColor = extendedColors.uncertainTag,
                        modifier = Modifier.weight(1f)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    StatCard(
                        title = "Interventions",
                        value = "$total",
                        subtitle = "Moments of awareness",
                        modifier = Modifier.weight(1f)
                    )
                    StatCard(
                        title = "Avoided",
                        value = "$avoidedCount",
                        subtitle = "Times you stepped away",
                        badgeColor = extendedColors.vegetationPrimary,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun AppSimulationDialog(
    apps: List<MonitoredAppEntity>,
    onDismiss: () -> Unit,
    onSelectApp: (MonitoredAppEntity) -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Simulate app launch",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Select an app to trigger the Aven awareness checkpoint, exactly as if you opened it on your phone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(18.dp))

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    val availableApps = if (apps.isNotEmpty()) apps else listOf(
                        MonitoredAppEntity("com.instagram.android", "Instagram", true, "Social"),
                        MonitoredAppEntity("com.twitter.android", "X / Twitter", true, "Social")
                    )

                    availableApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .clickable { onSelectApp(app) }
                                .padding(horizontal = 16.dp, vertical = 12.dp)
                                .testTag("simulate_app_${app.displayName.lowercase().replace(' ', '_')}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = app.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = app.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(
                                imageVector = Icons.Outlined.PlayArrow,
                                contentDescription = "Launch",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.CenterEnd) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                }
            }
        }
    }
}
