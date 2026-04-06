package com.primecut.theprimecut.di

import android.content.Context
import androidx.room.Room
import com.primecut.theprimecut.data.database.AppDatabase
import com.primecut.theprimecut.data.repository.FoodItemRepository
import com.primecut.theprimecut.data.repository.MealEntryRepository
import com.primecut.theprimecut.data.repository.UserProfileRepository
import com.primecut.theprimecut.data.repository.WeightLogRepository

class AppContainer(private val context: Context) {

    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "primecut.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    val foodItemRepository: FoodItemRepository by lazy {
        FoodItemRepository(database.foodItemDao())
    }

    val mealEntryRepository: MealEntryRepository by lazy {
        MealEntryRepository(database.mealEntryDao())
    }

    val userProfileRepository: UserProfileRepository by lazy {
        UserProfileRepository(database.userProfileDao())
    }

    val weightLogRepository: WeightLogRepository by lazy {
        WeightLogRepository(database.weightLogDao())
    }
}
