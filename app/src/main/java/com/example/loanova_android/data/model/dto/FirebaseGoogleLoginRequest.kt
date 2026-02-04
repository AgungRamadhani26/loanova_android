package com.example.loanova_android.data.model.dto

import com.google.gson.annotations.SerializedName

/**
 * Request DTO untuk login menggunakan Google via Firebase Authentication.
 * 
 * @param idToken Firebase ID Token yang didapat setelah Google Sign-In
 * @param fcmToken FCM Token untuk push notification (optional)
 */
data class FirebaseGoogleLoginRequest(
    @SerializedName("idToken")
    val idToken: String,
    
    @SerializedName("fcmToken")
    val fcmToken: String? = null
)
