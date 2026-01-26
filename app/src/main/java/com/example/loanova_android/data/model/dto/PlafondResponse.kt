package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

data class PlafondResponse(
    @SerializedName("id")
    val id: Long? = null,
    @SerializedName("name")
    val name: String? = null,
    @SerializedName("description")
    val description: String? = null,
    @SerializedName("maxAmount")
    val maxAmount: BigDecimal? = null,
    @SerializedName("interestRate")
    val interestRate: BigDecimal? = null,
    @SerializedName("tenorMin")
    val tenorMin: Int? = null,
    @SerializedName("tenorMax")
    val tenorMax: Int? = null
)
