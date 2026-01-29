package com.example.loanova_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loanova_android.data.model.entity.UserEntity
import com.example.loanova_android.data.local.entity.UserProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users WHERE username = :username")
    fun getUser(username: String): Flow<UserEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("DELETE FROM users")
    suspend fun clearAll()

    // --- Profile Methods ---
    @Query("SELECT * FROM user_profile_entity LIMIT 1") // Assuming single user per session for now
    fun getMyProfile(): Flow<UserProfileEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: UserProfileEntity)
    
    @Query("DELETE FROM user_profile_entity")
    suspend fun clearProfile()
}
