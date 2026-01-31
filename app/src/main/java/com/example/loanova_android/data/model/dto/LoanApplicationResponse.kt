package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName
import java.math.BigDecimal

/**
 * Response DTO untuk Loan Application dari API.
 */
data class LoanApplicationResponse(
    @SerializedName("id") val id: Long,
    @SerializedName("userId") val userId: Long,
    @SerializedName("username") val username: String,
    @SerializedName("branchId") val branchId: Long,
    @SerializedName("branchCode") val branchCode: String,
    @SerializedName("plafondId") val plafondId: Long,
    @SerializedName("plafondName") val plafondName: String,
    @SerializedName("amount") val amount: BigDecimal,
    @SerializedName("tenor") val tenor: Int,
    @SerializedName("status") val status: String,
    @SerializedName("submittedAt") val submittedAt: String,
    
    // Snapshot data
    @SerializedName("fullNameSnapshot") val fullNameSnapshot: String,
    @SerializedName("phoneNumberSnapshot") val phoneNumberSnapshot: String,
    @SerializedName("userAddressSnapshot") val userAddressSnapshot: String,
    @SerializedName("nikSnapshot") val nikSnapshot: String,
    @SerializedName("birthDateSnapshot") val birthDateSnapshot: String?,
    @SerializedName("npwpNumberSnapshot") val npwpNumberSnapshot: String?,
    
    // Pekerjaan
    @SerializedName("occupation") val occupation: String,
    @SerializedName("companyName") val companyName: String?,
    
    // Keuangan
    @SerializedName("rekeningNumber") val rekeningNumber: String,
    
    // Dokumen
    @SerializedName("ktpPhotoSnapshot") val ktpPhotoSnapshot: String?,
    @SerializedName("npwpPhotoSnapshot") val npwpPhotoSnapshot: String?,
    @SerializedName("savingBookCover") val savingBookCover: String,
    @SerializedName("payslipPhoto") val payslipPhoto: String,
    
    // Lokasi
    @SerializedName("latitude") val latitude: Double?,
    @SerializedName("longitude") val longitude: Double?
)
