package com.primecut.theprimecut.di

import android.content.Context
import androidx.room.Room
import com.primecut.theprimecut.data.database.AppDatabase
import com.primecut.theprimecut.data.dao.FoodItemDao
import com.primecut.theprimecut.data.dao.MealEntryDao
import com.primecut.theprimecut.data.dao.UserProfileDao
import com.primecut.theprimecut.data.dao.WeightLogDao
import com.primecut.theprimecut.data.repository.FoodItemRepository
import com.primecut.theprimecut.data.repository.MealEntryRepository
import com.primecut.theprimecut.data.repository.UserProfileRepository
import com.primecut.theprimecut.data.repository.WeightLogRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    // Database
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "primecut.db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    // DAOs
    @Provides
    @Singleton
    fun provideFoodItemDao(db: AppDatabase): FoodItemDao = db.foodItemDao()

    @Provides
    @Singleton
    fun provideMealEntryDao(db: AppDatabase): MealEntryDao = db.mealEntryDao()

    @Provides
    @Singleton
    fun provideUserProfileDao(db: AppDatabase): UserProfileDao = db.userProfileDao()

    @Provides
    @Singleton
    fun provideWeightLogDao(db: AppDatabase): WeightLogDao = db.weightLogDao()

    // Repositories
    @Provides
    @Singleton
    fun provideFoodItemRepository(dao: FoodItemDao) = FoodItemRepository(dao)

    @Provides
    @Singleton
    fun provideMealEntryRepository(dao: MealEntryDao) = MealEntryRepository(dao)

    @Provides
    @Singleton
    fun provideUserProfileRepository(dao: UserProfileDao) = UserProfileRepository(dao)

    @Provides
    @Singleton
    fun provideWeightLogRepository(dao: WeightLogDao) = WeightLogRepository(dao)
}
