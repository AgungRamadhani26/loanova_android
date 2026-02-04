package com.example.loanova_android.data.remote.datasource

import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/**
 * DataSource untuk Firebase Authentication operations.
 * 
 * Responsibility:
 * - Abstraksi untuk Firebase Auth operations (Google Sign-In)
 * - Memisahkan Firebase logic dari UI layer
 * - Repository depend ke DataSource ini untuk operasi Firebase
 */
@Singleton
class FirebaseAuthDataSource @Inject constructor() {
    
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance()
    
    /**
     * Sign in to Firebase using Google ID Token.
     * 
     * @param googleIdToken Token dari Google Credential Manager
     * @return Result<AuthResult> dari Firebase (wrapped untuk error handling)
     */
    suspend fun signInWithGoogle(googleIdToken: String): Result<AuthResult> {
        return try {
            val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
            val authResult = firebaseAuth.signInWithCredential(credential).await()
            Result.success(authResult)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get Firebase ID Token for current user.
     * Token ini yang akan dikirim ke backend untuk verifikasi.
     * 
     * @param forceRefresh True untuk memaksa refresh token
     * @return Result<String?> Firebase ID Token string atau null jika tidak ada user
     */
    suspend fun getFirebaseIdToken(forceRefresh: Boolean = true): Result<String?> {
        return try {
            val currentUser = firebaseAuth.currentUser 
                ?: return Result.failure(Exception("No user signed in"))
            val token = currentUser.getIdToken(forceRefresh).await().token
            Result.success(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    
    /**
     * Get current Firebase user.
     */
    fun getCurrentUser(): FirebaseUser? {
        return firebaseAuth.currentUser
    }
    
    /**
     * Sign out from Firebase.
     */
    fun signOut() {
        firebaseAuth.signOut()
    }
}
