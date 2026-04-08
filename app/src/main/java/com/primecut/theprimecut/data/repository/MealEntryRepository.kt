package com.primecut.theprimecut.data.repository

import com.primecut.theprimecut.data.dao.MealEntryDao
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.data.model.MacroSummary
import java.time.LocalDate

class MealEntryRepository(private val dao: MealEntryDao) {

    private val mealTypeOrder = listOf("Breakfast", "Lunch", "Dinner", "Snack")
    private val orderMap = mealTypeOrder.withIndex().associate { it.value to it.index }

    suspend fun getAll(userName: String): List<MealEntry> {
        return dao.getAll(userName)
            .sortedWith(
                compareByDescending<MealEntry> { it.date }
                    .thenBy { orderMap[it.mealType] ?: 4 }
            )
    }

    suspend fun getByDate(date: String, userName: String): List<MealEntry> {
        return dao.getByDate(date, userName)
            .sortedWith(
                compareByDescending<MealEntry> { it.date }
                    .thenBy { orderMap[it.mealType] ?: 4 }
            )
    }

    suspend fun add(entry: MealEntry) = dao.add(entry)

    suspend fun update(entry: MealEntry) = dao.update(entry)

    suspend fun delete(id: Int) = dao.delete(id)

    suspend fun getByDateRange(start: String, end: String, userName: String): List<MealEntry> {
        return dao.getByDateRange(start, end, userName)
            .sortedWith(
                compareByDescending<MealEntry> { it.date }
                    .thenBy { orderMap[it.mealType] ?: 4 }
            )
    }

    suspend fun getCaloriesByDay(userName: String, daysBack: Int = 6): Map<String, Float> {
        val since = LocalDate.now().minusDays(daysBack.toLong()).toString()
        return dao.getSince(since, userName)
            .groupBy { it.day }
            .mapValues { (_, meals) -> meals.sumOf { it.calories.toDouble() }.toFloat() }
    }

    suspend fun getMacroSummary(date: String, userName: String): MacroSummary {
        val entries = dao.getByDate(date, userName)
        return MacroSummary(
            calories = entries.sumOf { it.calories.toDouble() }.toFloat(),
            protein = entries.sumOf { it.protein.toDouble() }.toFloat(),
            carbs = entries.sumOf { it.carbs.toDouble() }.toFloat(),
            fats = entries.sumOf { it.fats.toDouble() }.toFloat(),
            fiber = entries.sumOf { it.fiber.toDouble() }.toFloat()
        )
    }

    suspend fun getMacroSummariesByDateRange(start: String, end: String, userName: String): Map<String, MacroSummary> {
        val entries = dao.getByDateRange(start, end, userName)

        val grouped = entries.groupBy { it.day }.mapValues { (_, meals) ->
            MacroSummary(
                calories = meals.sumOf { it.calories.toDouble() }.toFloat(),
                protein = meals.sumOf { it.protein.toDouble() }.toFloat(),
                carbs = meals.sumOf { it.carbs.toDouble() }.toFloat(),
                fats = meals.sumOf { it.fats.toDouble() }.toFloat(),
                fiber = meals.sumOf { it.fiber.toDouble() }.toFloat()
            )
        }

        val weekdays = listOf("Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday")
        return weekdays.associateWith { grouped[it] ?: MacroSummary() }
    }
}
