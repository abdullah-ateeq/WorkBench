package com.example.workbench.data

import com.example.workbench.model.CalculationType
import com.example.workbench.model.DimensionCategory
import com.example.workbench.model.DimensionCalculation
import com.example.workbench.model.DimensionUnit
import com.example.workbench.model.WorkshopDimension
import org.json.JSONArray
import org.json.JSONObject
import java.math.BigDecimal

object WorkBenchJsonAdapter {

    fun dimensionToJson(dimension: WorkshopDimension): JSONObject {
        return JSONObject().apply {
            put("id", dimension.id)
            put("name", dimension.name)
            put("category", dimension.category.name)
            put("lengthMm", dimension.lengthMm?.toPlainString() ?: JSONObject.NULL)
            put("widthMm", dimension.widthMm?.toPlainString() ?: JSONObject.NULL)
            put("heightMm", dimension.heightMm?.toPlainString() ?: JSONObject.NULL)
            put("thicknessMm", dimension.thicknessMm?.toPlainString() ?: JSONObject.NULL)
            put("unit", dimension.unit.symbol)
            put("notes", dimension.notes ?: JSONObject.NULL)
            put("isFavorite", dimension.isFavorite)
            put("createdAt", dimension.createdAt)
            put("updatedAt", dimension.updatedAt)
        }
    }

    fun dimensionFromJson(json: JSONObject): WorkshopDimension {
        val lengthMmStr = if (json.isNull("lengthMm")) null else json.optString("lengthMm")
        val widthMmStr = if (json.isNull("widthMm")) null else json.optString("widthMm")
        val heightMmStr = if (json.isNull("heightMm")) null else json.optString("heightMm")
        val thicknessMmStr = if (json.isNull("thicknessMm")) null else json.optString("thicknessMm")
        val notesStr = if (json.isNull("notes")) null else json.optString("notes")

        return WorkshopDimension(
            id = json.optString("id"),
            name = json.optString("name"),
            category = DimensionCategory.fromString(json.optString("category")),
            lengthMm = lengthMmStr?.let { BigDecimal(it) },
            widthMm = widthMmStr?.let { BigDecimal(it) },
            heightMm = heightMmStr?.let { BigDecimal(it) },
            thicknessMm = thicknessMmStr?.let { BigDecimal(it) },
            unit = DimensionUnit.fromSymbol(json.optString("unit", "cm")),
            notes = notesStr,
            isFavorite = json.optBoolean("isFavorite", false),
            createdAt = json.optLong("createdAt", System.currentTimeMillis()),
            updatedAt = json.optLong("updatedAt", System.currentTimeMillis())
        )
    }

    fun dimensionsToJson(list: List<WorkshopDimension>): String {
        val array = JSONArray()
        list.forEach { array.put(dimensionToJson(it)) }
        return array.toString()
    }

    fun dimensionsFromJson(jsonString: String?): List<WorkshopDimension> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<WorkshopDimension>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(dimensionFromJson(item))
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        return list
    }

    fun calculationToJson(calc: DimensionCalculation): JSONObject {
        return JSONObject().apply {
            put("id", calc.id)
            put("name", calc.name)
            put("calculationType", calc.calculationType.name)
            val inputsArray = JSONArray()
            calc.inputValues.forEach { inputsArray.put(it.toPlainString()) }
            put("inputValues", inputsArray)
            put("result", calc.result.toPlainString())
            put("unit", calc.unit)
            put("formula", calc.formula)
            put("secondaryResult", calc.secondaryResult?.toPlainString() ?: JSONObject.NULL)
            put("notes", calc.notes ?: JSONObject.NULL)
            put("createdAt", calc.createdAt)
        }
    }

    fun calculationFromJson(json: JSONObject): DimensionCalculation {
        val inputsArray = json.optJSONArray("inputValues")
        val inputs = mutableListOf<BigDecimal>()
        if (inputsArray != null) {
            for (i in 0 until inputsArray.length()) {
                inputs.add(BigDecimal(inputsArray.optString(i, "0")))
            }
        }
        val secondaryResultStr = if (json.isNull("secondaryResult")) null else json.optString("secondaryResult")
        val notesStr = if (json.isNull("notes")) null else json.optString("notes")

        return DimensionCalculation(
            id = json.optString("id"),
            name = json.optString("name"),
            calculationType = CalculationType.fromString(json.optString("calculationType")),
            inputValues = inputs,
            result = BigDecimal(json.optString("result", "0")),
            unit = json.optString("unit", "cm"),
            formula = json.optString("formula", ""),
            secondaryResult = secondaryResultStr?.let { BigDecimal(it) },
            notes = notesStr,
            createdAt = json.optLong("createdAt", System.currentTimeMillis())
        )
    }

    fun calculationsToJson(list: List<DimensionCalculation>): String {
        val array = JSONArray()
        list.forEach { array.put(calculationToJson(it)) }
        return array.toString()
    }

    fun calculationsFromJson(jsonString: String?): List<DimensionCalculation> {
        if (jsonString.isNullOrBlank()) return emptyList()
        val list = mutableListOf<DimensionCalculation>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                val item = array.getJSONObject(i)
                list.add(calculationFromJson(item))
            }
        } catch (_: Exception) {
            // Safe fallback
        }
        return list
    }
}
