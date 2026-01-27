package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

data class ValidationErrorData(
    @SerializedName("errors")
    val errors: Map<String, String>? = null
)
