package com.example.loanova_android.core.common

/**
 * Generic wrapper class untuk state management.
 * Mendukung Success, Error, dan Loading states.
 * 
 * @param isFromCache Menandakan apakah data berasal dari cache local (offline-first)
 */
sealed class Resource<T>(val data: T? = null, val message: String? = null, val isFromCache: Boolean = false) {
    class Success<T>(data: T, isFromCache: Boolean = false) : Resource<T>(data, isFromCache = isFromCache)
    class Error<T>(message: String, data: T? = null) : Resource<T>(data, message)
    class Loading<T>(val isLoading: Boolean = true) : Resource<T>(null)
}
