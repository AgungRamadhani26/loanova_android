package com.example.loanova_android.data.repository

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.local.dao.ApplicationHistoryDao
import com.example.loanova_android.data.local.dao.LoanApplicationDao
import com.example.loanova_android.data.mapper.DataMappers
import com.example.loanova_android.data.model.dto.ApplicationHistoryResponse
import com.example.loanova_android.data.model.dto.LoanApplicationResponse
import com.example.loanova_android.data.model.dto.LoanApplicationRequest
import com.example.loanova_android.data.remote.api.LoanApplicationApi
import com.example.loanova_android.domain.repository.ILoanApplicationRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import javax.inject.Inject

/**
 * Repository implementation untuk Loan Application operations.
 * Menggunakan strategi offline-first untuk read operations:
 * 1. Tampilkan cache local jika ada
 * 2. Sync dengan network
 * 3. Update cache dan emit dari cache (single source of truth)
 */
class LoanApplicationRepository @Inject constructor(
    private val loanApplicationApi: LoanApplicationApi,
    private val loanApplicationDao: LoanApplicationDao,
    private val applicationHistoryDao: ApplicationHistoryDao
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
                if (body != null && body.success && body.data != null) {
                    // Success: Simpan ke cache local
                    val entity = DataMappers.mapLoanApplicationResponseToEntity(body.data)
                    loanApplicationDao.insert(entity)
                    emit(Resource.Success(body.data))
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
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get My Applications - OFFLINE-FIRST
     * 1. Emit Loading
     * 2. Check local cache, emit jika ada
     * 3. Fetch dari network
     * 4. Jika sukses: update DB, emit dari DB
     * 5. Jika error tapi ada cache: tampilkan cache
     * 6. Jika error dan tidak ada cache: emit Error
     */
    override suspend fun getMyApplications(): Flow<Resource<List<LoanApplicationResponse>>> = flow {
        emit(Resource.Loading())
        
        // 1. Check Local Cache
        var localData: List<com.example.loanova_android.data.local.entity.LoanApplicationEntity>? = null
        try {
            localData = loanApplicationDao.getAllApplications().firstOrNull()
            if (!localData.isNullOrEmpty()) {
                val cachedResponse = localData.map { DataMappers.mapLoanApplicationEntityToResponse(it) }
                emit(Resource.Success(cachedResponse, isFromCache = true))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // 2. Network Sync
        try {
            val response = loanApplicationApi.getMyApplications()
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val networkData = body.data ?: emptyList()
                    
                    // Update cache: clear and insert fresh data
                    loanApplicationDao.deleteAll()
                    val entities = networkData.map { DataMappers.mapLoanApplicationResponseToEntity(it) }
                    loanApplicationDao.insertAll(entities)
                    
                    // Emit from DB as single source of truth
                    emitAll(loanApplicationDao.getAllApplications().map { list ->
                        Resource.Success(list.map { DataMappers.mapLoanApplicationEntityToResponse(it) })
                    })
                } else {
                    // API error but have cache -> continue showing cache
                    if (!localData.isNullOrEmpty()) {
                        emitAll(loanApplicationDao.getAllApplications().map { list ->
                            Resource.Success(list.map { DataMappers.mapLoanApplicationEntityToResponse(it) }, isFromCache = true)
                        })
                    } else {
                        emit(Resource.Error(body?.message ?: "Gagal mengambil data pengajuan"))
                    }
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
                
                // HTTP error but have cache -> continue showing cache
                if (!localData.isNullOrEmpty()) {
                    emitAll(loanApplicationDao.getAllApplications().map { list ->
                        Resource.Success(list.map { DataMappers.mapLoanApplicationEntityToResponse(it) }, isFromCache = true)
                    })
                } else {
                    emit(Resource.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            // Network exception but have cache -> continue showing cache
            if (!localData.isNullOrEmpty()) {
                emitAll(loanApplicationDao.getAllApplications().map { list ->
                    Resource.Success(list.map { DataMappers.mapLoanApplicationEntityToResponse(it) }, isFromCache = true)
                })
            } else {
                emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
            }
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get Application Detail - OFFLINE-FIRST
     */
    override suspend fun getApplicationDetail(id: Long): Flow<Resource<LoanApplicationResponse>> = flow {
        emit(Resource.Loading())
        
        // 1. Check Local Cache
        var localData: com.example.loanova_android.data.local.entity.LoanApplicationEntity? = null
        try {
            localData = loanApplicationDao.getApplicationById(id).firstOrNull()
            if (localData != null) {
                emit(Resource.Success(DataMappers.mapLoanApplicationEntityToResponse(localData), isFromCache = true))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // 2. Network Sync
        try {
            val response = loanApplicationApi.getApplicationDetail(id)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    // Update cache
                    val entity = DataMappers.mapLoanApplicationResponseToEntity(body.data)
                    loanApplicationDao.insert(entity)
                    
                    // Emit from DB as single source of truth
                    emitAll(loanApplicationDao.getApplicationById(id).map { 
                        if (it != null) Resource.Success(DataMappers.mapLoanApplicationEntityToResponse(it))
                        else Resource.Loading()
                    })
                } else {
                    if (localData != null) {
                        emitAll(loanApplicationDao.getApplicationById(id).map { 
                            if (it != null) Resource.Success(DataMappers.mapLoanApplicationEntityToResponse(it), isFromCache = true)
                            else Resource.Loading()
                        })
                    } else {
                        emit(Resource.Error(body?.message ?: "Gagal mengambil detail pengajuan"))
                    }
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
                
                if (localData != null) {
                    emitAll(loanApplicationDao.getApplicationById(id).map { 
                        if (it != null) Resource.Success(DataMappers.mapLoanApplicationEntityToResponse(it), isFromCache = true)
                        else Resource.Loading()
                    })
                } else {
                    emit(Resource.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            if (localData != null) {
                emitAll(loanApplicationDao.getApplicationById(id).map { 
                    if (it != null) Resource.Success(DataMappers.mapLoanApplicationEntityToResponse(it), isFromCache = true)
                    else Resource.Loading()
                })
            } else {
                emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
            }
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Get Application History - OFFLINE-FIRST
     */
    override suspend fun getApplicationHistory(id: Long): Flow<Resource<List<ApplicationHistoryResponse>>> = flow {
        emit(Resource.Loading())
        
        // 1. Check Local Cache
        var localData: List<com.example.loanova_android.data.local.entity.ApplicationHistoryEntity>? = null
        try {
            localData = applicationHistoryDao.getHistoryByLoanId(id).firstOrNull()
            if (!localData.isNullOrEmpty()) {
                val cachedResponse = localData.map { DataMappers.mapApplicationHistoryEntityToResponse(it) }
                emit(Resource.Success(cachedResponse, isFromCache = true))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        
        // 2. Network Sync
        try {
            val response = loanApplicationApi.getApplicationHistory(id)
            
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success) {
                    val networkData = body.data ?: emptyList()
                    
                    // Update cache: clear for this loan and insert fresh
                    applicationHistoryDao.deleteByLoanId(id)
                    val entities = networkData.map { DataMappers.mapApplicationHistoryResponseToEntity(it) }
                    applicationHistoryDao.insertAll(entities)
                    
                    // Emit from DB
                    emitAll(applicationHistoryDao.getHistoryByLoanId(id).map { list ->
                        Resource.Success(list.map { DataMappers.mapApplicationHistoryEntityToResponse(it) })
                    })
                } else {
                    if (!localData.isNullOrEmpty()) {
                        emitAll(applicationHistoryDao.getHistoryByLoanId(id).map { list ->
                            Resource.Success(list.map { DataMappers.mapApplicationHistoryEntityToResponse(it) }, isFromCache = true)
                        })
                    } else {
                        emit(Resource.Error(body?.message ?: "Gagal mengambil riwayat pengajuan"))
                    }
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorMessage = try {
                    val gson = com.google.gson.Gson()
                    val errorResponse = gson.fromJson(errorBody, com.example.loanova_android.core.base.ApiResponse::class.java)
                    errorResponse.message
                } catch (e: Exception) {
                    "Gagal mengambil riwayat pengajuan"
                }
                
                if (!localData.isNullOrEmpty()) {
                    emitAll(applicationHistoryDao.getHistoryByLoanId(id).map { list ->
                        Resource.Success(list.map { DataMappers.mapApplicationHistoryEntityToResponse(it) }, isFromCache = true)
                    })
                } else {
                    emit(Resource.Error(errorMessage))
                }
            }
        } catch (e: Exception) {
            if (!localData.isNullOrEmpty()) {
                emitAll(applicationHistoryDao.getHistoryByLoanId(id).map { list ->
                    Resource.Success(list.map { DataMappers.mapApplicationHistoryEntityToResponse(it) }, isFromCache = true)
                })
            } else {
                emit(Resource.Error(e.message ?: "Terjadi kesalahan jaringan"))
            }
        }
    }.flowOn(Dispatchers.IO)
}
