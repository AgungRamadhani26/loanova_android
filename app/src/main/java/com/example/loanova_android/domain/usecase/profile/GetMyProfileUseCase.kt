package com.example.loanova_android.domain.usecase.profile

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.UserProfileResponse
import com.example.loanova_android.domain.repository.IUserProfileRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetMyProfileUseCase @Inject constructor(
    private val repository: IUserProfileRepository
) {
    fun execute(): Flow<Resource<UserProfileResponse>> {
        return repository.getMyProfile()
    }
}
