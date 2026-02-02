package com.example.loanova_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loanova_android.data.local.entity.BranchEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO untuk operasi database Branch.
 * Mendukung offline-first dengan caching local.
 */
@Dao
interface BranchDao {
    
    /**
     * Get all branches
     */
    @Query("SELECT * FROM branch_entity ORDER BY branchName ASC")
    fun getAllBranches(): Flow<List<BranchEntity>>
    
    /**
     * Get branch by ID
     */
    @Query("SELECT * FROM branch_entity WHERE id = :branchId LIMIT 1")
    suspend fun getBranchById(branchId: Long): BranchEntity?
    
    /**
     * Insert atau update multiple branches
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(branches: List<BranchEntity>)
    
    /**
     * Delete all branches (untuk refresh data)
     */
    @Query("DELETE FROM branch_entity")
    suspend fun deleteAll()
    
    /**
     * Check apakah ada data di cache
     */
    @Query("SELECT COUNT(*) FROM branch_entity")
    suspend fun getCount(): Int
}
