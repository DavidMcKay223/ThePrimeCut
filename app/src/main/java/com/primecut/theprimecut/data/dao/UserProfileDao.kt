package com.primecut.theprimecut.data.dao

import androidx.room.*
import com.primecut.theprimecut.data.model.UserProfile

@Dao
interface UserProfileDao {

    @Query("SELECT * FROM user_profiles WHERE userName = :userName LIMIT 1")
    suspend fun getUserProfile(userName: String): UserProfile?

    @Query("SELECT userName FROM user_profiles")
    suspend fun getAllUserNames(): List<String>

    @Query("SELECT * FROM user_profiles")
    suspend fun getAllProfiles(): List<UserProfile>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfile)

    @Update
    suspend fun updateProfile(profile: UserProfile)
}
