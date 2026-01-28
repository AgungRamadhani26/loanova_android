package com.example.loanova_android.data.remote.api

import com.example.loanova_android.core.base.ApiResponse
import com.example.loanova_android.data.model.dto.UserProfileResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

interface UserProfileApi {
    @GET("api/user-profiles/me")
    suspend fun getMyProfile(): Response<ApiResponse<UserProfileResponse>>

    @Multipart
    @POST("api/user-profiles/complete")
    suspend fun completeProfile(
        @PartMap textParts: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part ktpPhoto: MultipartBody.Part?,
        @Part profilePhoto: MultipartBody.Part?,
        @Part npwpPhoto: MultipartBody.Part?
    ): Response<ApiResponse<UserProfileResponse>>
}
