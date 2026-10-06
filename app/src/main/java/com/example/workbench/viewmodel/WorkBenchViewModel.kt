package com.example.workbench.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.workbench.calculator.DimensionFormatter
import com.example.workbench.calculator.UnitConverter
import com.example.workbench.calculator.WorkBenchCalculator
import com.example.workbench.data.WorkBenchPreferencesRepository
import com.example.workbench.model.AppSettings
import com.example.workbench.model.CalculationType
import com.example.workbench.model.CenterResult
import com.example.workbench.model.DimensionCalculation
import com.example.workbench.model.DimensionCategory
import com.example.workbench.model.DimensionUnit
import com.example.workbench.model.WorkshopDimension
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.util.UUID

enum class HistoryFilter {
    ALL,
    DIMENSIONS,
    CALCULATIONS
}

sealed class HistoryItem {
    abstract val id: String
    abstract val timestamp: Long

    data class DimensionItem(val dimension: WorkshopDimension) : HistoryItem() {
        override val id: String get() = dimension.id
        override val timestamp: Long get() = dimension.updatedAt
    }

    data class CalculationItem(val calculation: DimensionCalculation) : HistoryItem() {
        override val id: String get() = calculation.id
        override val timestamp: Long get() = calculation.createdAt
    }
}

class WorkBenchViewModel(
    private val repository: WorkBenchPreferencesRepository
) : ViewModel() {

    // --- State from DataStore ---
    val dimensions: StateFlow<List<WorkshopDimension>> = repository.dimensionsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val calculations: StateFlow<List<DimensionCalculation>> = repository.calculationsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDimensionIds: StateFlow<List<String>> = repository.recentIdsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val settings: StateFlow<AppSettings> = repository.settingsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), AppSettings())

    // --- Dimension Screen UI State ---
    private val _dimensionSearchQuery = MutableStateFlow("")
    val dimensionSearchQuery: StateFlow<String> = _dimensionSearchQuery.asStateFlow()

    fun setDimensionSearchQuery(query: String) {
        _dimensionSearchQuery.value = query
    }

    // Filtered dimensions with sorting: Favorites first, then recently modified
    val filteredDimensions: StateFlow<List<WorkshopDimension>> = combine(
        dimensions,
        _dimensionSearchQuery
    ) { list, query ->
        val filtered = if (query.isBlank()) {
            list
        } else {
            val q = query.trim().lowercase()
            list.filter { item ->
                item.name.lowercase().contains(q) ||
                    item.category.displayName.lowercase().contains(q) ||
                    (item.notes?.lowercase()?.contains(q) == true)
            }
        }
        filtered.sortedWith(
            compareByDescending<WorkshopDimension> { it.isFavorite }
                .thenByDescending { it.updatedAt }
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val favoriteDimensions: StateFlow<List<WorkshopDimension>> = filteredDimensions
        .map { list -> list.filter { it.isFavorite } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val recentDimensions: StateFlow<List<WorkshopDimension>> = combine(
        dimensions,
        recentDimensionIds,
        _dimensionSearchQuery
    ) { allDims, recentIds, query ->
        if (recentIds.isEmpty()) emptyList()
        else {
            val map = allDims.associateBy { it.id }
            val q = query.trim().lowercase()
            recentIds.mapNotNull { map[it] }
                .filter { item ->
                    if (q.isBlank()) true
                    else item.name.lowercase().contains(q) ||
                        item.category.displayName.lowercase().contains(q) ||
                        (item.notes?.lowercase()?.contains(q) == true)
                }
                .take(WorkBenchPreferencesRepository.MAX_RECENTS)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // --- Calculator State ---
    private val _calculatorMode = MutableStateFlow(CalculationType.ADD)
    val calculatorMode: StateFlow<CalculationType> = _calculatorMode.asStateFlow()

    private val _calcUnit = MutableStateFlow(DimensionUnit.CM)
    val calcUnit: StateFlow<DimensionUnit> = _calcUnit.asStateFlow()

    // Mode 1: Add / Subtract inputs
    private val _addSubStart = MutableStateFlow("")
    val addSubStart: StateFlow<String> = _addSubStart.asStateFlow()

    private val _addSubChange = MutableStateFlow("")
    val addSubChange: StateFlow<String> = _addSubChange.asStateFlow()

    private val _addSubIsAdd = MutableStateFlow(true)
    val addSubIsAdd: StateFlow<Boolean> = _addSubIsAdd.asStateFlow()

    // Mode 2: Half / Double inputs
    private val _halfDoubleValue = MutableStateFlow("")
    val halfDoubleValue: StateFlow<String> = _halfDoubleValue.asStateFlow()

    private val _halfDoubleIsHalf = MutableStateFlow(true)
    val halfDoubleIsHalf: StateFlow<Boolean> = _halfDoubleIsHalf.asStateFlow()

    // Mode 3: Total from Pieces inputs
    private val _pieceSize = MutableStateFlow("")
    val pieceSize: StateFlow<String> = _pieceSize.asStateFlow()

    private val _pieceQuantity = MutableStateFlow("4")
    val pieceQuantity: StateFlow<String> = _pieceQuantity.asStateFlow()

    private val _pieceGap = MutableStateFlow("2")
    val pieceGap: StateFlow<String> = _pieceGap.asStateFlow()

    // Mode 4: Center Position inputs
    private val _centerAvailable = MutableStateFlow("")
    val centerAvailable: StateFlow<String> = _centerAvailable.asStateFlow()

    private val _centerObject = MutableStateFlow("")
    val centerObject: StateFlow<String> = _centerObject.asStateFlow()

    // Calculator output state
    private val _calcResult = MutableStateFlow<BigDecimal?>(null)
    val calcResult: StateFlow<BigDecimal?> = _calcResult.asStateFlow()

    private val _calcSecondaryResult = MutableStateFlow<BigDecimal?>(null)
    val calcSecondaryResult: StateFlow<BigDecimal?> = _calcSecondaryResult.asStateFlow()

    private val _calcFormula = MutableStateFlow<String?>(null)
    val calcFormula: StateFlow<String?> = _calcFormula.asStateFlow()

    private val _calcError = MutableStateFlow<String?>(null)
    val calcError: StateFlow<String?> = _calcError.asStateFlow()

    // --- History State ---
    private val _historyFilter = MutableStateFlow(HistoryFilter.ALL)
    val historyFilter: StateFlow<HistoryFilter> = _historyFilter.asStateFlow()

    private val _historySearchQuery = MutableStateFlow("")
    val historySearchQuery: StateFlow<String> = _historySearchQuery.asStateFlow()

    val combinedHistory: StateFlow<List<HistoryItem>> = combine(
        dimensions,
        calculations,
        _historyFilter,
        _historySearchQuery
    ) { dims, calcs, filter, query ->
        val q = query.trim().lowercase()
        val dimItems = if (filter == HistoryFilter.CALCULATIONS) emptyList() else {
            dims.filter { item ->
                if (q.isBlank()) true
                else item.name.lowercase().contains(q) ||
                    item.category.displayName.lowercase().contains(q) ||
                    (item.notes?.lowercase()?.contains(q) == true)
            }.map { HistoryItem.DimensionItem(it) }
        }

        val calcItems = if (filter == HistoryFilter.DIMENSIONS) emptyList() else {
            calcs.filter { item ->
                if (q.isBlank()) true
                else item.name.lowercase().contains(q) ||
                    item.calculationType.displayName.lowercase().contains(q) ||
                    item.formula.lowercase().contains(q) ||
                    (item.notes?.lowercase()?.contains(q) == true)
            }.map { HistoryItem.CalculationItem(it) }
        }

        (dimItems + calcItems).sortedByDescending { it.timestamp }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Run initial calculation when inputs change or on load
        recalculate()
    }

    // --- Calculator Methods ---
    fun setCalculatorMode(mode: CalculationType) {
        _calculatorMode.value = mode
        recalculate()
    }

    fun setCalcUnit(unit: DimensionUnit) {
        _calcUnit.value = unit
    }

    fun setAddSubStart(value: String) {
        _addSubStart.value = value
        recalculate()
    }

    fun setAddSubChange(value: String) {
        _addSubChange.value = value
        recalculate()
    }

    fun setAddSubIsAdd(isAdd: Boolean) {
        _addSubIsAdd.value = isAdd
        _calculatorMode.value = if (isAdd) CalculationType.ADD else CalculationType.SUBTRACT
        recalculate()
    }

    fun setHalfDoubleValue(value: String) {
        _halfDoubleValue.value = value
        recalculate()
    }

    fun setHalfDoubleIsHalf(isHalf: Boolean) {
        _halfDoubleIsHalf.value = isHalf
        _calculatorMode.value = if (isHalf) CalculationType.HALF else CalculationType.DOUBLE
        recalculate()
    }

    fun setPieceSize(value: String) {
        _pieceSize.value = value
        recalculate()
    }

    fun setPieceQuantity(value: String) {
        _pieceQuantity.value = value
        recalculate()
    }

    fun setPieceGap(value: String) {
        _pieceGap.value = value
        recalculate()
    }

    fun setCenterAvailable(value: String) {
        _centerAvailable.value = value
        recalculate()
    }

    fun setCenterObject(value: String) {
        _centerObject.value = value
        recalculate()
    }

    fun resetCalculator() {
        when (_calculatorMode.value) {
            CalculationType.ADD, CalculationType.SUBTRACT -> {
                _addSubStart.value = ""
                _addSubChange.value = ""
            }
            CalculationType.HALF, CalculationType.DOUBLE -> {
                _halfDoubleValue.value = ""
            }
            CalculationType.TOTAL_FROM_PIECES -> {
                _pieceSize.value = ""
                _pieceQuantity.value = "4"
                _pieceGap.value = "2"
            }
            CalculationType.CENTER_POSITION -> {
                _centerAvailable.value = ""
                _centerObject.value = ""
            }
        }
        _calcResult.value = null
        _calcSecondaryResult.value = null
        _calcFormula.value = null
        _calcError.value = null
    }

    fun prefillFromDimension(dimension: WorkshopDimension) {
        val targetUnit = dimension.unit
        _calcUnit.value = targetUnit
        val l = dimension.lengthMm?.let { UnitConverter.fromMillimeters(it, targetUnit) }
        val w = dimension.widthMm?.let { UnitConverter.fromMillimeters(it, targetUnit) }

        // Prefill depending on available dimensions
        if (l != null && w != null) {
            _calculatorMode.value = CalculationType.CENTER_POSITION
            _centerAvailable.value = l.toPlainString()
            _centerObject.value = w.toPlainString()
        } else if (l != null) {
            _calculatorMode.value = CalculationType.HALF
            _halfDoubleIsHalf.value = true
            _halfDoubleValue.value = l.toPlainString()
        }
        recalculate()
    }

    private fun recalculate() {
        _calcError.value = null
        _calcSecondaryResult.value = null
        val unit = _calcUnit.value

        when (_calculatorMode.value) {
            CalculationType.ADD -> {
                val start = DimensionFormatter.parseBigDecimal(_addSubStart.value)
                val change = DimensionFormatter.parseBigDecimal(_addSubChange.value)
                if (start != null && change != null) {
                    val result = WorkBenchCalculator.addDimensions(start, change)
                    _calcResult.value = result
                    _calcFormula.value = "$start ${unit.symbol} + $change ${unit.symbol} = $result ${unit.symbol}"
                } else {
                    _calcResult.value = null
                    _calcFormula.value = null
                }
            }

            CalculationType.SUBTRACT -> {
                val start = DimensionFormatter.parseBigDecimal(_addSubStart.value)
                val change = DimensionFormatter.parseBigDecimal(_addSubChange.value)
                if (start != null && change != null) {
                    try {
                        val result = WorkBenchCalculator.subtractDimensions(start, change)
                        _calcResult.value = result
                        _calcFormula.value = "$start ${unit.symbol} − $change ${unit.symbol} = $result ${unit.symbol}"
                    } catch (e: IllegalArgumentException) {
                        _calcResult.value = null
                        _calcFormula.value = null
                        _calcError.value = e.message ?: "Result cannot be negative."
                    }
                } else {
                    _calcResult.value = null
                    _calcFormula.value = null
                }
            }

            CalculationType.HALF -> {
                val value = DimensionFormatter.parseBigDecimal(_halfDoubleValue.value)
                if (value != null) {
                    val result = WorkBenchCalculator.halfDimension(value)
                    _calcResult.value = result
                    _calcFormula.value = "$value ${unit.symbol} ÷ 2 = $result ${unit.symbol}"
                } else {
                    _calcResult.value = null
                    _calcFormula.value = null
                }
            }

            CalculationType.DOUBLE -> {
                val value = DimensionFormatter.parseBigDecimal(_halfDoubleValue.value)
                if (value != null) {
                    val result = WorkBenchCalculator.doubleDimension(value)
                    _calcResult.value = result
                    _calcFormula.value = "$value ${unit.symbol} × 2 = $result ${unit.symbol}"
                } else {
                    _calcResult.value = null
                    _calcFormula.value = null
                }
            }

            CalculationType.TOTAL_FROM_PIECES -> {
                val size = DimensionFormatter.parseBigDecimal(_pieceSize.value)
                val qtyInt = _pieceQuantity.value.toIntOrNull()
                val gap = DimensionFormatter.parseBigDecimal(_pieceGap.value) ?: BigDecimal.ZERO

                if (size != null && qtyInt != null && qtyInt >= 1) {
                    val result = WorkBenchCalculator.calculatePieceTotal(size, qtyInt, gap)
                    _calcResult.value = result
                    val formulaStr = if (qtyInt == 1) {
                        "$size ${unit.symbol} × 1 = $result ${unit.symbol}"
                    } else if (gap.compareTo(BigDecimal.ZERO) == 0) {
                        "$size × $qtyInt = $result ${unit.symbol}"
                    } else {
                        "($size × $qtyInt) + ($gap × ${qtyInt - 1}) = $result ${unit.symbol}"
                    }
                    _calcFormula.value = formulaStr
                } else {
                    _calcResult.value = null
                    _calcFormula.value = null
                }
            }

            CalculationType.CENTER_POSITION -> {
                val available = DimensionFormatter.parseBigDecimal(_centerAvailable.value)
                val obj = DimensionFormatter.parseBigDecimal(_centerObject.value)

                if (available != null && obj != null) {
                    try {
                        val centerResult: CenterResult = WorkBenchCalculator.calculateCenterPosition(available, obj)
                        _calcResult.value = centerResult.left
                        _calcSecondaryResult.value = centerResult.right
                        _calcFormula.value = "($available − $obj) ÷ 2 = ${centerResult.left} ${unit.symbol}"
                    } catch (e: IllegalArgumentException) {
                        _calcResult.value = null
                        _calcSecondaryResult.value = null
                        _calcFormula.value = null
                        _calcError.value = e.message ?: "Object is larger than available space."
                    }
                } else {
                    _calcResult.value = null
                    _calcSecondaryResult.value = null
                    _calcFormula.value = null
                }
            }
        }
    }

    fun saveCurrentCalculation(customName: String? = null, notes: String? = null) {
        val result = _calcResult.value ?: return
        val unit = _calcUnit.value
        val mode = _calculatorMode.value

        val defaultName = when (mode) {
            CalculationType.ADD -> "Addition"
            CalculationType.SUBTRACT -> "Subtraction"
            CalculationType.HALF -> "Half Dimension"
            CalculationType.DOUBLE -> "Double Dimension"
            CalculationType.TOTAL_FROM_PIECES -> "Piece Run Total"
            CalculationType.CENTER_POSITION -> "Centered Position"
        }

        val name = if (!customName.isNullOrBlank()) customName else defaultName
        val inputs = mutableListOf<BigDecimal>()

        when (mode) {
            CalculationType.ADD, CalculationType.SUBTRACT -> {
                DimensionFormatter.parseBigDecimal(_addSubStart.value)?.let { inputs.add(it) }
                DimensionFormatter.parseBigDecimal(_addSubChange.value)?.let { inputs.add(it) }
            }
            CalculationType.HALF, CalculationType.DOUBLE -> {
                DimensionFormatter.parseBigDecimal(_halfDoubleValue.value)?.let { inputs.add(it) }
            }
            CalculationType.TOTAL_FROM_PIECES -> {
                DimensionFormatter.parseBigDecimal(_pieceSize.value)?.let { inputs.add(it) }
                _pieceQuantity.value.toBigDecimalOrNull()?.let { inputs.add(it) }
                DimensionFormatter.parseBigDecimal(_pieceGap.value)?.let { inputs.add(it) }
            }
            CalculationType.CENTER_POSITION -> {
                DimensionFormatter.parseBigDecimal(_centerAvailable.value)?.let { inputs.add(it) }
                DimensionFormatter.parseBigDecimal(_centerObject.value)?.let { inputs.add(it) }
            }
        }

        val calculation = DimensionCalculation(
            id = UUID.randomUUID().toString(),
            name = name,
            calculationType = mode,
            inputValues = inputs,
            result = result,
            unit = unit.symbol,
            formula = _calcFormula.value ?: "",
            secondaryResult = _calcSecondaryResult.value,
            notes = notes,
            createdAt = System.currentTimeMillis()
        )

        viewModelScope.launch {
            repository.saveCalculation(calculation)
        }
    }

    // --- Dimension Operations ---
    fun saveDimension(
        id: String? = null,
        name: String,
        category: DimensionCategory,
        length: BigDecimal?,
        width: BigDecimal?,
        height: BigDecimal?,
        thickness: BigDecimal?,
        unit: DimensionUnit,
        notes: String?,
        isFavorite: Boolean = false
    ) {
        val now = System.currentTimeMillis()
        val recordId = id ?: UUID.randomUUID().toString()

        // Normalize all stored values into millimeters
        val lengthMm = length?.let { UnitConverter.toMillimeters(it, unit) }
        val widthMm = width?.let { UnitConverter.toMillimeters(it, unit) }
        val heightMm = height?.let { UnitConverter.toMillimeters(it, unit) }
        val thicknessMm = thickness?.let { UnitConverter.toMillimeters(it, unit) }

        val dimension = WorkshopDimension(
            id = recordId,
            name = name.trim(),
            category = category,
            lengthMm = lengthMm,
            widthMm = widthMm,
            heightMm = heightMm,
            thicknessMm = thicknessMm,
            unit = unit,
            notes = notes?.trim()?.ifEmpty { null },
            isFavorite = isFavorite,
            createdAt = if (id != null) {
                dimensions.value.find { it.id == id }?.createdAt ?: now
            } else now,
            updatedAt = now
        )

        viewModelScope.launch {
            repository.saveDimension(dimension)
        }
    }

    fun duplicateDimension(dimension: WorkshopDimension) {
        val now = System.currentTimeMillis()
        val duplicated = dimension.copy(
            id = UUID.randomUUID().toString(),
            name = "Copy of ${dimension.name}",
            createdAt = now,
            updatedAt = now
        )
        viewModelScope.launch {
            repository.saveDimension(duplicated)
        }
    }

    fun deleteDimension(dimensionId: String) {
        viewModelScope.launch {
            repository.deleteDimension(dimensionId)
        }
    }

    fun toggleFavorite(dimensionId: String) {
        viewModelScope.launch {
            repository.toggleFavorite(dimensionId)
        }
    }

    fun recordRecent(dimensionId: String) {
        viewModelScope.launch {
            repository.recordRecent(dimensionId)
        }
    }

    // --- History Operations ---
    fun setHistoryFilter(filter: HistoryFilter) {
        _historyFilter.value = filter
    }

    fun setHistorySearchQuery(query: String) {
        _historySearchQuery.value = query
    }

    fun deleteCalculation(calculationId: String) {
        viewModelScope.launch {
            repository.deleteCalculation(calculationId)
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            repository.clearAllCalculations()
        }
    }

    fun clearAllDimensions() {
        viewModelScope.launch {
            repository.clearAllDimensions()
        }
    }

    // --- Settings Operations ---
    fun updateSettings(update: (AppSettings) -> AppSettings) {
        viewModelScope.launch {
            repository.updateSettings(update)
        }
    }

    fun resetSettings() {
        viewModelScope.launch {
            repository.resetSettings()
        }
    }
}
