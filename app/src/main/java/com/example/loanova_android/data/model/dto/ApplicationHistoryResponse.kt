package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Response DTO untuk Application History dari API.
 * Menampilkan riwayat perubahan status loan application.
 */
data class ApplicationHistoryResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("loanApplicationId") val loanApplicationId: Long,
    @SerializedName("actionByUserId") val actionByUserId: Long,
    @SerializedName("actionByUsername") val actionByUsername: String,
    @SerializedName("actionByRole") val actionByRole: String,
    @SerializedName("status") val status: String,
    @SerializedName("comment") val comment: String?,
    @SerializedName("createdAt") val createdAt: String
)
