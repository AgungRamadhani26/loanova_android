# Loanova Android Project - Implementation Documentation

Dokumen ini menjelaskan secara sangat detail mengenai implementasi teknis dari aplikasi **Loanova Android**, mencakup arsitektur, pola desain, dan detail implementasi di setiap layer. Dokumen ini menyertakan kode sumber asli (copy-paste) untuk memberikan gambaran yang akurat tentang implementasi.

---

## 1. Arsitektur Proyek: Clean Architecture & MVVM

Aplikasi ini dibangun menggunakan prinsip **Clean Architecture** yang dibagi menjadi beberapa layer mandiri untuk memastikan *separation of concerns*, kemudahan testing, dan skalabilitas.

### Layer-Layer Utama:
1.  **Core Layer (`core/`)**: Infrastruktur dasar (DI, Navigation, Network).
2.  **Data Layer (`data/`)**: Implementasi Repository dan Data Source.
3.  **Domain Layer (`domain/`)**: Business Logic dan Use Cases (Pure Kotlin).
4.  **UI Layer (`ui/`)**: Tampilan (Compose) dan State Management (ViewModel).

---

## 2. Core Layer: Infrastruktur Dasar

Layer ini menyediakan pondasi teknis untuk aplikasi.

### A. Dependency Injection (Dagger Hilt)

**File: `core/di/NetworkModule.kt`**

Modul ini menyediakan dependensi jaringan menggunakan Retrofit, OkHttp, dan Chucker untuk debugging.
*   **ChuckerInterceptor**: Membantu memantau trafik HTTP di notifikasi HP.
*   **HttpLoggingInterceptor**: Logging request/response ke Logcat.
*   **Retrofit**: Dikonfigurasi dengan `GsonConverter` untuk parsing JSON otomatis.

```kotlin
package com.example.loanova_android.core.di

import android.content.Context
import com.chuckerteam.chucker.api.ChuckerCollector
import com.chuckerteam.chucker.api.ChuckerInterceptor
import com.chuckerteam.chucker.api.RetentionManager
import com.example.loanova_android.data.remote.api.AuthApi
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module                                          // Menandai kelas ini sebagai Dagger Module
@InstallIn(SingletonComponent::class)           // Memasang module ke SingletonComponent (app-level scope)
object NetworkModule {                          // Object singleton untuk menyediakan dependensi network

    @Provides                                    // Memberitahu Dagger bahwa method ini menyediakan dependency
    @Singleton                                   // Instance hanya dibuat satu kali selama lifecycle aplikasi
    fun provideChuckerCollector(@ApplicationContext context: Context): ChuckerCollector {
        return ChuckerCollector(                 // Membuat instance ChuckerCollector
            context = context,                   // Context aplikasi untuk akses resource
            showNotification = true,             // Menampilkan notifikasi saat ada activity network
            retentionPeriod = RetentionManager.Period.ONE_HOUR  // Menyimpan log selama 1 jam
        )
    }

    @Provides
    @Singleton
    fun provideChuckerInterceptor(
        @ApplicationContext context: Context,    // Inject context otomatis oleh Hilt
        chuckerCollector: ChuckerCollector       // Inject ChuckerCollector dari method sebelumnya
    ): ChuckerInterceptor {
        return ChuckerInterceptor.Builder(context)
            .collector(chuckerCollector)         // Menghubungkan ke collector untuk menyimpan data
            .maxContentLength(250_000L)          // Maksimal 250KB content body yang akan dilog
            .redactHeaders("Auth-Token", "Bearer")  // Menyembunyikan header sensitive dari log
            .alwaysReadResponseBody(true)        // Selalu baca response body untuk logging
            .build()                             // Membangun instance ChuckerInterceptor
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {  // Membuat HttpLoggingInterceptor dengan DSL apply
            level = HttpLoggingInterceptor.Level.BODY  // Log seluruh request & response termasuk body
        }
    }

    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().create()  // Membuat Gson instance untuk JSON parsing

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,  // Inject logging interceptor
        chuckerInterceptor: ChuckerInterceptor       // Inject chucker interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()            // Builder pattern untuk konfigurasi OkHttpClient
            .addInterceptor(loggingInterceptor)  // Menambahkan interceptor untuk logging ke Logcat
            .addInterceptor(chuckerInterceptor)  // Menambahkan interceptor untuk Chucker (debug UI)
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)  // Timeout koneksi 30 detik
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)     // Timeout baca response 30 detik
            .build()                             // Membangun OkHttpClient yang sudah dikonfigurasi
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, gson: Gson): Retrofit {
        return Retrofit.Builder()                         // Builder untuk membuat Retrofit instance
            .baseUrl("http://10.55.44.44:9091/")         // Base URL untuk semua endpoint API
            .client(okHttpClient)                         // Menggunakan OkHttpClient yang sudah dikonfigurasi
            .addConverterFactory(GsonConverterFactory.create(gson))  // Converter JSON ke object Kotlin
            .build()                                      // Membangun Retrofit instance
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)  // Membuat implementasi dari AuthApi interface otomatis
    }
}
```

#### Penjelasan Detail NetworkModule:

1. **Annotation @Module & @InstallIn**: Kedua annotation ini menandakan bahwa kelas ini adalah Dagger Hilt Module yang akan di-install pada `SingletonComponent`, artinya semua dependency yang disediakan akan bersifat singleton (satu instance untuk seluruh aplikasi).

2. **ChuckerCollector & ChuckerInterceptor**: Chucker adalah library debugging yang menampilkan HTTP traffic dalam notifikasi Android. Sangat berguna untuk development karena kita bisa melihat request/response tanpa perlu melihat Logcat.

3. **HttpLoggingInterceptor**: Mencatat semua HTTP request dan response ke Logcat dengan level `BODY` (termasuk isi body JSON).

4. **OkHttpClient**: HTTP client yang digunakan oleh Retrofit. Dikonfigurasi dengan:
   - Dua interceptor (logging & chucker)
   - Timeout 30 detik untuk koneksi dan pembacaan

5. **Retrofit**: Library untuk melakukan HTTP request dengan cara yang type-safe. Dikonfigurasi dengan:
   - Base URL server backend
   - OkHttpClient custom
   - Gson converter untuk parsing JSON

6. **Dependency Injection Flow**: 
   - Hilt otomatis memanggil `provideChuckerCollector` → `provideChuckerInterceptor` → `provideOkHttpClient` → `provideRetrofit` → `provideAuthApi`
   - Setiap method menerima parameter yang merupakan hasil dari method lain, membentuk dependency graph

**File: `core/di/RepositoryModule.kt`**

Modul ini mengikat (binding) interface repository ke implementasinya. Ini penting agar kita bisa menyuntikkan (inject) interface `IAuthRepository` ke UseCase, bukan implementasinya langsung.

```kotlin
package com.example.loanova_android.core.di

import com.example.loanova_android.data.repository.AuthRepositoryImpl
import com.example.loanova_android.domain.repository.IAuthRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module                                          // Menandai sebagai Dagger Module
@InstallIn(SingletonComponent::class)           // Scope: singleton untuk seluruh aplikasi
abstract class RepositoryModule {               // Abstract class karena menggunakan @Binds

    @Binds                                       // Mengikat interface ke implementasi (lebih efisien dari @Provides)
    @Singleton                                   // Hanya satu instance AuthRepositoryImpl di seluruh app
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl   // Parameter: implementasi konkret yang akan di-inject
    ): IAuthRepository                           // Return type: interface yang akan tersedia untuk di-inject
}
```

#### Penjelasan Detail RepositoryModule:

1. **@Binds vs @Provides**: `@Binds` lebih efisien daripada `@Provides` karena tidak perlu membuat fungsi dengan body. Dagger hanya perlu tahu bahwa `AuthRepositoryImpl` adalah implementasi dari `IAuthRepository`.

2. **Abstract Class**: Ketika menggunakan `@Binds`, class harus abstract karena Dagger akan generate implementasi secara otomatis.

3. **Dependency Inversion Principle**: Dengan pattern ini, `LoginUseCase` hanya bergantung pada interface `IAuthRepository`, bukan implementasi konkretnya. Ini membuat kode lebih testable dan flexible.

---

### B. Navigasi

**File: `core/navigation/Screen.kt`**

Mendefinisikan rute (URL-like path) untuk setiap layar menggunakan sealed class pattern.

```kotlin
package com.example.loanova_android.core.navigation

sealed class Screen(val route: String) {        // Sealed class: semua subclass harus dalam file yang sama
    object Home : Screen("home")                 // Object singleton untuk screen Home dengan route "home"
    object Login : Screen("login")               // Object singleton untuk screen Login dengan route "login"
    object Dashboard : Screen("dashboard/{username}") {  // Route dengan parameter dinamis {username}
        fun createRoute(username: String) = "dashboard/$username"  // Helper untuk membuat route dengan nilai actual
    }
}
```

#### Penjelasan Detail Screen:

1. **Sealed Class**: Membatasi hierarchy, sehingga semua kemungkinan Screen sudah didefinisikan di compile-time. Mencegah pembuatan Screen baru di luar file ini.

2. **Route dengan Parameter**: `"dashboard/{username}"` menggunakan sintaks Navigation Compose untuk parameter dinamis. Kurung kurawal `{}` menandakan placeholder.

3. **Helper Function `createRoute`**: Memudahkan pembuatan route dengan mengganti placeholder dengan nilai actual, misalnya `"dashboard/johndoe"`.

---

**File: `core/navigation/AppNavigation.kt`**

Konfigurasi `NavHost` yang mengatur perpindahan antar layar dan passing argumen.

```kotlin
package com.example.loanova_android.core.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.example.loanova_android.ui.features.auth.login.LoginScreen
import com.example.loanova_android.ui.features.home.HomeScreen
import com.example.loanova_android.ui.features.home.DashboardScreen

@Composable                                      // Function composable untuk UI
fun AppNavigation(navController: NavHostController) {  // Menerima NavController untuk kontrol navigasi
    NavHost(                                     // Container untuk semua destination navigasi
        navController = navController,           // Controller yang mengatur state navigasi
        startDestination = Screen.Home.route     // Screen pertama yang ditampilkan saat app dibuka
    ) {
        composable(Screen.Home.route) {          // Mendefinisikan destination untuk route "home"
            HomeScreen(
                onNavigateToLogin = {            // Callback lambda yang dipanggil saat user klik login
                    navController.navigate(Screen.Login.route)  // Navigate ke Login screen
                }
            )
        }
        
        composable(Screen.Login.route) {         // Destination untuk route "login"
            LoginScreen(
                onNavigateToDashboard = { username ->  // Callback dengan parameter username
                    navController.navigate(Screen.Dashboard.createRoute(username)) {  // Navigate ke Dashboard
                        popUpTo(Screen.Home.route) { inclusive = true }  // Hapus Home & Login dari backstack
                    }
                }
            )
        }
        
        composable(                              // Destination dengan argumen dinamis
            route = Screen.Dashboard.route,      // Route: "dashboard/{username}"
            arguments = listOf(navArgument("username") { type = NavType.StringType })  // Definisi argumen
        ) { backStackEntry ->                    // Lambda menerima NavBackStackEntry untuk akses argumen
            val username = backStackEntry.arguments?.getString("username") ?: ""  // Extract argumen username
            DashboardScreen(username = username)  // Pass username ke DashboardScreen
        }
    }
}
```

#### Penjelasan Detail AppNavigation:

1. **NavHost**: Container utama yang memegang semua destination (layar) dalam aplikasi. Mirip seperti routing di web app.

2. **startDestination**: Screen pertama yang akan ditampilkan. Dalam kasus ini `Screen.Home`.

3. **composable()**: Mendefinisikan sebuah destination. Parameter pertama adalah route string yang unik.

4. **Lambda Callback Pattern**: 
   - `onNavigateToLogin` dan `onNavigateToDashboard` adalah callbacks yang dikirim ke child composable
   - Child memanggil callback ini untuk trigger navigasi
   - Ini memisahkan logic navigasi dari UI component (separation of concerns)

5. **popUpTo dengan inclusive = true**: 
   - Setelah login sukses, kita tidak ingin user bisa back ke Login atau Home screen
   - `popUpTo(Screen.Home.route) { inclusive = true }` menghapus semua screen dari Home hingga sebelum Dashboard dari backstack
   - Ketika user press tombol back, app akan keluar, bukan kembali ke login

6. **Passing Arguments**: 
   - Route `"dashboard/{username}"` memiliki placeholder `{username}`
   - `navArgument("username")` mendefinisikan tipe data (String) dari argumen
   - `backStackEntry.arguments?.getString("username")` mengambil nilai argumen dari navigation state

---

## 3. Data Layer: Implementasi Data

### A. API Definition

**File: `data/remote/api/AuthApi.kt`**

Interface Retrofit yang mendefinisikan endpoint API menggunakan deklaratif annotations.

```kotlin
package com.example.loanova_android.data.remote.api

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {                              // Interface: Retrofit akan generate implementasinya
    @POST("api/auth/login")                      // HTTP POST ke endpoint "api/auth/login"
    suspend fun login(                           // suspend: function async yang bisa dipanggil di coroutine
        @Body request: LoginRequest              // @Body: object akan di-serialize jadi JSON request body
    ): Response<LoginResponse>                   // Return: wrapper yang berisi status code, headers, & body
}
```

### B. Access Data Source

**File: `data/remote/datasource/AuthRemoteDataSource.kt`**

Layer abstraksi tambahan di atas API client. Berguna untuk testing dan future expansion (misal: caching, retry logic).

```kotlin
package com.example.loanova_android.data.remote.datasource

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.LoginResponse
import com.example.loanova_android.data.remote.api.AuthApi
import retrofit2.Response
import javax.inject.Inject

class AuthRemoteDataSource @Inject constructor(  // @Inject: constructor injection oleh Hilt
    private val authApi: AuthApi                 // Dependency: AuthApi yang sudah di-provide di NetworkModule
) {
    suspend fun login(request: LoginRequest): Response<LoginResponse> {  // Fungsi wrapper
        return authApi.login(request)            // Delegasi langsung ke AuthApi
    }
}
```

#### Penjelasan Detail AuthRemoteDataSource:

1. **Constructor Injection**: `@Inject constructor` memberitahu Hilt untuk inject `AuthApi` saat membuat instance.

2. **Kenapa perlu layer ini?**:
   - **Testability**: Mudah di-mock untuk unit testing Repository
   - **Separation of Concerns**: Repository tidak berinteraksi langsung dengan library external (Retrofit)
   - **Future Flexibility**: Bisa tambahkan retry logic, request throttling, atau offline caching di sini

3. **Suspending Function Chain**: Karena `authApi.login()` adalah suspend function, maka wrapper ini juga harus suspend.

---

### C. Repository Implementation

**File: `data/repository/AuthRepositoryImpl.kt`**
Di sinilah logika pemetaan data terjadi. Repository ini:
1.  Memanggil API Login.
2.  Mengecek apakah respon sukses.
3.  Jika sukses, mengonversi data mentah (DTO) menjadi Business Model (`User`).
4.  Jika gagal, mem-parsing pesan error dari JSON error body agar terbaca oleh user ("Username atau password salah").

```kotlin
package com.example.loanova_android.data.repository

import com.example.loanova_android.data.model.dto.LoginRequest
import com.example.loanova_android.data.model.dto.LoginResponse
import com.example.loanova_android.data.remote.datasource.AuthRemoteDataSource
import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import com.google.gson.Gson
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val remoteDataSource: AuthRemoteDataSource,
    private val gson: Gson
) : IAuthRepository {

    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = remoteDataSource.login(LoginRequest(username, password))
            if (response.isSuccessful) {
                val body = response.body()
                if (body != null && body.success && body.data != null) {
                    Result.success(
                        User(
                            username = body.data.username ?: username,
                            roles = body.data.roles ?: emptyList(),
                            permissions = body.data.permissions ?: emptyList(),
                            accessToken = body.data.accessToken ?: "",
                            refreshToken = body.data.refreshToken ?: ""
                        )
                    )
                } else {
                    Result.failure(Exception(body?.message ?: "Gagal login"))
                }
            } else {
                val errorBody = response.errorBody()?.string()
                val errorResponse = gson.fromJson(errorBody, LoginResponse::class.java)
                
                val errorMessage = when {
                    errorResponse?.data?.errors != null -> {
                        errorResponse.data.errors.values.joinToString(", ")
                    }
                    errorResponse?.message != null -> errorResponse.message
                    else -> "Username atau password salah"
                }
                Result.failure(Exception(errorMessage))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

---

## 4. Domain Layer: Business Logic

Layer ini murni Kotlin, tidak ada dependensi Android.

### A. Business Model

**File: `domain/model/User.kt`**
Representasi user yang digunakan di seluruh aplikasi.

```kotlin
package com.example.loanova_android.domain.model

data class User(
    val username: String,
    val roles: List<String>,
    val permissions: List<String>,
    val accessToken: String,
    val refreshToken: String
)
```

### B. Repository Interface

**File: `domain/repository/IAuthRepository.kt`**
Kontrak/Janji yang harus dipenuhi oleh layer Data.

```kotlin
package com.example.loanova_android.domain.repository

import com.example.loanova_android.domain.model.User

interface IAuthRepository {
    suspend fun login(username: String, password: String): Result<User>
}
```

### C. Use Case

**File: `domain/usecase/auth/LoginUseCase.kt`**
Membungkus satu aksi bisnis spesifik. ViewModel memanggil ini, bukan Repository secara langsung.

```kotlin
package com.example.loanova_android.domain.usecase.auth

import com.example.loanova_android.domain.model.User
import com.example.loanova_android.domain.repository.IAuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: IAuthRepository
) {
    suspend fun execute(username: String, password: String): Result<User> {
        return repository.login(username, password)
    }
}
```

---

## 5. UI Layer: Presentation

Layer ini menggunakan Jetpack Compose untuk mendesain antarmuka yang modern dan reaktif.

### A. Theme & Branding

**File: `ui/theme/Color.kt`**
Warna-warna inti aplikasi.

```kotlin
package com.example.loanova_android.ui.theme

import androidx.compose.ui.graphics.Color

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)

val LoanovaBlue = Color(0xFF1D4ED8)
val LoanovaLightBlue = Color(0xFF3B82F6)
val LoanovaGold = Color(0xFFFBBF24)
val LoanovaBackground = Color(0xFFF8FAFC)
```

**File: `ui/theme/Theme.kt`**
Setup tema Material Design.

```kotlin
package com.example.loanova_android.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Purple80,
    secondary = PurpleGrey80,
    tertiary = Pink80
)

private val LightColorScheme = lightColorScheme(
    primary = Purple40,
    secondary = PurpleGrey40,
    tertiary = Pink40
)

@Composable
fun Loanova_androidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

### B. Login Feature

**File: `ui/features/auth/login/LoginState.kt`**
(Juga dikenal sebagai `LoginUiState`). Menyimpan status tampilan layar login.

```kotlin
package com.example.loanova_android.ui.features.auth.login

import com.example.loanova_android.domain.model.User

data class LoginUiState(
    val isLoading: Boolean = false,
    val success: User? = null,
    val error: String? = null,
)
```

**File: `ui/features/auth/login/LoginViewModel.kt`**
Pengelola logika tampilan. Memantau input user dan memanggil `LoginUseCase`. Hasilnya diupdate ke `LoginUiState`.

```kotlin
package com.example.loanova_android.ui.features.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.loanova_android.domain.usecase.auth.LoginUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState = _uiState.asStateFlow()

    fun login(username: String, password: String) {
        if (username.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(error = "Username dan password tidak boleh kosong") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            val result = loginUseCase.execute(username, password)
            
            result.onSuccess { user ->
                _uiState.update { it.copy(isLoading = false, success = user) }
            }.onFailure { error ->
                _uiState.update { it.copy(isLoading = false, error = error.message ?: "Login gagal") }
            }
        }
    }

    fun clearError() {
        _uiState.update { it.copy(error = null) }
    }
}
```

**File: `ui/features/auth/login/LoginScreen.kt`**
Layar utama Login. Fitur utama:
*   Animasi Logo berdenyut (`pulsing animation`) menggunakan `infiniteTransition`.
*   Input field dengan Toggle Password Visibility.
*   Loading Indicator saat login diproses.
*   Error message handling.

```kotlin
package com.example.loanova_android.ui.features.auth.login

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loanova_android.R
import com.example.loanova_android.ui.theme.LoanovaBlue
import com.example.loanova_android.ui.theme.LoanovaBackground
import com.example.loanova_android.ui.theme.Loanova_androidTheme

@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onNavigateToDashboard: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Handle navigation as a side-effect in the smart composable
    LaunchedEffect(uiState.success) {
        uiState.success?.let {
            onNavigateToDashboard(it.username)
        }
    }

    LoginScreenContent(
        uiState = uiState,
        onLoginClick = viewModel::login,
        onClearError = viewModel::clearError
    )
}

@Composable
fun LoginScreenContent(
    uiState: LoginUiState,
    onLoginClick: (String, String) -> Unit,
    onClearError: () -> Unit
) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val isPreview = LocalInspectionMode.current

    val scale by if (isPreview) {
        remember { mutableStateOf(1f) }
    } else {
        val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
        infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 1.08f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ), label = "scale"
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(LoanovaBackground)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .scale(scale),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.logo_nova),
                    contentDescription = "Loanova Logo",
                    modifier = Modifier.size(110.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "Loanova",
                fontSize = 32.sp,
                fontWeight = FontWeight.ExtraBold,
                color = LoanovaBlue
            )
            Text(
                text = "Solusi Finansial Masa Depan",
                fontSize = 14.sp,
                color = Color.Gray.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            if (uiState.error != null) {
                Text(
                    text = uiState.error,
                    color = MaterialTheme.colorScheme.error,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = TextAlign.Center
                )
            }

            OutlinedTextField(
                value = username,
                onValueChange = {
                    username = it
                    onClearError()
                },
                label = { Text("Username") },
                placeholder = { Text("Masukkan username Anda") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LoanovaBlue,
                    focusedLabelColor = LoanovaBlue,
                    cursorColor = LoanovaBlue
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    onClearError()
                },
                label = { Text("Password") },
                placeholder = { Text("Masukkan password Anda") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                trailingIcon = {
                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(imageVector = image, contentDescription = if (passwordVisible) "Sembunyikan password" else "Tampilkan password")
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = LoanovaBlue,
                    focusedLabelColor = LoanovaBlue,
                    cursorColor = LoanovaBlue
                ),
                enabled = !uiState.isLoading
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLoginClick(username, password) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = LoanovaBlue),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = "MASUK",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Belum punya akun?", color = Color.Gray, fontSize = 14.sp)
                TextButton(onClick = { /* TODO: Register */ }) {
                    Text(text = "Daftar Sekarang", color = LoanovaBlue, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
```

### C. Home & Dashboard

**File: `ui/features/home/DashboardScreen.kt`**
Layar sederhana setelah login berhasil.

```kotlin
package com.example.loanova_android.ui.features.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loanova_android.ui.theme.LoanovaBlue

@Composable
fun DashboardScreen(username: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Selamat Datang,", fontSize = 18.sp)
        Text(
            text = username,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = LoanovaBlue
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(text = "Ini adalah Dashboard Anda.")
    }
}
```

**File: `ui/features/home/HomeScreen.kt`**
Layar awal yang menampilkan fitur aplikasi dengan desain menarik (Gradient Hero Section, Icon Grid, Card List).

```kotlin
package com.example.loanova_android.ui.features.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.loanova_android.ui.theme.*

@Composable
fun HomeScreen(
    onNavigateToLogin: () -> Unit = {}
) {
    Scaffold(
        topBar = { HomeHeader() },
        containerColor = LoanovaBackground
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {
            item { HeroSection(onNavigateToLogin) }
            item { FeatureSection() }
            item { PlafondTitleSection() }
            item { PlafondListSection() }
            item { SecuritySection() }
            item { StepsSection() }
        }
    }
}
// ... (Lihat file asli untuk komponen sub-section seperti HomeHeader, HeroSection, dll)
```

---
*Dokumentasi ini dibuat secara otomatis dari source code proyek `loanova_android`.*
