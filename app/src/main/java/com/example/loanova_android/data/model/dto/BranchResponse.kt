package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal
import java.time.LocalDateTime

/**
 * Response DTO untuk Branch dari API.
 */
data class BranchResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("branchCode") val branchCode: String,
    @SerializedName("branchName") val branchName: String,
    @SerializedName("branchAddress") val branchAddress: String?
)
