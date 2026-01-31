package com.example.loanova_android.domain.usecase.plafond

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.UserPlafondResponse
import com.example.loanova_android.domain.repository.IUserPlafondRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class GetActivePlafondUseCase @Inject constructor(
    private val repository: IUserPlafondRepository
) {
    fun execute(): Flow<Resource<UserPlafondResponse>> {
        return repository.getActivePlafond().map { result ->
            result.fold(
                onSuccess = { data -> Resource.Success(data) },
                onFailure = { exception -> Resource.Error(exception.message ?: "Gagal mengambil data plafond") }
            )
        }
    }
}
