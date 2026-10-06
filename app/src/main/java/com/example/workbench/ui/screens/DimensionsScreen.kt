package com.example.workbench.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
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
import com.example.workbench.model.WorkshopDimension
import com.example.workbench.ui.components.AddEditDimensionDialog
import com.example.workbench.ui.components.DeleteConfirmationDialog
import com.example.workbench.ui.components.DimensionCard
import com.example.workbench.ui.components.DimensionDetailDialog
import com.example.workbench.ui.components.SearchBox
import com.example.workbench.viewmodel.WorkBenchViewModel
import kotlinx.coroutines.launch

@Composable
fun DimensionsScreen(
    viewModel: WorkBenchViewModel,
    onNavigateToCalculator: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    val allDimensions by viewModel.dimensions.collectAsState()
    val filteredDimensions by viewModel.filteredDimensions.collectAsState()
    val favoriteDimensions by viewModel.favoriteDimensions.collectAsState()
    val recentDimensions by viewModel.recentDimensions.collectAsState()
    val settings by viewModel.settings.collectAsState()
    val searchQuery by viewModel.dimensionSearchQuery.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var editingDimension by remember { mutableStateOf<WorkshopDimension?>(null) }
    var viewingDimension by remember { mutableStateOf<WorkshopDimension?>(null) }
    var pendingDeleteDimension by remember { mutableStateOf<WorkshopDimension?>(null) }

    fun copyToClipboard(dimension: WorkshopDimension) {
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
        if (!dimension.notes.isNullOrBlank()) sb.appendLine("Notes: ${dimension.notes}")

        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Workshop Dimension", sb.toString().trim())
        clipboard.setPrimaryClip(clip)

        if (settings.hapticFeedback) {
            haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
        }
        scope.launch {
            snackbarHostState.showSnackbar("Dimensions copied")
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    if (settings.hapticFeedback) {
                        haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    }
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Dimension")
            }
        }
    ) { innerPadding ->
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val isTabletOrLandscape = maxWidth > 720.dp

            if (allDimensions.isEmpty()) {
                // Empty State
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
                            imageVector = Icons.Default.SquareFoot,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "NO SAVED DIMENSIONS",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Save frequently used workshop measurements for quick reference.",
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        Spacer(modifier = Modifier.height(24.dp))
                        Button(
                            onClick = {
                                if (settings.hapticFeedback) {
                                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                                }
                                showAddDialog = true
                            }
                        ) {
                            Icon(imageVector = Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("+ ADD DIMENSION")
                        }
                    }
                }
            } else if (isTabletOrLandscape) {
                // Responsive Two-Column Tablet / Landscape layout
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Left list column
                    Column(modifier = Modifier.weight(1f)) {
                        DimensionsHeaderAndSearch(
                            query = searchQuery,
                            onQueryChange = viewModel::setDimensionSearchQuery
                        )
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 80.dp)
                        ) {
                            renderDimensionSections(
                                searchActive = searchQuery.isNotBlank(),
                                favorites = favoriteDimensions,
                                recents = recentDimensions,
                                all = filteredDimensions,
                                settings = settings,
                                onOpen = {
                                    viewModel.recordRecent(it.id)
                                    viewingDimension = it
                                },
                                onEdit = { editingDimension = it },
                                onDuplicate = { viewModel.duplicateDimension(it) },
                                onDelete = {
                                    if (settings.confirmDelete) pendingDeleteDimension = it
                                    else viewModel.deleteDimension(it.id)
                                },
                                onCopy = { copyToClipboard(it) },
                                onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                                onCalculate = {
                                    viewModel.prefillFromDimension(it)
                                    onNavigateToCalculator()
                                }
                            )
                        }
                    }

                    // Right detail / preview column
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .padding(top = 8.dp)
                    ) {
                        val activeDim = viewingDimension ?: filteredDimensions.firstOrNull()
                        if (activeDim != null) {
                            OutlinedCard(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.outlinedCardColors(containerColor = MaterialTheme.colorScheme.surface)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = "SELECTED SPECIFICATION",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = activeDim.name,
                                        style = MaterialTheme.typography.titleLarge,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Spacer(modifier = Modifier.height(12.dp))
                                    DimensionCard(
                                        dimension = activeDim,
                                        settings = settings,
                                        onOpen = { viewingDimension = activeDim },
                                        onEdit = { editingDimension = activeDim },
                                        onDuplicate = { viewModel.duplicateDimension(activeDim) },
                                        onDelete = {
                                            if (settings.confirmDelete) pendingDeleteDimension = activeDim
                                            else viewModel.deleteDimension(activeDim.id)
                                        },
                                        onCopy = { copyToClipboard(activeDim) },
                                        onToggleFavorite = { viewModel.toggleFavorite(activeDim.id) },
                                        onCalculate = {
                                            viewModel.prefillFromDimension(activeDim)
                                            onNavigateToCalculator()
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Phone Single-Column Portrait
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp)
                ) {
                    DimensionsHeaderAndSearch(
                        query = searchQuery,
                        onQueryChange = viewModel::setDimensionSearchQuery
                    )

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 80.dp)
                    ) {
                        renderDimensionSections(
                            searchActive = searchQuery.isNotBlank(),
                            favorites = favoriteDimensions,
                            recents = recentDimensions,
                            all = filteredDimensions,
                            settings = settings,
                            onOpen = {
                                viewModel.recordRecent(it.id)
                                viewingDimension = it
                            },
                            onEdit = { editingDimension = it },
                            onDuplicate = { viewModel.duplicateDimension(it) },
                            onDelete = {
                                if (settings.confirmDelete) pendingDeleteDimension = it
                                else viewModel.deleteDimension(it.id)
                            },
                            onCopy = { copyToClipboard(it) },
                            onToggleFavorite = { viewModel.toggleFavorite(it.id) },
                            onCalculate = {
                                viewModel.prefillFromDimension(it)
                                onNavigateToCalculator()
                            }
                        )
                    }
                }
            }
        }
    }

    // Add / Edit Dialog
    if (showAddDialog || editingDimension != null) {
        AddEditDimensionDialog(
            initialDimension = editingDimension,
            defaultUnit = settings.defaultUnit,
            onDismiss = {
                showAddDialog = false
                editingDimension = null
            },
            onSave = { id, name, cat, l, w, h, t, unit, notes, fav ->
                viewModel.saveDimension(id, name, cat, l, w, h, t, unit, notes, fav)
                showAddDialog = false
                editingDimension = null
            }
        )
    }

    // Detail Dialog
    viewingDimension?.let { dim ->
        DimensionDetailDialog(
            dimension = dim,
            settings = settings,
            onDismiss = { viewingDimension = null },
            onEdit = {
                viewingDimension = null
                editingDimension = dim
            },
            onCalculate = {
                viewingDimension = null
                viewModel.prefillFromDimension(dim)
                onNavigateToCalculator()
            },
            onCopyAll = {
                copyToClipboard(dim)
            }
        )
    }

    // Delete confirmation dialog
    pendingDeleteDimension?.let { dim ->
        DeleteConfirmationDialog(
            title = "Delete Dimension",
            message = "Are you sure you want to delete \"${dim.name}\"? This action cannot be undone.",
            onConfirm = {
                viewModel.deleteDimension(dim.id)
                pendingDeleteDimension = null
            },
            onDismiss = { pendingDeleteDimension = null }
        )
    }
}

@Composable
private fun DimensionsHeaderAndSearch(
    query: String,
    onQueryChange: (String) -> Unit
) {
    Column(modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "WORKBENCH",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Saved Workshop Dimensions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        SearchBox(query = query, onQueryChange = onQueryChange)
    }
}

private fun androidx.compose.foundation.lazy.LazyListScope.renderDimensionSections(
    searchActive: Boolean,
    favorites: List<WorkshopDimension>,
    recents: List<WorkshopDimension>,
    all: List<WorkshopDimension>,
    settings: com.example.workbench.model.AppSettings,
    onOpen: (WorkshopDimension) -> Unit,
    onEdit: (WorkshopDimension) -> Unit,
    onDuplicate: (WorkshopDimension) -> Unit,
    onDelete: (WorkshopDimension) -> Unit,
    onCopy: (WorkshopDimension) -> Unit,
    onToggleFavorite: (WorkshopDimension) -> Unit,
    onCalculate: (WorkshopDimension) -> Unit
) {
    // 1. Favorites section (only show if favorites exist and not searching or search matches favorites)
    if (favorites.isNotEmpty() && !searchActive) {
        item {
            Text(
                text = "FAVORITES",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
            )
        }
        items(favorites, key = { "fav_${it.id}" }) { item ->
            DimensionCard(
                dimension = item,
                settings = settings,
                onOpen = { onOpen(item) },
                onEdit = { onEdit(item) },
                onDuplicate = { onDuplicate(item) },
                onDelete = { onDelete(item) },
                onCopy = { onCopy(item) },
                onToggleFavorite = { onToggleFavorite(item) },
                onCalculate = { onCalculate(item) }
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }

    // 2. All Dimensions section
    item {
        Text(
            text = if (searchActive) "SEARCH RESULTS (${all.size})" else "ALL DIMENSIONS (${all.size})",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
        )
    }

    if (all.isEmpty() && searchActive) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Text(
                    text = "No dimensions match your search.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        items(all, key = { "all_${it.id}" }) { item ->
            DimensionCard(
                dimension = item,
                settings = settings,
                onOpen = { onOpen(item) },
                onEdit = { onEdit(item) },
                onDuplicate = { onDuplicate(item) },
                onDelete = { onDelete(item) },
                onCopy = { onCopy(item) },
                onToggleFavorite = { onToggleFavorite(item) },
                onCalculate = { onCalculate(item) }
            )
        }
    }
}
