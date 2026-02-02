package com.example.loanova_android.core.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.loanova_android.data.local.dao.ApplicationHistoryDao
import com.example.loanova_android.data.local.dao.BranchDao
import com.example.loanova_android.data.local.dao.LoanApplicationDao
import com.example.loanova_android.data.local.dao.PlafondDao
import com.example.loanova_android.data.local.dao.UserDao
import com.example.loanova_android.data.local.dao.UserPlafondDao
import com.example.loanova_android.data.local.entity.ApplicationHistoryEntity
import com.example.loanova_android.data.local.entity.BranchEntity
import com.example.loanova_android.data.local.entity.LoanApplicationEntity
import com.example.loanova_android.data.local.entity.PlafondEntity
import com.example.loanova_android.data.local.entity.UserPlafondEntity
import com.example.loanova_android.data.local.entity.UserProfileEntity
import com.example.loanova_android.data.model.entity.UserEntity

@Database(
    entities = [
        UserEntity::class, 
        PlafondEntity::class, 
        UserProfileEntity::class,
        LoanApplicationEntity::class,
        ApplicationHistoryEntity::class,
        BranchEntity::class,
        UserPlafondEntity::class
    ], 
    version = 4, // Increment version for offline-first active plafond
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun plafondDao(): PlafondDao
    abstract fun loanApplicationDao(): LoanApplicationDao
    abstract fun applicationHistoryDao(): ApplicationHistoryDao
    abstract fun branchDao(): BranchDao
    abstract fun userPlafondDao(): UserPlafondDao
}
