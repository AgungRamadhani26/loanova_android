package com.example.loanova_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loanova_android.data.local.entity.UserPlafondEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database User Plafond (Active Plafond).
 * Mendukung offline-first dengan caching local.
 */
@Dao
interface UserPlafondDao {
    
    /**
     * Get active plafond untuk user yang login
     */
    @Query("SELECT * FROM user_plafond_entity WHERE userId = :userId AND isActive = 1 LIMIT 1")
    fun getActivePlafondByUserId(userId: Long): Flow<UserPlafondEntity?>
    
    /**
     * Get active plafond (sync version)
     */
    @Query("SELECT * FROM user_plafond_entity WHERE userId = :userId AND isActive = 1 LIMIT 1")
    suspend fun getActivePlafondByUserIdSync(userId: Long): UserPlafondEntity?
    
    /**
     * Insert atau update plafond
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(plafond: UserPlafondEntity)
    
    /**
     * Delete all user plafonds (untuk clear cache saat user logout)
     */
    @Query("DELETE FROM user_plafond_entity")
    suspend fun deleteAll()
    
    /**
     * Delete plafond untuk user tertentu
     */
    @Query("DELETE FROM user_plafond_entity WHERE userId = :userId")
    suspend fun deleteByUserId(userId: Long)
}
