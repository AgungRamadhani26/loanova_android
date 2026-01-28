package com.example.loanova_android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import coil.ImageLoader
import coil.ImageLoaderFactory
import javax.inject.Inject
import okhttp3.OkHttpClient

@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory {

    @Inject
    lateinit var okHttpClient: OkHttpClient

    override fun newImageLoader(): ImageLoader {
        return ImageLoader.Builder(this)
            .okHttpClient(okHttpClient)
            .crossfade(true)
            .build()
    }

    override fun onCreate() {
        super.onCreate()
        try {
            if (com.google.firebase.FirebaseApp.getApps(this).isEmpty()) {
                com.google.firebase.FirebaseApp.initializeApp(this)
                android.util.Log.d("MyApplication", "Firebase initialized manually")
            }
        } catch (e: Exception) {
            android.util.Log.e("MyApplication", "Firebase initialization failed", e)
        }
    }
}
