package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.remote.datasource.PlafondRemoteDataSource
import com.example.loanova_android.domain.model.Plafond
import com.example.loanova_android.domain.repository.IPlafondRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject

class PlafondRepositoryImpl @Inject constructor(
    private val remoteDataSource: PlafondRemoteDataSource
) : IPlafondRepository {

    override fun getPublicPlafonds(): Flow<Resource<List<Plafond>>> = flow {
        emit(Resource.Loading())
        try {
            val response = remoteDataSource.getPublicPlafonds()
            val body = response.body()

            if (response.isSuccessful && body?.success == true && body.data != null) {
                val plafonds = body.data.map { responseItem ->
                    Plafond(
                        id = responseItem.id ?: 0L,
                        name = responseItem.name ?: "",
                        description = responseItem.description ?: "",
                        maxAmount = responseItem.maxAmount ?: java.math.BigDecimal.ZERO,
                        interestRate = responseItem.interestRate ?: java.math.BigDecimal.ZERO,
                        tenorMin = responseItem.tenorMin ?: 0,
                        tenorMax = responseItem.tenorMax ?: 0
                    )
                }
                emit(Resource.Success(plafonds))
            } else {
                val errorMessage = body?.message ?: response.message()
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Unknown Network Error"))
        }
    }.flowOn(Dispatchers.IO)
}
