package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class UserPlafondResponse(
    @SerializedName("id")
    val id: Long,
    @SerializedName("userId")
    val userId: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName("plafondId")
    val plafondId: Long,
    @SerializedName("plafondName")
    val plafondName: String,
    @SerializedName("maxAmount")
    val maxAmount: BigDecimal,
    @SerializedName("remainingAmount")
    val remainingAmount: BigDecimal,
    @SerializedName("isActive")
    val isActive: Boolean,
    @SerializedName("assignedAt")
    val assignedAt: String // ISO Date String
)
