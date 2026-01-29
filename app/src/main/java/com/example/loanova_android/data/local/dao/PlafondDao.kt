package com.example.loanova_android.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.loanova_android.data.local.entity.PlafondEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PlafondDao {
    @Query("SELECT * FROM plafond_entity")
    fun getAllPlafonds(): Flow<List<PlafondEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(plafonds: List<PlafondEntity>)

    @Query("DELETE FROM plafond_entity")
    suspend fun deleteAll()
}
