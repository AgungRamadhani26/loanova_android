package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import com.example.loanova_android.data.model.dto.LoanApplicationRequest
import com.example.loanova_android.data.remote.api.LoanApplicationApi
import com.example.loanova_android.domain.repository.ILoanApplicationRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

/**
 * Repository implementation untuk Loan Application operations.
 */
class LoanApplicationRepository @Inject constructor(
    private val loanApplicationApi: LoanApplicationApi
) : ILoanApplicationRepository {
    
    override suspend fun submitLoanApplication(
        request: LoanApplicationRequest
    ): Flow<Resource<LoanApplicationResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            // Build multipart request body
            val requestBody = MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("branchId", request.branchId.toString())
                .addFormDataPart("amount", request.amount)
                .addFormDataPart("tenor", request.tenor.toString())
                .addFormDataPart("occupation", request.occupation)
                .addFormDataPart("rekeningNumber", request.rekeningNumber)
                .addFormDataPart("latitude", request.latitude.toString())
                .addFormDataPart("longitude", request.longitude.toString())
                .apply {
                    // Optional field
                    if (!request.companyName.isNullOrBlank()) {
                        addFormDataPart("companyName", request.companyName)
                    }
                    
                    // File uploads
                    val savingBookBody = request.savingBookCover.asRequestBody("image/*".toMediaTypeOrNull())
                    addFormDataPart("savingBookCover", request.savingBookCover.name, savingBookBody)
                    
                    val payslipBody = request.payslipPhoto.asRequestBody("image/*".toMediaTypeOrNull())
                    addFormDataPart("payslipPhoto", request.payslipPhoto.name, payslipBody)
                }
                .build()
            
            val response = loanApplicationApi.submitLoanApplication(requestBody)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengajukan pinjaman"))
                }
            } else {
                // Parse error body for validation errors
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, com.example.loanova_android.core.base.ApiResponse::class.java)
                    errorResponse.message
                } catch (e: Exception) {
                    "Gagal mengajukan pinjaman"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
        }
    }
    
    override suspend fun getMyApplications(): Flow<Resource<List<LoanApplicationResponse>>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = loanApplicationApi.getMyApplications()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data ?: emptyList()))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil data pengajuan"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, com.example.loanova_android.core.base.ApiResponse::class.java)
                    errorResponse.message
                } catch (e: Exception) {
                    "Gagal mengambil data pengajuan"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
        }
    }
    
    override suspend fun getApplicationDetail(id: Long): Flow<Resource<LoanApplicationResponse>> = flow {
        emit(Resource.Loading())
        
        try {
            val response = loanApplicationApi.getApplicationDetail(id)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    emit(Resource.Success(body.data!!))
                } else {
                    emit(Resource.Error(body?.message ?: "Gagal mengambil detail pengajuan"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, com.example.loanova_android.core.base.ApiResponse::class.java)
                    errorResponse.message
                } catch (e: Exception) {
                    "Gagal mengambil detail pengajuan"
                }
                emit(Resource.Error(errorMessage))
            }
        } catch (e: Exception) {
            emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
        }
    }
}
