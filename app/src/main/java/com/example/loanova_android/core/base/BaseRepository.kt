package com.example.loanova_android.core.base

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.data.model.dto.ValidationErrorData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import retrofit2.Response

/**
 * Base Repository for centralized error handling.
 * Reduces code duplication across repositories.
 */
abstract class BaseRepository(private val gson: Gson) {

    /**
     * Handles API Response and returns a Resource.
     * @param response Retrofit Response object
     * @param onSuccess Callback if success, returns the data to be emitted (allows cache saving etc)
     */
    protected suspend fun <T, R> handleApiResponse(
        response: Response<ApiResponse<T>>,
        onSuccess: suspend (T) -> R
    ): Resource<R> {
        try {
            val body = response.body()
            if (response.isSuccessful && body != null) {
                if (body.success && body.data != null) {
                    val result = onSuccess(body.data)
                    return Resource.Success(result)
                } else {
                    return Resource.Error(body.message)
                }
            } else {
                return parseError(response)
            }
        } catch (e: Exception) {
            return Resource.Error(e.message ?: "Unknown Error")
        }
    }

    protected fun <T, R> parseError(response: Response<T>): Resource.Error<R> {
        val errorBody = response.errorBody()?.string()
        if (errorBody != null) {
            try {
                // Try parsing as standard API Response
                val type = object : TypeToken<ApiResponse<ValidationErrorData>>() {}.type
                val errorResponse: ApiResponse<ValidationErrorData> = gson.fromJson(errorBody, type)

                if (errorResponse.data?.errors != null && errorResponse.data.errors.isNotEmpty()) {
                    val errorsJson = gson.toJson(errorResponse.data.errors)
                    // Standardized format: VALIDATION_ERROR||Message||JSON_ERRORS
                    val backendMessage = if (errorResponse.message.isNullOrBlank()) response.message() else errorResponse.message
                    return Resource.Error("VALIDATION_ERROR||$backendMessage||$errorsJson")
                } else {
                    val msg = if (errorResponse.message.isNotEmpty()) errorResponse.message else response.message()
                    return Resource.Error(msg)
                }
            } catch (e: Exception) {
                // Failed to parse as JSON or different format
                return Resource.Error(response.message())
            }
        }
        return Resource.Error(response.message())
    }
}
