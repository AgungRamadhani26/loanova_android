package com.example.loanova_android.domain.repository

import com.example.loanova_android.data.model.dto.UserPlafondResponse
import kotlinx.coroutines.flow.Flow

interface IUserPlafondRepository {
    fun getActivePlafond(): Flow<Result<UserPlafondResponse>>
}
