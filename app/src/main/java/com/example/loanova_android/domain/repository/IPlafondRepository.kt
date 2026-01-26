package com.example.loanova_android.domain.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.model.Plafond
import kotlinx.coroutines.flow.Flow

interface IPlafondRepository {
    fun getPublicPlafonds(): Flow<Resource<List<Plafond>>>
}
