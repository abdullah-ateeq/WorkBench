package com.example.workbench.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FileCopy
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.calculator.UnitConverter
import com.example.workbench.calculator.WorkBenchCalculator
import com.example.workbench.model.AppSettings
import com.example.workbench.model.WorkshopDimension
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DimensionCard(
    dimension: WorkshopDimension,
    settings: AppSettings,
    onOpen: () -> Unit,
    onEdit: () -> Unit,
    onDuplicate: () -> Unit,
    onDelete: () -> Unit,
    onCopy: () -> Unit,
    onToggleFavorite: () -> Unit,
    onCalculate: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val unit = dimension.unit
    val places = settings.decimalPlaces

    // Convert stored mm to display unit
    val lengthDisplay = dimension.lengthMm?.let { UnitConverter.fromMillimeters(it, unit) }
    val widthDisplay = dimension.widthMm?.let { UnitConverter.fromMillimeters(it, unit) }
    val heightDisplay = dimension.heightMm?.let { UnitConverter.fromMillimeters(it, unit) }
    val thicknessDisplay = dimension.thicknessMm?.let { UnitConverter.fromMillimeters(it, unit) }

    val formattedDimensions = DimensionFormatter.formatSummaryDimensions(
        length = lengthDisplay,
        width = widthDisplay,
        height = heightDisplay,
        unit = unit,
        decimalPlaces = places
    )

    val dateStr = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(dimension.updatedAt))

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onOpen() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Category, Favorite, Menu/Duplicate
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                SuggestionChip(
                    onClick = {},
                    label = {
                        Text(
                            text = dimension.category.displayName.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                        labelColor = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            if (settings.hapticFeedback) {
                                haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                            }
                            onToggleFavorite()
                        }
                    ) {
                        Icon(
                            imageVector = if (dimension.isFavorite) Icons.Filled.Star else Icons.Outlined.StarBorder,
                            contentDescription = if (dimension.isFavorite) "Remove from favorites" else "Add to favorites",
                            tint = if (dimension.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onCopy) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy dimensions",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // Title
            Text(
                text = dimension.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Primary dimensions
            Text(
                text = formattedDimensions,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                fontFamily = FontFamily.Monospace,
                color = MaterialTheme.colorScheme.primary
            )

            // Thickness callout if present
            if (thicknessDisplay != null) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Thickness: ${DimensionFormatter.formatMeasurement(thicknessDisplay, unit, places)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Optional Area & Volume badges
            val hasArea = settings.showArea && dimension.lengthMm != null && dimension.widthMm != null
            val hasVolume = settings.showVolume && dimension.lengthMm != null && dimension.widthMm != null && dimension.heightMm != null

            if (hasArea || hasVolume) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (hasArea) {
                        val areaM2 = WorkBenchCalculator.calculateAreaInSquareMeters(
                            dimension.lengthMm!!,
                            dimension.widthMm!!
                        )
                        OutlinedCard(
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = "Top Area: ${DimensionFormatter.formatArea(areaM2, places)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (hasVolume) {
                        val volM3 = WorkBenchCalculator.calculateVolumeInCubicMeters(
                            dimension.lengthMm!!,
                            dimension.widthMm!!,
                            dimension.heightMm!!
                        )
                        OutlinedCard(
                            colors = CardDefaults.outlinedCardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                            )
                        ) {
                            Text(
                                text = "Vol: ${DimensionFormatter.formatVolume(volM3, places + 1)}",
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Notes if present
            if (!dimension.notes.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = dimension.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Footer: Date and action buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dateStr,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.outline
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onCalculate) {
                        Icon(
                            imageVector = Icons.Default.Calculate,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Calculate", style = MaterialTheme.typography.labelSmall)
                    }

                    IconButton(onClick = onDuplicate) {
                        Icon(
                            imageVector = Icons.Default.FileCopy,
                            contentDescription = "Duplicate dimension",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onEdit) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit dimension",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(onClick = onDelete) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete dimension",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        }
    }
}
