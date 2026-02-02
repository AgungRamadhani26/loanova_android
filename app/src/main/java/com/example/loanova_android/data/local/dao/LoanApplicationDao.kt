package com.example.loanova_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loanova_android.data.local.entity.LoanApplicationEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database Loan Application.
 * Mendukung offline-first dengan caching local.
 */
@Dao
interface LoanApplicationDao {
    
    /**
     * Get all loan applications untuk user yang login
     */
    @Query("SELECT * FROM loan_application_entity ORDER BY submittedAt DESC")
    fun getAllApplications(): Flow<List<LoanApplicationEntity>>
    
    /**
     * Get loan application by ID
     */
    @Query("SELECT * FROM loan_application_entity WHERE id = :applicationId")
    fun getApplicationById(applicationId: Long): Flow<LoanApplicationEntity?>
    
    /**
     * Get loan application by ID (non-Flow untuk sinkronisasi)
     */
    @Query("SELECT * FROM loan_application_entity WHERE id = :applicationId LIMIT 1")
    suspend fun getApplicationByIdSync(applicationId: Long): LoanApplicationEntity?
    
    /**
     * Insert atau update single application
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(application: LoanApplicationEntity)
    
    /**
     * Insert atau update multiple applications
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(applications: List<LoanApplicationEntity>)
    
    /**
     * Delete all applications (untuk clear cache saat user logout)
     */
    @Query("DELETE FROM loan_application_entity")
    suspend fun deleteAll()
    
    /**
     * Delete specific application by ID
     */
    @Query("DELETE FROM loan_application_entity WHERE id = :applicationId")
    suspend fun deleteById(applicationId: Long)
    
    /**
     * Check apakah ada data di cache
     */
    @Query("SELECT COUNT(*) FROM loan_application_entity")
    suspend fun getCount(): Int
}
