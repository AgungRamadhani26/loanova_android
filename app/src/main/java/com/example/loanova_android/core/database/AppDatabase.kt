package com.example.loanova_android.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.loanova_android.data.local.dao.PlafondDao
import com.example.loanova_android.data.local.dao.UserDao
import com.example.loanova_android.data.local.entity.PlafondEntity
import com.example.loanova_android.data.local.entity.UserProfileEntity
import com.example.loanova_android.data.model.entity.UserEntity

@Database(
    entities = [
        UserEntity::class, 
        PlafondEntity::class, 
        UserProfileEntity::class
    ], 
    version = 2, // Increment version for schema change (Room will likely need fallbackToDestructiveMigration if no Migration provided)
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun plafondDao(): PlafondDao
}
