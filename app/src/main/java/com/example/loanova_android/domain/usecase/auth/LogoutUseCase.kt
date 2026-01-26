package com.example.loanova_android.domain.usecase.auth

import com.example.loanova_android.core.common.Resource
import com.example.loanova_android.domain.repository.IAuthRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    /**
     * Menjalankan bisnis logic untuk Logout.
     * 
     * TANGGUNG JAWAB:
     * - Memanggil repository untuk komunikasi ke API dan hapus sesi lokal.
     * - Tidak peduli dengan UI state (itu tugas ViewModel).
     * 
     * WHY USECASE?
     * - Agar logic logout reusable (bisa dipanggil dari Home, Profile, atau Auto-Logout saat exp).
     * - Single Responsibility: Class ini HANYA tahu cara logout.
     */
    fun execute(): Flow<Resource<Boolean>> {
        return repository.logout()
    }
}
