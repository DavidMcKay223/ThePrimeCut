package com.primecut.theprimecut.data.repository

import com.primecut.theprimecut.data.dao.FoodItemDao
import com.primecut.theprimecut.data.model.FoodItem

class FoodItemRepository(
    private val dao: FoodItemDao
) {
    suspend fun getAll(): List<FoodItem> = dao.getAll()
    suspend fun getFoodItemByName(name: String) = dao.getFoodItemByName(name)
    suspend fun insertAll(items: List<FoodItem>) = items.forEach { dao.insert(it) }
}
