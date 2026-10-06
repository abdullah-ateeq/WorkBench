package com.example.workbench.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workbench.model.AppThemeMode
import com.example.workbench.model.DimensionUnit
import com.example.workbench.ui.components.DeleteConfirmationDialog
import com.example.workbench.viewmodel.WorkBenchViewModel
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    viewModel: WorkBenchViewModel,
    modifier: Modifier = Modifier
) {
    val settings by viewModel.settings.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var themeMenuExpanded by remember { mutableStateOf(false) }
    var unitMenuExpanded by remember { mutableStateOf(false) }
    var decimalMenuExpanded by remember { mutableStateOf(false) }

    var showClearDimensionsDialog by remember { mutableStateOf(false) }
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    var showResetSettingsDialog by remember { mutableStateOf(false) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Column(modifier = Modifier.padding(top = 16.dp, bottom = 16.dp)) {
                Text(
                    text = "SETTINGS",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Application Preferences & Units",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Appearance Section
            SettingsSectionHeader(title = "APPEARANCE")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Theme selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Theme", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                text = when (settings.themeMode) {
                                    AppThemeMode.SYSTEM -> "System Default"
                                    AppThemeMode.LIGHT -> "Light Mode"
                                    AppThemeMode.DARK -> "Dark Mode"
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        androidx.compose.foundation.layout.Box {
                            OutlinedButton(onClick = { themeMenuExpanded = true }) {
                                Text(settings.themeMode.name)
                            }
                            DropdownMenu(
                                expanded = themeMenuExpanded,
                                onDismissRequest = { themeMenuExpanded = false }
                            ) {
                                AppThemeMode.entries.forEach { mode ->
                                    DropdownMenuItem(
                                        text = { Text(mode.name) },
                                        onClick = {
                                            viewModel.updateSettings { it.copy(themeMode = mode) }
                                            themeMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Dynamic Color
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dynamic Color", style = MaterialTheme.typography.bodyLarge)
                            Text("Material You colors on Android 12+", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Switch(
                            checked = settings.dynamicColor,
                            onCheckedChange = { checked ->
                                viewModel.updateSettings { it.copy(dynamicColor = checked) }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Measurement Section
            SettingsSectionHeader(title = "MEASUREMENTS & PRECISION")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Default Unit
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Default Unit", style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "${settings.defaultUnit.displayName} (${settings.defaultUnit.symbol})",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        androidx.compose.foundation.layout.Box {
                            OutlinedButton(onClick = { unitMenuExpanded = true }) {
                                Text(settings.defaultUnit.symbol)
                            }
                            DropdownMenu(
                                expanded = unitMenuExpanded,
                                onDismissRequest = { unitMenuExpanded = false }
                            ) {
                                DimensionUnit.entries.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text("${u.displayName} (${u.symbol})") },
                                        onClick = {
                                            viewModel.updateSettings { it.copy(defaultUnit = u) }
                                            unitMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    // Decimal Places
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Decimal Places", style = MaterialTheme.typography.bodyLarge)
                            Text("${settings.decimalPlaces} decimal digits", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        androidx.compose.foundation.layout.Box {
                            OutlinedButton(onClick = { decimalMenuExpanded = true }) {
                                Text("${settings.decimalPlaces}")
                            }
                            DropdownMenu(
                                expanded = decimalMenuExpanded,
                                onDismissRequest = { decimalMenuExpanded = false }
                            ) {
                                (0..4).forEach { p ->
                                    DropdownMenuItem(
                                        text = { Text("$p places") },
                                        onClick = {
                                            viewModel.updateSettings { it.copy(decimalPlaces = p) }
                                            decimalMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Display & Technical Options
            SettingsSectionHeader(title = "DISPLAY & BEHAVIOR")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    // Show Area
                    SettingsSwitchRow(
                        title = "Show Top Area",
                        subtitle = "Display surface area calculations on dimensions",
                        checked = settings.showArea,
                        onCheckedChange = { chk -> viewModel.updateSettings { it.copy(showArea = chk) } }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Show Volume
                    SettingsSwitchRow(
                        title = "Show Volume",
                        subtitle = "Display volume calculations for 3D records",
                        checked = settings.showVolume,
                        onCheckedChange = { chk -> viewModel.updateSettings { it.copy(showVolume = chk) } }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Show Dimension Diagram
                    SettingsSwitchRow(
                        title = "Show Dimension Diagram",
                        subtitle = "Technical Compose Canvas isometric wireframe",
                        checked = settings.showDimensionDiagram,
                        onCheckedChange = { chk -> viewModel.updateSettings { it.copy(showDimensionDiagram = chk) } }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Show Calculation Formula
                    SettingsSwitchRow(
                        title = "Show Calculation Formula",
                        subtitle = "Display mathematical step breakdown in calculator",
                        checked = settings.showCalculationFormula,
                        onCheckedChange = { chk -> viewModel.updateSettings { it.copy(showCalculationFormula = chk) } }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Haptic Feedback
                    SettingsSwitchRow(
                        title = "Haptic Feedback",
                        subtitle = "Light haptic vibration on key actions",
                        checked = settings.hapticFeedback,
                        onCheckedChange = { chk -> viewModel.updateSettings { it.copy(hapticFeedback = chk) } }
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp))

                    // Confirm Delete
                    SettingsSwitchRow(
                        title = "Confirm Delete",
                        subtitle = "Show confirmation dialog before removing records",
                        checked = settings.confirmDelete,
                        onCheckedChange = { chk -> viewModel.updateSettings { it.copy(confirmDelete = chk) } }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Data Management Section
            SettingsSectionHeader(title = "DATA MANAGEMENT")

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    TextButton(
                        onClick = { showResetSettingsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Reset Settings to Default")
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    TextButton(
                        onClick = { showClearHistoryDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Calculation History", color = MaterialTheme.colorScheme.error)
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 6.dp))

                    TextButton(
                        onClick = { showClearDimensionsDialog = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Clear Saved Dimensions", color = MaterialTheme.colorScheme.error)
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }
    }

    if (showResetSettingsDialog) {
        DeleteConfirmationDialog(
            title = "Reset Settings",
            message = "Reset all preferences back to default values?",
            confirmButtonText = "Reset",
            onConfirm = {
                viewModel.resetSettings()
                showResetSettingsDialog = false
                scope.launch { snackbarHostState.showSnackbar("Settings reset") }
            },
            onDismiss = { showResetSettingsDialog = false }
        )
    }

    if (showClearHistoryDialog) {
        DeleteConfirmationDialog(
            title = "Clear Calculation History",
            message = "Remove all saved calculator history items?",
            confirmButtonText = "Clear History",
            onConfirm = {
                viewModel.clearAllHistory()
                showClearHistoryDialog = false
                scope.launch { snackbarHostState.showSnackbar("Calculation history cleared") }
            },
            onDismiss = { showClearHistoryDialog = false }
        )
    }

    if (showClearDimensionsDialog) {
        DeleteConfirmationDialog(
            title = "Clear Saved Dimensions",
            message = "Remove all saved workshop dimensions? This cannot be undone.",
            confirmButtonText = "Clear Dimensions",
            onConfirm = {
                viewModel.clearAllDimensions()
                showClearDimensionsDialog = false
                scope.launch { snackbarHostState.showSnackbar("Saved dimensions cleared") }
            },
            onDismiss = { showClearDimensionsDialog = false }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
}

@Composable
private fun SettingsSwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, style = MaterialTheme.typography.bodyLarge)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
