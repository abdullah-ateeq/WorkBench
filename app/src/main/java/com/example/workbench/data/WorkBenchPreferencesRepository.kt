package com.example.workbench.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.workbench.model.AppSettings
import com.example.workbench.model.AppThemeMode
import com.example.workbench.model.DimensionCalculation
import com.example.workbench.model.DimensionUnit
import com.example.workbench.model.WorkshopDimension
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import org.json.JSONArray

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "workbench_preferences")

class WorkBenchPreferencesRepository(private val context: Context) {

    companion object {
        const val MAX_RECORDS = 100
        const val MAX_RECENTS = 5

        private val KEY_DIMENSIONS = stringPreferencesKey("saved_dimensions")
        private val KEY_CALCULATIONS = stringPreferencesKey("calculation_history")
        private val KEY_RECENT_IDS = stringPreferencesKey("recent_dimension_ids")

        private val KEY_THEME = stringPreferencesKey("setting_theme")
        private val KEY_DYNAMIC_COLOR = booleanPreferencesKey("setting_dynamic_color")
        private val KEY_DEFAULT_UNIT = stringPreferencesKey("setting_default_unit")
        private val KEY_DECIMAL_PLACES = intPreferencesKey("setting_decimal_places")
        private val KEY_SHOW_AREA = booleanPreferencesKey("setting_show_area")
        private val KEY_SHOW_VOLUME = booleanPreferencesKey("setting_show_volume")
        private val KEY_SHOW_DIAGRAM = booleanPreferencesKey("setting_show_diagram")
        private val KEY_SHOW_FORMULA = booleanPreferencesKey("setting_show_formula")
        private val KEY_HAPTICS = booleanPreferencesKey("setting_haptics")
        private val KEY_CONFIRM_DELETE = booleanPreferencesKey("setting_confirm_delete")
    }

    val dimensionsFlow: Flow<List<WorkshopDimension>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_DIMENSIONS]
        WorkBenchJsonAdapter.dimensionsFromJson(json)
    }

    val calculationsFlow: Flow<List<DimensionCalculation>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_CALCULATIONS]
        WorkBenchJsonAdapter.calculationsFromJson(json)
    }

    val recentIdsFlow: Flow<List<String>> = context.dataStore.data.map { prefs ->
        val json = prefs[KEY_RECENT_IDS]
        if (json.isNullOrBlank()) emptyList()
        else {
            try {
                val array = JSONArray(json)
                val list = mutableListOf<String>()
                for (i in 0 until array.length()) {
                    list.add(array.getString(i))
                }
                list
            } catch (_: Exception) {
                emptyList()
            }
        }
    }

    val settingsFlow: Flow<AppSettings> = context.dataStore.data.map { prefs ->
        AppSettings(
            themeMode = AppThemeMode.fromString(prefs[KEY_THEME] ?: AppThemeMode.SYSTEM.name),
            dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
            defaultUnit = DimensionUnit.fromSymbol(prefs[KEY_DEFAULT_UNIT] ?: "cm"),
            decimalPlaces = prefs[KEY_DECIMAL_PLACES] ?: 2,
            showArea = prefs[KEY_SHOW_AREA] ?: true,
            showVolume = prefs[KEY_SHOW_VOLUME] ?: true,
            showDimensionDiagram = prefs[KEY_SHOW_DIAGRAM] ?: true,
            showCalculationFormula = prefs[KEY_SHOW_FORMULA] ?: true,
            hapticFeedback = prefs[KEY_HAPTICS] ?: true,
            confirmDelete = prefs[KEY_CONFIRM_DELETE] ?: true
        )
    }

    suspend fun saveDimension(dimension: WorkshopDimension) {
        context.dataStore.edit { prefs ->
            val currentList = WorkBenchJsonAdapter.dimensionsFromJson(prefs[KEY_DIMENSIONS]).toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == dimension.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = dimension
            } else {
                currentList.add(0, dimension)
            }

            // Cap at MAX_RECORDS (100)
            while (currentList.size > MAX_RECORDS) {
                currentList.removeAt(currentList.lastIndex)
            }

            prefs[KEY_DIMENSIONS] = WorkBenchJsonAdapter.dimensionsToJson(currentList)
        }
        recordRecent(dimension.id)
    }

    suspend fun deleteDimension(dimensionId: String) {
        context.dataStore.edit { prefs ->
            val currentList = WorkBenchJsonAdapter.dimensionsFromJson(prefs[KEY_DIMENSIONS]).toMutableList()
            currentList.removeAll { it.id == dimensionId }
            prefs[KEY_DIMENSIONS] = WorkBenchJsonAdapter.dimensionsToJson(currentList)

            // Remove from recents
            val currentRecents = prefs[KEY_RECENT_IDS]?.let { parseIds(it) } ?: emptyList()
            val updatedRecents = currentRecents.filter { it != dimensionId }
            prefs[KEY_RECENT_IDS] = JSONArray(updatedRecents).toString()
        }
    }

    suspend fun toggleFavorite(dimensionId: String) {
        context.dataStore.edit { prefs ->
            val currentList = WorkBenchJsonAdapter.dimensionsFromJson(prefs[KEY_DIMENSIONS]).toMutableList()
            val index = currentList.indexOfFirst { it.id == dimensionId }
            if (index >= 0) {
                val item = currentList[index]
                currentList[index] = item.copy(
                    isFavorite = !item.isFavorite,
                    updatedAt = System.currentTimeMillis()
                )
                prefs[KEY_DIMENSIONS] = WorkBenchJsonAdapter.dimensionsToJson(currentList)
            }
        }
    }

    suspend fun recordRecent(dimensionId: String) {
        context.dataStore.edit { prefs ->
            val currentRecents = (prefs[KEY_RECENT_IDS]?.let { parseIds(it) } ?: emptyList()).toMutableList()
            currentRecents.remove(dimensionId)
            currentRecents.add(0, dimensionId)
            while (currentRecents.size > MAX_RECENTS) {
                currentRecents.removeAt(currentRecents.lastIndex)
            }
            prefs[KEY_RECENT_IDS] = JSONArray(currentRecents).toString()
        }
    }

    suspend fun saveCalculation(calculation: DimensionCalculation) {
        context.dataStore.edit { prefs ->
            val currentList = WorkBenchJsonAdapter.calculationsFromJson(prefs[KEY_CALCULATIONS]).toMutableList()
            val existingIndex = currentList.indexOfFirst { it.id == calculation.id }
            if (existingIndex >= 0) {
                currentList[existingIndex] = calculation
            } else {
                currentList.add(0, calculation)
            }

            // Cap at MAX_RECORDS (100)
            while (currentList.size > MAX_RECORDS) {
                currentList.removeAt(currentList.lastIndex)
            }

            prefs[KEY_CALCULATIONS] = WorkBenchJsonAdapter.calculationsToJson(currentList)
        }
    }

    suspend fun deleteCalculation(calculationId: String) {
        context.dataStore.edit { prefs ->
            val currentList = WorkBenchJsonAdapter.calculationsFromJson(prefs[KEY_CALCULATIONS]).toMutableList()
            currentList.removeAll { it.id == calculationId }
            prefs[KEY_CALCULATIONS] = WorkBenchJsonAdapter.calculationsToJson(currentList)
        }
    }

    suspend fun clearAllDimensions() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_DIMENSIONS)
            prefs.remove(KEY_RECENT_IDS)
        }
    }

    suspend fun clearAllCalculations() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_CALCULATIONS)
        }
    }

    suspend fun updateSettings(update: (AppSettings) -> AppSettings) {
        context.dataStore.edit { prefs ->
            val current = AppSettings(
                themeMode = AppThemeMode.fromString(prefs[KEY_THEME] ?: AppThemeMode.SYSTEM.name),
                dynamicColor = prefs[KEY_DYNAMIC_COLOR] ?: true,
                defaultUnit = DimensionUnit.fromSymbol(prefs[KEY_DEFAULT_UNIT] ?: "cm"),
                decimalPlaces = prefs[KEY_DECIMAL_PLACES] ?: 2,
                showArea = prefs[KEY_SHOW_AREA] ?: true,
                showVolume = prefs[KEY_SHOW_VOLUME] ?: true,
                showDimensionDiagram = prefs[KEY_SHOW_DIAGRAM] ?: true,
                showCalculationFormula = prefs[KEY_SHOW_FORMULA] ?: true,
                hapticFeedback = prefs[KEY_HAPTICS] ?: true,
                confirmDelete = prefs[KEY_CONFIRM_DELETE] ?: true
            )
            val updated = update(current)

            prefs[KEY_THEME] = updated.themeMode.name
            prefs[KEY_DYNAMIC_COLOR] = updated.dynamicColor
            prefs[KEY_DEFAULT_UNIT] = updated.defaultUnit.symbol
            prefs[KEY_DECIMAL_PLACES] = updated.decimalPlaces
            prefs[KEY_SHOW_AREA] = updated.showArea
            prefs[KEY_SHOW_VOLUME] = updated.showVolume
            prefs[KEY_SHOW_DIAGRAM] = updated.showDimensionDiagram
            prefs[KEY_SHOW_FORMULA] = updated.showCalculationFormula
            prefs[KEY_HAPTICS] = updated.hapticFeedback
            prefs[KEY_CONFIRM_DELETE] = updated.confirmDelete
        }
    }

    suspend fun resetSettings() {
        context.dataStore.edit { prefs ->
            prefs.remove(KEY_THEME)
            prefs.remove(KEY_DYNAMIC_COLOR)
            prefs.remove(KEY_DEFAULT_UNIT)
            prefs.remove(KEY_DECIMAL_PLACES)
            prefs.remove(KEY_SHOW_AREA)
            prefs.remove(KEY_SHOW_VOLUME)
            prefs.remove(KEY_SHOW_DIAGRAM)
            prefs.remove(KEY_SHOW_FORMULA)
            prefs.remove(KEY_HAPTICS)
            prefs.remove(KEY_CONFIRM_DELETE)
        }
    }

    private fun parseIds(json: String): List<String> {
        return try {
            val array = JSONArray(json)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }
}
