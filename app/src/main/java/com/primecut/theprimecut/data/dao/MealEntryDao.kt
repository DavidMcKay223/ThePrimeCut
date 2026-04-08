package com.primecut.theprimecut.data.dao

import androidx.room.*
import com.primecut.theprimecut.data.model.MealEntry

@Dao
interface MealEntryDao {

    @Query("SELECT * FROM meal_entries WHERE userName = :userName ORDER BY date DESC")
    suspend fun getAll(userName: String): List<MealEntry>

    @Query("SELECT * FROM meal_entries WHERE date = :date AND userName = :userName ORDER BY date DESC")
    suspend fun getByDate(date: String, userName: String): List<MealEntry>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun add(entry: MealEntry)

    @Update
    suspend fun update(entry: MealEntry)

    @Query("DELETE FROM meal_entries WHERE id = :id")
    suspend fun delete(id: Int)

    @Query("SELECT * FROM meal_entries WHERE date >= :since AND userName = :userName")
    suspend fun getSince(since: String, userName: String): List<MealEntry>

    @Query("SELECT * FROM meal_entries WHERE date BETWEEN :start AND :end AND userName = :userName ORDER BY date DESC")
    suspend fun getByDateRange(start: String, end: String, userName: String): List<MealEntry>
}