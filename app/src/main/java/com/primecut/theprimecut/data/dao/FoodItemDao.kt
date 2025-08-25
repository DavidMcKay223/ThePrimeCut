package com.primecut.theprimecut.data.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.primecut.theprimecut.data.model.FoodItem

@Dao
interface FoodItemDao {
    @Query("SELECT * FROM food_items WHERE recipeName = :recipeName LIMIT 1")
    suspend fun getFoodItemByName(recipeName: String): FoodItem?

    @Query("SELECT * FROM food_items ORDER BY recipeName ASC")
    suspend fun getAll(): List<FoodItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(foodItem: FoodItem)
}
