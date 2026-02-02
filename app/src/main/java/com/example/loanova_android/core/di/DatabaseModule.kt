package com.example.loanova_android.core.di

import android.content.Context
import androidx.room.Room
import com.example.loanova_android.core.database.AppDatabase
import com.example.loanova_android.data.local.dao.ApplicationHistoryDao
import com.example.loanova_android.data.local.dao.BranchDao
import com.example.loanova_android.data.local.dao.LoanApplicationDao
import com.example.loanova_android.data.local.dao.PlafondDao
import com.example.loanova_android.data.local.dao.UserDao
import com.example.loanova_android.data.local.dao.UserPlafondDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "loanova.db"
        )
        .fallbackToDestructiveMigration()
        .build()
    }

    @Provides
    fun provideUserDao(database: AppDatabase): UserDao = database.userDao()

    @Provides
    fun providePlafondDao(database: AppDatabase): PlafondDao = database.plafondDao()
    
    @Provides
    fun provideLoanApplicationDao(database: AppDatabase): LoanApplicationDao = database.loanApplicationDao()
    
    @Provides
    fun provideApplicationHistoryDao(database: AppDatabase): ApplicationHistoryDao = database.applicationHistoryDao()
    
    @Provides
    fun provideBranchDao(database: AppDatabase): BranchDao = database.branchDao()
    
    @Provides
    fun provideUserPlafondDao(database: AppDatabase): UserPlafondDao = database.userPlafondDao()
}
