package com.example.workbench.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.calculator.UnitConverter
import com.example.workbench.calculator.WorkBenchCalculator
import com.example.workbench.model.AppSettings
import com.example.workbench.model.WorkshopDimension
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DimensionDetailDialog(
    dimension: WorkshopDimension,
    settings: AppSettings,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onCalculate: () -> Unit,
    onCopyAll: () -> Unit
) {
    val unit = dimension.unit
    val places = settings.decimalPlaces

    val lengthDisplay = dimension.lengthMm?.let { UnitConverter.fromMillimeters(it, unit) }
    val widthDisplay = dimension.widthMm?.let { UnitConverter.fromMillimeters(it, unit) }
    val heightDisplay = dimension.heightMm?.let { UnitConverter.fromMillimeters(it, unit) }
    val thicknessDisplay = dimension.thicknessMm?.let { UnitConverter.fromMillimeters(it, unit) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .padding(vertical = 24.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Top bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = dimension.category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = dimension.name,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Canvas Diagram (if enabled)
                if (settings.showDimensionDiagram && (lengthDisplay != null || widthDisplay != null || heightDisplay != null)) {
                    DimensionDiagram(
                        length = lengthDisplay,
                        width = widthDisplay,
                        height = heightDisplay,
                        thickness = thicknessDisplay,
                        unit = unit,
                        decimalPlaces = places,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // Dimension Breakdown Table
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Text(
                            text = "DIMENSION SPECIFICATION",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(10.dp))

                        if (lengthDisplay != null) {
                            DimensionRow(
                                label = "Length",
                                symbol = "L",
                                value = DimensionFormatter.formatMeasurement(lengthDisplay, unit, places)
                            )
                        }
                        if (widthDisplay != null) {
                            DimensionRow(
                                label = "Depth / Width",
                                symbol = "D",
                                value = DimensionFormatter.formatMeasurement(widthDisplay, unit, places)
                            )
                        }
                        if (heightDisplay != null) {
                            DimensionRow(
                                label = "Height",
                                symbol = "H",
                                value = DimensionFormatter.formatMeasurement(heightDisplay, unit, places)
                            )
                        }
                        if (thicknessDisplay != null) {
                            DimensionRow(
                                label = "Top / Wall Thickness",
                                symbol = "T",
                                value = DimensionFormatter.formatMeasurement(thicknessDisplay, unit, places)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Calculated Information: Area & Volume
                val hasArea = dimension.lengthMm != null && dimension.widthMm != null
                val hasVolume = dimension.lengthMm != null && dimension.widthMm != null && dimension.heightMm != null

                if (hasArea || hasVolume) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            Text(
                                text = "CALCULATED PROPERTIES (INFORMATIONAL)",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(8.dp))

                            if (hasArea && settings.showArea) {
                                val areaM2 = WorkBenchCalculator.calculateAreaInSquareMeters(dimension.lengthMm!!, dimension.widthMm!!)
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Top Area", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        DimensionFormatter.formatArea(areaM2, places),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            if (hasVolume && settings.showVolume) {
                                val volM3 = WorkBenchCalculator.calculateVolumeInCubicMeters(
                                    dimension.lengthMm!!,
                                    dimension.widthMm!!,
                                    dimension.heightMm!!
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text("Volume", style = MaterialTheme.typography.bodyMedium)
                                    Text(
                                        DimensionFormatter.formatVolume(volM3, places + 1),
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Bold,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                        }
                    }
                }

                if (!dimension.notes.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "NOTES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = dimension.notes,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = onCalculate,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Calculate, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Calculate")
                    }

                    OutlinedButton(
                        onClick = onCopyAll,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy All")
                    }

                    OutlinedButton(
                        onClick = onEdit
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit")
                    }
                }
            }
        }
    }
}

@Composable
private fun DimensionRow(
    label: String,
    symbol: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "$symbol ",
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            fontFamily = FontFamily.Monospace,
            color = MaterialTheme.colorScheme.primary
        )
    }
}
