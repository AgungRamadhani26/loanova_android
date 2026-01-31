package com.example.loanova_android.core.di

import com.example.loanova_android.data.repository.AuthRepositoryImpl
import com.example.loanova_android.data.repository.BranchRepository
import com.example.loanova_android.data.repository.LoanApplicationRepository
import com.example.loanova_android.data.repository.PlafondRepositoryImpl
import com.example.loanova_android.domain.repository.IAuthRepository
import com.example.loanova_android.data.repository.UserProfileRepositoryImpl
import com.example.loanova_android.domain.repository.IBranchRepository
import com.example.loanova_android.domain.repository.ILoanApplicationRepository
import com.example.loanova_android.domain.repository.IUserProfileRepository
import com.example.loanova_android.domain.repository.IPlafondRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): IAuthRepository

    @Binds
    @Singleton
    abstract fun bindPlafondRepository(
        plafondRepositoryImpl: PlafondRepositoryImpl
    ): IPlafondRepository

    @Binds
    @Singleton
    abstract fun bindUserProfileRepository(
        userProfileRepositoryImpl: UserProfileRepositoryImpl
    ): IUserProfileRepository

    @Binds
    @Singleton
    abstract fun bindUserPlafondRepository(
        userPlafondRepositoryImpl: com.example.loanova_android.data.repository.UserPlafondRepositoryImpl
    ): com.example.loanova_android.domain.repository.IUserPlafondRepository

    @Binds
    @Singleton
    abstract fun bindBranchRepository(
        branchRepository: BranchRepository
    ): IBranchRepository

    @Binds
    @Singleton
    abstract fun bindLoanApplicationRepository(
        loanApplicationRepository: LoanApplicationRepository
    ): ILoanApplicationRepository
}

