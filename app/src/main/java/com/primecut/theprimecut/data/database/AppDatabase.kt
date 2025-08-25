package com.primecut.theprimecut.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.primecut.theprimecut.data.dao.FoodItemDao
import com.primecut.theprimecut.data.dao.MealEntryDao
import com.primecut.theprimecut.data.dao.UserProfileDao
import com.primecut.theprimecut.data.dao.WeightLogDao
import com.primecut.theprimecut.data.model.FoodItem
import com.primecut.theprimecut.data.model.MealEntry
import com.primecut.theprimecut.data.model.UserProfile
import com.primecut.theprimecut.data.model.WeightLog

@Database(
    entities = [FoodItem::class, MealEntry::class, UserProfile::class, WeightLog::class],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun foodItemDao(): FoodItemDao
    abstract fun mealEntryDao(): MealEntryDao
    abstract fun userProfileDao(): UserProfileDao
    abstract fun weightLogDao(): WeightLogDao
}
