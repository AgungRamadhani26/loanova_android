package com.example.loanova_android.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.loanova_android.data.local.dao.UserDao
import com.example.loanova_android.data.model.entity.UserEntity

@Database(entities = [UserEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
}
