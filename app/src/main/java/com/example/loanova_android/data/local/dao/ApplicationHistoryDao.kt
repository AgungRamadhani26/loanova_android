package com.example.loanova_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loanova_android.data.local.entity.ApplicationHistoryEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database Application History.
 * Mendukung offline-first dengan caching local.
 */
@Dao
interface ApplicationHistoryDao {
    
    /**
     * Get history by loan application ID
     */
    @Query("SELECT * FROM application_history_entity WHERE loanApplicationId = :loanApplicationId ORDER BY createdAt DESC")
    fun getHistoryByLoanId(loanApplicationId: Long): Flow<List<ApplicationHistoryEntity>>
    
    /**
     * Insert atau update multiple history entries
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(histories: List<ApplicationHistoryEntity>)
    
    /**
     * Delete all history for specific loan application
     */
    @Query("DELETE FROM application_history_entity WHERE loanApplicationId = :loanApplicationId")
    suspend fun deleteByLoanId(loanApplicationId: Long)
    
    /**
     * Delete all history (untuk clear cache saat user logout)
     */
    @Query("DELETE FROM application_history_entity")
    suspend fun deleteAll()
    
    /**
     * Check apakah ada history untuk loan tertentu
     */
    @Query("SELECT COUNT(*) FROM application_history_entity WHERE loanApplicationId = :loanApplicationId")
    suspend fun getCountByLoanId(loanApplicationId: Long): Int
}
