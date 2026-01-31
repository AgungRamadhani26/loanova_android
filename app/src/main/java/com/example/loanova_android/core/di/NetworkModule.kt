package com.example.loanova_android.core.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.example.loanova_android.data.remote.api.AuthApi
import com.example.loanova_android.data.remote.api.BranchApi
import com.example.loanova_android.data.remote.api.LoanApplicationApi
import com.example.loanova_android.data.remote.api.PlafondApi
import com.example.loanova_android.data.remote.api.UserProfileApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideChuckerCollector(@ApplicationContext context: Context): ChuckerCollector {
        return ChuckerCollector(
            context = context,
            showNotification = true,
            retentionPeriod = RetentionManager.Period.ONE_HOUR
        )
    }

    @Provides
    @Singleton
    fun provideChuckerInterceptor(
        @ApplicationContext context: Context,
        chuckerCollector: ChuckerCollector
    ): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .collector(chuckerCollector)
            .maxContentLength(250_000L)
            .redactHeaders("Auth-Token", "Bearer")
            .alwaysReadResponseBody(true)
            .build()
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }



    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        chuckerInterceptor: ChuckerInterceptor,
        authInterceptor: com.example.loanova_android.data.remote.AuthInterceptor,
        tokenAuthenticator: com.example.loanova_android.data.remote.TokenAuthenticator
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(chuckerInterceptor)
            .addInterceptor(authInterceptor)
            .authenticator(tokenAuthenticator)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()
            .baseUrl(com.example.loanova_android.BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Provides
    @Singleton
    fun providePlafondApi(retrofit: Retrofit): PlafondApi {
        return retrofit.create(PlafondApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserProfileApi(retrofit: Retrofit): UserProfileApi {
        return retrofit.create(UserProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideUserPlafondApi(retrofit: Retrofit): com.example.loanova_android.data.remote.api.UserPlafondApi {
        return retrofit.create(com.example.loanova_android.data.remote.api.UserPlafondApi::class.java)
    }

    @Provides
    @Singleton
    fun provideBranchApi(retrofit: Retrofit): BranchApi {
        return retrofit.create(BranchApi::class.java)
    }

    @Provides
    @Singleton
    fun provideLoanApplicationApi(retrofit: Retrofit): LoanApplicationApi {
        return retrofit.create(LoanApplicationApi::class.java)
    }

    @Provides
    @Singleton
    fun provideImageLoader(
        @ApplicationContext context: Context,
        okHttpClient: OkHttpClient
    ): coil.ImageLoader {
        return coil.ImageLoader.Builder(context)
            .okHttpClient(okHttpClient)
            .diskCache {
                coil.disk.DiskCache.Builder()
                    .directory(context.cacheDir.resolve("image_cache"))
                    .maxSizePercent(0.02) // 2% of disk space
                    .build()
            }
            .respectCacheHeaders(false) // Cache images even if headers say otherwise (for offline support)
            .build()
    }
}

