package com.example.loanova_android.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity untuk menyimpan Loan Application di local database.
 * Digunakan untuk offline-first support pada fitur My Loans.
 */
@Entity(tableName = "loan_application_entity")
data class LoanApplicationEntity(
    @PrimaryKey
    val id: Long,
    val userId: Long,
    val username: String,
    val branchId: Long,
    val branchCode: String,
    val plafondId: Long,
    val plafondName: String,
    val amount: String, // BigDecimal stored as String for precision
    val tenor: Int,
    val interestRateSnapshot: String?, // BigDecimal stored as String
    val status: String,
    val submittedAt: String,
    
    // Snapshot data
    val fullNameSnapshot: String,
    val phoneNumberSnapshot: String,
    val userAddressSnapshot: String,
    val nikSnapshot: String,
    val birthDateSnapshot: String?,
    val npwpNumberSnapshot: String?,
    
    // Pekerjaan
    val occupation: String,
    val companyName: String?,
    
    // Keuangan
    val rekeningNumber: String,
    
    // Dokumen
    val ktpPhotoSnapshot: String?,
    val npwpPhotoSnapshot: String?,
    val savingBookCover: String,
    val payslipPhoto: String,
    
    // Lokasi
    val latitude: Double?,
    val longitude: Double?,
    
    // Cache metadata
    val lastSyncedAt: Long = System.currentTimeMillis()
)
