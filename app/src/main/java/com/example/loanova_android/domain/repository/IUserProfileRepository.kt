package com.example.loanova_android.domain.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.UserProfileResponse
import kotlinx.coroutines.flow.Flow

import java.io.File

import com.example.loanova_android.data.model.dto.UserProfileCompleteRequest

interface IUserProfileRepository {
    fun getMyProfile(): Flow<Resource<UserProfileResponse>>
    fun completeProfile(request: UserProfileCompleteRequest): Flow<Resource<UserProfileResponse>>
    fun updateProfile(request: UserProfileCompleteRequest): Flow<Resource<UserProfileResponse>>
}
