package com.example.workbench.ui.components

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.calculator.UnitConverter
import com.example.workbench.model.DimensionCategory
import com.example.workbench.model.DimensionUnit
import com.example.workbench.model.StarterTemplates
import com.example.workbench.model.WorkshopDimension
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AddEditDimensionDialog(
    initialDimension: WorkshopDimension? = null,
    defaultUnit: DimensionUnit = DimensionUnit.CM,
    onDismiss: () -> Unit,
    onSave: (
        id: String?,
        name: String,
        category: DimensionCategory,
        length: BigDecimal?,
        width: BigDecimal?,
        height: BigDecimal?,
        thickness: BigDecimal?,
        unit: DimensionUnit,
        notes: String?,
        isFavorite: Boolean
    ) -> Unit
) {
    val isEditing = initialDimension != null

    var currentUnit by remember {
        mutableStateOf(initialDimension?.unit ?: defaultUnit)
    }

    var name by remember {
        mutableStateOf(initialDimension?.name ?: "")
    }

    var category by remember {
        mutableStateOf(initialDimension?.category ?: DimensionCategory.CUSTOM)
    }

    // Initialize dimensions converted to currentUnit
    var lengthStr by remember {
        val initialL = initialDimension?.lengthMm?.let {
            UnitConverter.fromMillimeters(it, currentUnit)
        }
        mutableStateOf(initialL?.toPlainString() ?: "")
    }

    var widthStr by remember {
        val initialW = initialDimension?.widthMm?.let {
            UnitConverter.fromMillimeters(it, currentUnit)
        }
        mutableStateOf(initialW?.toPlainString() ?: "")
    }

    var heightStr by remember {
        val initialH = initialDimension?.heightMm?.let {
            UnitConverter.fromMillimeters(it, currentUnit)
        }
        mutableStateOf(initialH?.toPlainString() ?: "")
    }

    var thicknessStr by remember {
        val initialT = initialDimension?.thicknessMm?.let {
            UnitConverter.fromMillimeters(it, currentUnit)
        }
        mutableStateOf(initialT?.toPlainString() ?: "")
    }

    var notes by remember {
        mutableStateOf(initialDimension?.notes ?: "")
    }

    var isFavorite by remember {
        mutableStateOf(initialDimension?.isFavorite ?: false)
    }

    var nameError by remember { mutableStateOf<String?>(null) }
    var lengthError by remember { mutableStateOf<String?>(null) }
    var widthError by remember { mutableStateOf<String?>(null) }
    var heightError by remember { mutableStateOf<String?>(null) }
    var thicknessError by remember { mutableStateOf<String?>(null) }

    var categoryMenuExpanded by remember { mutableStateOf(false) }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    // Helper to handle unit change while preserving physical measurement
    fun updateUnitPreservingMeasurement(newUnit: DimensionUnit) {
        if (newUnit == currentUnit) return
        val oldUnit = currentUnit

        val parsedL = DimensionFormatter.parseBigDecimal(lengthStr)
        val parsedW = DimensionFormatter.parseBigDecimal(widthStr)
        val parsedH = DimensionFormatter.parseBigDecimal(heightStr)
        val parsedT = DimensionFormatter.parseBigDecimal(thicknessStr)

        if (parsedL != null) {
            val converted = UnitConverter.convert(parsedL, oldUnit, newUnit)
            lengthStr = DimensionFormatter.formatNumberTrimZeros(converted, 4)
        }
        if (parsedW != null) {
            val converted = UnitConverter.convert(parsedW, oldUnit, newUnit)
            widthStr = DimensionFormatter.formatNumberTrimZeros(converted, 4)
        }
        if (parsedH != null) {
            val converted = UnitConverter.convert(parsedH, oldUnit, newUnit)
            heightStr = DimensionFormatter.formatNumberTrimZeros(converted, 4)
        }
        if (parsedT != null) {
            val converted = UnitConverter.convert(parsedT, oldUnit, newUnit)
            thicknessStr = DimensionFormatter.formatNumberTrimZeros(converted, 4)
        }

        currentUnit = newUnit
    }

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
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isEditing) "Edit Dimension" else "Add Workshop Dimension",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Templates row (shown only when creating new)
                if (!isEditing) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "STARTING TEMPLATES",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        StarterTemplates.presets.forEach { preset ->
                            FilterChip(
                                selected = category == preset.category && name == preset.defaultName,
                                onClick = {
                                    name = preset.defaultName
                                    category = preset.category
                                    currentUnit = preset.unit
                                    lengthStr = preset.length?.toPlainString() ?: ""
                                    widthStr = preset.width?.toPlainString() ?: ""
                                    heightStr = preset.height?.toPlainString() ?: ""
                                    thicknessStr = preset.thickness?.toPlainString() ?: ""
                                    notes = preset.notes
                                    nameError = null
                                    lengthError = null
                                    widthError = null
                                    heightError = null
                                    thicknessError = null
                                },
                                label = { Text(preset.templateName) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Name field (Required)
                OutlinedTextField(
                    value = name,
                    onValueChange = {
                        name = it
                        if (it.isNotBlank()) nameError = null
                    },
                    label = { Text("Name *") },
                    placeholder = { Text("e.g. Main Workbench, Garage Shelf") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Category and Unit Selection Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Category Dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Category",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { categoryMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = category.displayName,
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = categoryMenuExpanded,
                            onDismissRequest = { categoryMenuExpanded = false }
                        ) {
                            DimensionCategory.entries.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat.displayName) },
                                    onClick = {
                                        category = cat
                                        categoryMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    // Unit Dropdown
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Unit",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        OutlinedButton(
                            onClick = { unitMenuExpanded = true },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "${currentUnit.displayName} (${currentUnit.symbol})",
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                        DropdownMenu(
                            expanded = unitMenuExpanded,
                            onDismissRequest = { unitMenuExpanded = false }
                        ) {
                            DimensionUnit.entries.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.displayName} (${u.symbol})") },
                                    onClick = {
                                        updateUnitPreservingMeasurement(u)
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Dimension Inputs Grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = lengthStr,
                        onValueChange = {
                            lengthStr = it
                            lengthError = null
                        },
                        label = { Text("Length (${currentUnit.symbol})") },
                        placeholder = { Text("Optional") },
                        isError = lengthError != null,
                        supportingText = lengthError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = widthStr,
                        onValueChange = {
                            widthStr = it
                            widthError = null
                        },
                        label = { Text("Width / Depth (${currentUnit.symbol})") },
                        placeholder = { Text("Optional") },
                        isError = widthError != null,
                        supportingText = widthError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = heightStr,
                        onValueChange = {
                            heightStr = it
                            heightError = null
                        },
                        label = { Text("Height (${currentUnit.symbol})") },
                        placeholder = { Text("Optional") },
                        isError = heightError != null,
                        supportingText = heightError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = thicknessStr,
                        onValueChange = {
                            thicknessStr = it
                            thicknessError = null
                        },
                        label = { Text("Thickness (${currentUnit.symbol})") },
                        placeholder = { Text("Optional") },
                        isError = thicknessError != null,
                        supportingText = thicknessError?.let { { Text(it) } },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes (Optional)
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    placeholder = { Text("e.g. Use 18 mm plywood, Keep rear clearance") },
                    maxLines = 3,
                    keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Sentences),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Cancel & Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(onClick = onDismiss) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Button(
                        onClick = {
                            // Validation
                            var hasError = false
                            if (name.isBlank()) {
                                nameError = "Enter a name."
                                hasError = true
                            }

                            val parsedL = DimensionFormatter.parseBigDecimal(lengthStr)
                            if (lengthStr.isNotBlank() && (parsedL == null || parsedL <= BigDecimal.ZERO)) {
                                lengthError = "Must be > 0"
                                hasError = true
                            }

                            val parsedW = DimensionFormatter.parseBigDecimal(widthStr)
                            if (widthStr.isNotBlank() && (parsedW == null || parsedW <= BigDecimal.ZERO)) {
                                widthError = "Must be > 0"
                                hasError = true
                            }

                            val parsedH = DimensionFormatter.parseBigDecimal(heightStr)
                            if (heightStr.isNotBlank() && (parsedH == null || parsedH <= BigDecimal.ZERO)) {
                                heightError = "Must be > 0"
                                hasError = true
                            }

                            val parsedT = DimensionFormatter.parseBigDecimal(thicknessStr)
                            if (thicknessStr.isNotBlank() && (parsedT == null || parsedT <= BigDecimal.ZERO)) {
                                thicknessError = "Must be > 0"
                                hasError = true
                            }

                            if (!hasError) {
                                onSave(
                                    initialDimension?.id,
                                    name,
                                    category,
                                    parsedL,
                                    parsedW,
                                    parsedH,
                                    parsedT,
                                    currentUnit,
                                    notes.ifBlank { null },
                                    isFavorite
                                )
                                onDismiss()
                            }
                        }
                    ) {
                        Text(if (isEditing) "Save Changes" else "Save Dimension")
                    }
                }
            }
        }
    }
}
