package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName
import java.io.File

/**
 * Data Transfer Object for Loan Application Request.
 * Used to encapsulate all data needed for loan submission.
 * Note: Files are included here for convenience in passing data across layers,
 * but will be handled as MultipartBody.Part in the Repository.
 */
data class LoanApplicationRequest(
    @SerializedName("branchId")
    val branchId: Long,
    @SerializedName("amount")
    val amount: String,
    @SerializedName("tenor")
    val tenor: Int,
    @SerializedName("occupation")
    val occupation: String,
    @SerializedName("companyName")
    val companyName: String?,
    @SerializedName("rekeningNumber")
    val rekeningNumber: String,
    @SerializedName("latitude")
    val latitude: Double,
    @SerializedName("longitude")
    val longitude: Double,
    @SerializedName("savingBookCover")
    val savingBookCover: File,
    @SerializedName("payslipPhoto")
    val payslipPhoto: File
)
