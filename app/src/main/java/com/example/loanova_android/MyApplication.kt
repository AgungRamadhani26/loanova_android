package com.example.loanova_android

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

import coil.ImageLoader
import coil.ImageLoaderFactory
import javax.inject.Inject

@HiltAndroidApp
class MyApplication : Application(), ImageLoaderFactory, androidx.work.Configuration.Provider {

    @Inject
    lateinit var imageLoader: ImageLoader

    @Inject
    lateinit var workerFactory: androidx.hilt.work.HiltWorkerFactory

    override fun newImageLoader(): ImageLoader = imageLoader

    override val workManagerConfiguration: androidx.work.Configuration
        get() = androidx.work.Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

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
