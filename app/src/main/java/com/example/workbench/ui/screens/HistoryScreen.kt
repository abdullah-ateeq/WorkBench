package com.example.workbench.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.calculator.UnitConverter
import com.example.workbench.model.DimensionCalculation
import com.example.workbench.model.WorkshopDimension
import com.example.workbench.ui.components.CalculationCard
import com.example.workbench.ui.components.DeleteConfirmationDialog
import com.example.workbench.ui.components.DimensionCard
import com.example.workbench.ui.components.DimensionDetailDialog
import com.example.workbench.ui.components.SearchBox
import com.example.workbench.viewmodel.HistoryFilter
import com.example.workbench.viewmodel.HistoryItem
import com.example.workbench.viewmodel.WorkBenchViewModel
import kotlinx.coroutines.launch

@Composable
fun HistoryScreen(
    viewModel: WorkBenchViewModel,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val historyItems by viewModel.combinedHistory.collectAsState()
    val filter by viewModel.historyFilter.collectAsState()
    val searchQuery by viewModel.historySearchQuery.collectAsState()
    val settings by viewModel.settings.collectAsState()

    var viewingDimension by remember { mutableStateOf<WorkshopDimension?>(null) }
    var pendingDeleteDimension by remember { mutableStateOf<WorkshopDimension?>(null) }
    var pendingDeleteCalculation by remember { mutableStateOf<DimensionCalculation?>(null) }
    var showClearAllDialog by remember { mutableStateOf(false) }

    fun copyCalculation(calc: DimensionCalculation) {
        val resultFormatted = DimensionFormatter.formatNumber(calc.result, settings.decimalPlaces)
        val text = "${calc.name}: $resultFormatted ${calc.unit}\n${calc.formula}"
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Calculation", text))

        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
        scope.launch {
            snackbarHostState.showSnackbar("Calculation copied")
        }
    }

    fun copyDimension(dimension: WorkshopDimension) {
        val unit = dimension.unit
        val places = settings.decimalPlaces
        val l = dimension.lengthMm?.let { UnitConverter.fromMillimeters(it, unit) }
        val w = dimension.widthMm?.let { UnitConverter.fromMillimeters(it, unit) }
        val h = dimension.heightMm?.let { UnitConverter.fromMillimeters(it, unit) }
        val t = dimension.thicknessMm?.let { UnitConverter.fromMillimeters(it, unit) }

        val sb = StringBuilder()
        sb.appendLine(dimension.name)
        if (l != null) sb.appendLine("Length: ${DimensionFormatter.formatMeasurement(l, unit, places)}")
        if (w != null) sb.appendLine("Depth: ${DimensionFormatter.formatMeasurement(w, unit, places)}")
        if (h != null) sb.appendLine("Height: ${DimensionFormatter.formatMeasurement(h, unit, places)}")
        if (t != null) sb.appendLine("Top Thickness: ${DimensionFormatter.formatMeasurement(t, unit, places)}")

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Dimension", sb.toString().trim()))

        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
        scope.launch {
            snackbarHostState.showSnackbar("Dimensions copied")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
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
                        text = "HISTORY",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Saved Dimensions & Calculations",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (historyItems.isNotEmpty()) {
                    IconButton(onClick = { showClearAllDialog = true }) {
                        Icon(
                            imageVector = Icons.Default.DeleteSweep,
                            contentDescription = "Clear all history",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }

            // Search
            SearchBox(
                query = searchQuery,
                onQueryChange = viewModel::setHistorySearchQuery,
                placeholder = "Search history..."
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Filter Chips: ALL, DIMENSIONS, CALCULATIONS
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = filter == HistoryFilter.ALL,
                    onClick = { viewModel.setHistoryFilter(HistoryFilter.ALL) },
                    label = { Text("ALL") }
                )
                FilterChip(
                    selected = filter == HistoryFilter.DIMENSIONS,
                    onClick = { viewModel.setHistoryFilter(HistoryFilter.DIMENSIONS) },
                    label = { Text("DIMENSIONS") }
                )
                FilterChip(
                    selected = filter == HistoryFilter.CALCULATIONS,
                    onClick = { viewModel.setHistoryFilter(HistoryFilter.CALCULATIONS) },
                    label = { Text("CALCULATIONS") }
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (historyItems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NO HISTORY",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "No records match your search."
                            else "Saved calculations and dimensions will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 80.dp)
                ) {
                    items(historyItems, key = { it.id }) { item ->
                        when (item) {
                            is HistoryItem.DimensionItem -> {
                                DimensionCard(
                                    dimension = item.dimension,
                                    settings = settings,
                                    onOpen = { viewingDimension = item.dimension },
                                    onEdit = { /* edited from dimensions */ },
                                    onDuplicate = { viewModel.duplicateDimension(item.dimension) },
                                    onDelete = {
                                        if (settings.confirmDelete) pendingDeleteDimension = item.dimension
                                        else viewModel.deleteDimension(item.dimension.id)
                                    },
                                    onCopy = { copyDimension(item.dimension) },
                                    onToggleFavorite = { viewModel.toggleFavorite(item.dimension.id) },
                                    onCalculate = {
                                        viewModel.prefillFromDimension(item.dimension)
                                        onNavigateToCalculator()
                                    }
                                )
                            }
                            is HistoryItem.CalculationItem -> {
                                CalculationCard(
                                    calculation = item.calculation,
                                    settings = settings,
                                    onCopy = { copyCalculation(item.calculation) },
                                    onDelete = {
                                        if (settings.confirmDelete) pendingDeleteCalculation = item.calculation
                                        else viewModel.deleteCalculation(item.calculation.id)
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // Detail dialog
    viewingDimension?.let { dim ->
        DimensionDetailDialog(
            dimension = dim,
            settings = settings,
            onDismiss = { viewingDimension = null },
            onEdit = { viewingDimension = null },
            onCalculate = {
                viewingDimension = null
                viewModel.prefillFromDimension(dim)
                onNavigateToCalculator()
            },
            onCopyAll = { copyDimension(dim) }
        )
    }

    // Delete single dimension
    pendingDeleteDimension?.let { dim ->
        DeleteConfirmationDialog(
            title = "Delete Dimension",
            message = "Are you sure you want to delete \"${dim.name}\"?",
            onConfirm = {
                viewModel.deleteDimension(dim.id)
                pendingDeleteDimension = null
            },
            onDismiss = { pendingDeleteDimension = null }
        )
    }

    // Delete single calculation
    pendingDeleteCalculation?.let { calc ->
        DeleteConfirmationDialog(
            title = "Delete Calculation",
            message = "Are you sure you want to delete \"${calc.name}\"?",
            onConfirm = {
                viewModel.deleteCalculation(calc.id)
                pendingDeleteCalculation = null
            },
            onDismiss = { pendingDeleteCalculation = null }
        )
    }

    // Clear all history dialog
    if (showClearAllDialog) {
        DeleteConfirmationDialog(
            title = "Clear All Calculations",
            message = "Are you sure you want to clear saved calculation history?",
            confirmButtonText = "Clear All",
            onConfirm = {
                viewModel.clearAllHistory()
                showClearAllDialog = false
            },
            onDismiss = { showClearAllDialog = false }
        )
    }
}
