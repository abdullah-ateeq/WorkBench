package com.example.workbench.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.model.CalculationType
import com.example.workbench.model.DimensionUnit
import com.example.workbench.ui.components.CalculatorDiagram
import com.example.workbench.viewmodel.WorkBenchViewModel
import kotlinx.coroutines.launch

@Composable
fun CalculatorScreen(
    viewModel: WorkBenchViewModel,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val mode by viewModel.calculatorMode.collectAsState()
    val unit by viewModel.calcUnit.collectAsState()
    val settings by viewModel.settings.collectAsState()

    val addSubStart by viewModel.addSubStart.collectAsState()
    val addSubChange by viewModel.addSubChange.collectAsState()
    val addSubIsAdd by viewModel.addSubIsAdd.collectAsState()

    val halfDoubleValue by viewModel.halfDoubleValue.collectAsState()
    val halfDoubleIsHalf by viewModel.halfDoubleIsHalf.collectAsState()

    val pieceSize by viewModel.pieceSize.collectAsState()
    val pieceQuantity by viewModel.pieceQuantity.collectAsState()
    val pieceGap by viewModel.pieceGap.collectAsState()

    val centerAvailable by viewModel.centerAvailable.collectAsState()
    val centerObject by viewModel.centerObject.collectAsState()

    val result by viewModel.calcResult.collectAsState()
    val secondaryResult by viewModel.calcSecondaryResult.collectAsState()
    val formula by viewModel.calcFormula.collectAsState()
    val error by viewModel.calcError.collectAsState()

    var showFormulaBreakdown by remember { mutableStateOf(true) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var saveNameInput by remember { mutableStateOf("") }
    var saveNotesInput by remember { mutableStateOf("") }
    var unitMenuExpanded by remember { mutableStateOf(false) }

    fun copyResultToClipboard() {
        if (result == null && secondaryResult == null) return
        val places = settings.decimalPlaces
        val textToCopy = if (mode == CalculationType.CENTER_POSITION && secondaryResult != null) {
            val l = DimensionFormatter.formatMeasurement(result, unit, places)
            val r = DimensionFormatter.formatMeasurement(secondaryResult, unit, places)
            "Centered Position\nLeft: $l\nRight: $r\nFormula: ${formula ?: ""}"
        } else {
            val res = DimensionFormatter.formatMeasurement(result, unit, places)
            "${mode.displayName}: $res\nFormula: ${formula ?: ""}"
        }

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Calculation Result", textToCopy)
        clipboard.setPrimaryClip(clip)

        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
        scope.launch {
            snackbarHostState.showSnackbar("Calculation copied")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrLandscape = maxWidth > 720.dp

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp, bottom = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "CALCULATOR",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Workshop Dimension Arithmetic",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    // Unit selector chip
                    Box {
                        OutlinedButton(
                            onClick = { unitMenuExpanded = true }
                        ) {
                            Text(
                                text = "Unit: ${unit.symbol}",
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        DropdownMenu(
                            expanded = unitMenuExpanded,
                            onDismissRequest = { unitMenuExpanded = false }
                        ) {
                            DimensionUnit.entries.forEach { u ->
                                DropdownMenuItem(
                                    text = { Text("${u.displayName} (${u.symbol})") },
                                    onClick = {
                                        viewModel.setCalcUnit(u)
                                        unitMenuExpanded = false
                                    }
                                )
                            }
                        }
                    }
                }

                // Mode Tabs
                val modes = listOf(
                    CalculationType.ADD to "Add / Sub",
                    CalculationType.HALF to "Half / Double",
                    CalculationType.TOTAL_FROM_PIECES to "Pieces Total",
                    CalculationType.CENTER_POSITION to "Center"
                )

                val selectedTabIndex = when (mode) {
                    CalculationType.ADD, CalculationType.SUBTRACT -> 0
                    CalculationType.HALF, CalculationType.DOUBLE -> 1
                    CalculationType.TOTAL_FROM_PIECES -> 2
                    CalculationType.CENTER_POSITION -> 3
                }

                androidx.compose.material3.PrimaryTabRow(
                    selectedTabIndex = selectedTabIndex,
                    modifier = Modifier.padding(bottom = 12.dp)
                ) {
                    modes.forEachIndexed { index, (m, label) ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = {
                                if (index == 0) {
                                    viewModel.setAddSubIsAdd(addSubIsAdd)
                                } else if (index == 1) {
                                    viewModel.setHalfDoubleIsHalf(halfDoubleIsHalf)
                                } else {
                                    viewModel.setCalculatorMode(m)
                                }
                            },
                            text = {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        )
                    }
                }

                if (isTabletOrLandscape) {
                    // Two columns on Tablet
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Left: Inputs
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            CalculatorInputControls(
                                mode = mode,
                                unit = unit,
                                addSubStart = addSubStart,
                                addSubChange = addSubChange,
                                addSubIsAdd = addSubIsAdd,
                                onAddSubStartChange = viewModel::setAddSubStart,
                                onAddSubChangeChange = viewModel::setAddSubChange,
                                onAddSubIsAddChange = viewModel::setAddSubIsAdd,
                                halfDoubleValue = halfDoubleValue,
                                halfDoubleIsHalf = halfDoubleIsHalf,
                                onHalfDoubleValueChange = viewModel::setHalfDoubleValue,
                                onHalfDoubleIsHalfChange = viewModel::setHalfDoubleIsHalf,
                                pieceSize = pieceSize,
                                pieceQuantity = pieceQuantity,
                                pieceGap = pieceGap,
                                onPieceSizeChange = viewModel::setPieceSize,
                                onPieceQuantityChange = viewModel::setPieceQuantity,
                                onPieceGapChange = viewModel::setPieceGap,
                                centerAvailable = centerAvailable,
                                centerObject = centerObject,
                                onCenterAvailableChange = viewModel::setCenterAvailable,
                                onCenterObjectChange = viewModel::setCenterObject,
                                onReset = viewModel::resetCalculator
                            )
                        }

                        // Right: Results & Diagram
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState())
                        ) {
                            CalculatorResultDisplay(
                                mode = mode,
                                result = result,
                                secondaryResult = secondaryResult,
                                error = error,
                                formula = formula,
                                unit = unit,
                                settings = settings,
                                showBreakdown = showFormulaBreakdown,
                                onToggleBreakdown = { showFormulaBreakdown = !showFormulaBreakdown },
                                onCopy = { copyResultToClipboard() },
                                onSave = {
                                    saveNameInput = ""
                                    saveNotesInput = ""
                                    showSaveDialog = true
                                },
                                onReset = viewModel::resetCalculator,
                                pieceSize = pieceSize,
                                pieceQty = pieceQuantity,
                                pieceGap = pieceGap,
                                centerAvail = centerAvailable,
                                centerObj = centerObject,
                                addSubStart = addSubStart,
                                addSubChange = addSubChange,
                                halfDoubleVal = halfDoubleValue
                            )
                        }
                    }
                } else {
                    // Single Column on Phone
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 80.dp)
                    ) {
                        CalculatorInputControls(
                            mode = mode,
                            unit = unit,
                            addSubStart = addSubStart,
                            addSubChange = addSubChange,
                            addSubIsAdd = addSubIsAdd,
                            onAddSubStartChange = viewModel::setAddSubStart,
                            onAddSubChangeChange = viewModel::setAddSubChange,
                            onAddSubIsAddChange = viewModel::setAddSubIsAdd,
                            halfDoubleValue = halfDoubleValue,
                            halfDoubleIsHalf = halfDoubleIsHalf,
                            onHalfDoubleValueChange = viewModel::setHalfDoubleValue,
                            onHalfDoubleIsHalfChange = viewModel::setHalfDoubleIsHalf,
                            pieceSize = pieceSize,
                            pieceQuantity = pieceQuantity,
                            pieceGap = pieceGap,
                            onPieceSizeChange = viewModel::setPieceSize,
                            onPieceQuantityChange = viewModel::setPieceQuantity,
                            onPieceGapChange = viewModel::setPieceGap,
                            centerAvailable = centerAvailable,
                            centerObject = centerObject,
                            onCenterAvailableChange = viewModel::setCenterAvailable,
                            onCenterObjectChange = viewModel::setCenterObject,
                            onReset = viewModel::resetCalculator
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        CalculatorResultDisplay(
                            mode = mode,
                            result = result,
                            secondaryResult = secondaryResult,
                            error = error,
                            formula = formula,
                            unit = unit,
                            settings = settings,
                            showBreakdown = showFormulaBreakdown,
                            onToggleBreakdown = { showFormulaBreakdown = !showFormulaBreakdown },
                            onCopy = { copyResultToClipboard() },
                            onSave = {
                                saveNameInput = ""
                                saveNotesInput = ""
                                showSaveDialog = true
                            },
                            onReset = viewModel::resetCalculator,
                            pieceSize = pieceSize,
                            pieceQty = pieceQuantity,
                            pieceGap = pieceGap,
                            centerAvail = centerAvailable,
                            centerObj = centerObject,
                            addSubStart = addSubStart,
                            addSubChange = addSubChange,
                            halfDoubleVal = halfDoubleValue
                        )
                    }
                }
            }
        }
    }

    // Save calculation dialog
    if (showSaveDialog && result != null) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("Save Calculation") },
            text = {
                Column {
                    Text(
                        text = "Result: ${DimensionFormatter.formatMeasurement(result, unit, settings.decimalPlaces)}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = saveNameInput,
                        onValueChange = { saveNameInput = it },
                        label = { Text("Record Name") },
                        placeholder = { Text("e.g. Total shelf run") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = saveNotesInput,
                        onValueChange = { saveNotesInput = it },
                        label = { Text("Notes (Optional)") },
                        placeholder = { Text("e.g. Cut 4 slats at 30 cm") },
                        maxLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.saveCurrentCalculation(
                            customName = saveNameInput.ifBlank { null },
                            notes = saveNotesInput.ifBlank { null }
                        )
                        showSaveDialog = false
                        if (settings.hapticFeedback) {
                            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar("Calculation saved to History")
                        }
                    }
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun CalculatorInputControls(
    mode: CalculationType,
    unit: DimensionUnit,
    addSubStart: String,
    addSubChange: String,
    addSubIsAdd: Boolean,
    onAddSubStartChange: (String) -> Unit,
    onAddSubChangeChange: (String) -> Unit,
    onAddSubIsAddChange: (Boolean) -> Unit,
    halfDoubleValue: String,
    halfDoubleIsHalf: Boolean,
    onHalfDoubleValueChange: (String) -> Unit,
    onHalfDoubleIsHalfChange: (Boolean) -> Unit,
    pieceSize: String,
    pieceQuantity: String,
    pieceGap: String,
    onPieceSizeChange: (String) -> Unit,
    onPieceQuantityChange: (String) -> Unit,
    onPieceGapChange: (String) -> Unit,
    centerAvailable: String,
    centerObject: String,
    onCenterAvailableChange: (String) -> Unit,
    onCenterObjectChange: (String) -> Unit,
    onReset: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            when (mode) {
                CalculationType.ADD, CalculationType.SUBTRACT -> {
                    // Segmented selector for Add / Subtract
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = addSubIsAdd,
                            onClick = { onAddSubIsAddChange(true) },
                            label = { Text("ADD (+)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !addSubIsAdd,
                            onClick = { onAddSubIsAddChange(false) },
                            label = { Text("SUBTRACT (−)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = addSubStart,
                        onValueChange = onAddSubStartChange,
                        label = { Text("Start Dimension (${unit.symbol})") },
                        placeholder = { Text("e.g. 120") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = addSubChange,
                        onValueChange = onAddSubChangeChange,
                        label = { Text(if (addSubIsAdd) "Dimension to Add (${unit.symbol})" else "Dimension to Subtract (${unit.symbol})") },
                        placeholder = { Text("e.g. 5") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                CalculationType.HALF, CalculationType.DOUBLE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = halfDoubleIsHalf,
                            onClick = { onHalfDoubleIsHalfChange(true) },
                            label = { Text("HALF (÷ 2)") },
                            modifier = Modifier.weight(1f)
                        )
                        FilterChip(
                            selected = !halfDoubleIsHalf,
                            onClick = { onHalfDoubleIsHalfChange(false) },
                            label = { Text("DOUBLE (× 2)") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = halfDoubleValue,
                        onValueChange = onHalfDoubleValueChange,
                        label = { Text("Dimension (${unit.symbol})") },
                        placeholder = { Text("e.g. 120") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                CalculationType.TOTAL_FROM_PIECES -> {
                    Text(
                        text = "Calculate total dimension from repeated pieces.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = pieceSize,
                        onValueChange = onPieceSizeChange,
                        label = { Text("Piece Size (${unit.symbol})") },
                        placeholder = { Text("e.g. 30") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = pieceQuantity,
                            onValueChange = onPieceQuantityChange,
                            label = { Text("Quantity") },
                            placeholder = { Text("e.g. 4") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f)
                        )

                        OutlinedTextField(
                            value = pieceGap,
                            onValueChange = onPieceGapChange,
                            label = { Text("Optional Gap (${unit.symbol})") },
                            placeholder = { Text("e.g. 2") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                CalculationType.CENTER_POSITION -> {
                    Text(
                        text = "Calculate centered position of an object inside available width.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = centerAvailable,
                        onValueChange = onCenterAvailableChange,
                        label = { Text("Available Width (${unit.symbol})") },
                        placeholder = { Text("e.g. 120") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = centerObject,
                        onValueChange = onCenterObjectChange,
                        label = { Text("Object Width (${unit.symbol})") },
                        placeholder = { Text("e.g. 80") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onReset) {
                    Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset")
                }
            }
        }
    }
}

@Composable
private fun CalculatorResultDisplay(
    mode: CalculationType,
    result: java.math.BigDecimal?,
    secondaryResult: java.math.BigDecimal?,
    error: String?,
    formula: String?,
    unit: DimensionUnit,
    settings: com.example.workbench.model.AppSettings,
    showBreakdown: Boolean,
    onToggleBreakdown: () -> Unit,
    onCopy: () -> Unit,
    onSave: () -> Unit,
    onReset: () -> Unit,
    pieceSize: String,
    pieceQty: String,
    pieceGap: String,
    centerAvail: String,
    centerObj: String,
    addSubStart: String,
    addSubChange: String,
    halfDoubleVal: String
) {
    val places = settings.decimalPlaces

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = if (error != null) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
            else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            if (error != null) {
                Text(
                    text = "VALIDATION ERROR",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = error,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (result != null) {
                // Result header
                val resultTitle = when (mode) {
                    CalculationType.ADD -> "SUM TOTAL"
                    CalculationType.SUBTRACT -> "REDUCED DIMENSION"
                    CalculationType.HALF -> "HALF DIMENSION"
                    CalculationType.DOUBLE -> "DOUBLED DIMENSION"
                    CalculationType.TOTAL_FROM_PIECES -> "TOTAL RUN"
                    CalculationType.CENTER_POSITION -> "CENTERED OBJECT SPACING"
                }

                Text(
                    text = resultTitle,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (mode == CalculationType.CENTER_POSITION && secondaryResult != null) {
                    val leftStr = DimensionFormatter.formatMeasurement(result, unit, places)
                    val rightStr = DimensionFormatter.formatMeasurement(secondaryResult, unit, places)
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Left Space", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = leftStr,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Column {
                                Text("Right Space", style = MaterialTheme.typography.labelMedium)
                                Text(
                                    text = rightStr,
                                    style = MaterialTheme.typography.headlineLarge,
                                    fontWeight = FontWeight.Bold,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                } else {
                    Text(
                        text = DimensionFormatter.formatMeasurement(result, unit, places),
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Diagram if enabled
                if (settings.showDimensionDiagram) {
                    Spacer(modifier = Modifier.height(12.dp))
                    val v1 = DimensionFormatter.parseBigDecimal(
                        when (mode) {
                            CalculationType.ADD, CalculationType.SUBTRACT -> addSubStart
                            CalculationType.HALF, CalculationType.DOUBLE -> halfDoubleVal
                            CalculationType.TOTAL_FROM_PIECES -> pieceSize
                            CalculationType.CENTER_POSITION -> centerAvail
                        }
                    )
                    val v2 = DimensionFormatter.parseBigDecimal(
                        when (mode) {
                            CalculationType.ADD, CalculationType.SUBTRACT -> addSubChange
                            CalculationType.CENTER_POSITION -> centerObj
                            else -> null
                        }
                    )
                    val v3 = DimensionFormatter.parseBigDecimal(pieceGap)
                    val qtyInt = pieceQty.toIntOrNull()

                    CalculatorDiagram(
                        mode = mode,
                        val1 = v1,
                        val2 = v2,
                        val3 = v3,
                        qty = qtyInt,
                        result = result,
                        unit = unit,
                        decimalPlaces = places
                    )
                }

                // Collapsible Formula Breakdown
                if (settings.showCalculationFormula && !formula.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedCard(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "HOW IT WAS CALCULATED",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                IconButton(onClick = onToggleBreakdown) {
                                    Icon(
                                        imageVector = if (showBreakdown) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                        contentDescription = "Toggle calculation breakdown"
                                    )
                                }
                            }
                            AnimatedVisibility(visible = showBreakdown) {
                                Text(
                                    text = formula,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontFamily = FontFamily.Monospace,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons: Copy, Save
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Button(
                        onClick = onSave,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.Bookmark, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save")
                    }

                    OutlinedButton(
                        onClick = onCopy,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(imageVector = Icons.Default.ContentCopy, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Copy")
                    }
                }
            } else {
                // Empty state waiting for inputs
                Text(
                    text = "ENTER MEASUREMENTS",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.outline
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Enter dimensions above to see the calculated result and dynamic workshop diagram.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
