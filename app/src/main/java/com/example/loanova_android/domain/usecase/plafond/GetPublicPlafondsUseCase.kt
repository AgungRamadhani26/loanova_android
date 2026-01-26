package com.example.loanova_android.domain.usecase.plafond

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.model.Plafond
import com.example.loanova_android.domain.repository.IPlafondRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPublicPlafondsUseCase @Inject constructor(
    private val repository: IPlafondRepository
) {
    fun execute(): Flow<Resource<List<Plafond>>> {
        return repository.getPublicPlafonds()
    }
}
