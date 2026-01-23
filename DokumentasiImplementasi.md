# Dokumentasi Implementasi dan Perbaikan Proyek Loanova

Dokumen ini menjelaskan secara rinci semua langkah implementasi, identifikasi masalah, dan perbaikan yang telah kita lakukan pada proyek aplikasi Android Loanova. Tujuannya adalah untuk memberikan pemahaman yang jelas tentang alur kerja dan solusi teknis yang diterapkan.

---

## 1. Implementasi Awal: `LoginScreen.kt`

Fokus awal kita adalah membangun dan menampilkan `LoginScreen` menggunakan Jetpack Compose. File ini dirancang untuk menangani input pengguna untuk login dan menampilkan elemen UI dasar.

### Elemen UI dan Fungsionalitas:
- **Struktur Utama**: Menggunakan `Column` untuk menyusun elemen secara vertikal di tengah layar (`verticalArrangement = Arrangement.Center`, `horizontalAlignment = Alignment.CenterHorizontally`).
- **Input Fields**: Dua `OutlinedTextField` digunakan untuk input `username` dan `password`. State-nya dikelola menggunakan `remember { mutableStateOf("") }`.
- **Password Visibility**: Fungsionalitas untuk menampilkan/menyembunyikan password diimplementasikan menggunakan `IconButton` dengan `Icons.Filled.Visibility` dan `Icons.Filled.VisibilityOff`. State `passwordVisible` mengontrol transformasi visual dari `OutlinedTextField`.
- **Logo dengan Animasi**: Sebuah `Image` yang menampilkan logo aplikasi (`R.drawable.logo_nova`) diberi animasi *scaling* (memperbesar dan memperkecil) secara terus-menerus. Ini dicapai dengan `rememberInfiniteTransition` dan `animateFloat`, memberikan efek visual yang dinamis pada logo.
- **Tombol Masuk**: Sebuah `Button` yang akan memicu logika login (saat itu masih berupa `TODO`).

---

## 2. Debugging Masalah Pratinjau (Preview) dan Rendering

Setelah membuat layout dasar, kita menghadapi beberapa tantangan untuk membuat pratinjau Compose berfungsi dengan benar. Ini adalah masalah umum dalam pengembangan Compose dan memerlukan beberapa langkah debugging.

### Langkah 1: Menambahkan Pratinjau Awal
**Masalah**: `LoginScreen` tidak dapat dilihat di panel pratinjau Android Studio.
**Solusi**: Saya menambahkan fungsi Composable baru yang dianotasi dengan `@Preview`.

```kotlin
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    LoginScreen()
}
```

### Langkah 2: Mengatasi "Render Problem" Pertama (Theme)
**Masalah**: Pratinjau menampilkan "Render Problem" setelah anotasi `@Preview` ditambahkan.
**Penyebab**: Komponen Material (seperti `OutlinedTextField` dan `Button`) memerlukan `MaterialTheme` untuk mengambil warna, tipografi, dan bentuk. Pratinjau yang tidak dibungkus dalam tema akan gagal merender komponen-komponen ini.
**Solusi**: Saya membungkus `LoginScreen()` di dalam tema aplikasi, yaitu `Loanova_androidTheme`, di dalam fungsi pratinjau.

```kotlin
@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    Loanova_androidTheme {
        LoginScreen(onNavigateToDashboard = { /* ... */ })
    }
}
```

### Langkah 3: Mengatasi "Render Problem" Kedua (Animasi)
**Masalah**: Pratinjau masih menampilkan "Render Problem" meskipun sudah dibungkus tema.
**Penyebab**: Animasi tak terbatas (`rememberInfiniteTransition`) sering kali menjadi penyebab masalah di mode pratinjau. Lingkungan pratinjau tidak dirancang untuk menjalankan animasi yang berjalan selamanya dan dapat menyebabkan crash atau kegagalan render.
**Solusi**: Saya menggunakan `LocalInspectionMode.current` untuk mendeteksi apakah Composable sedang dirender dalam mode pratinjau. Jika ya, animasi dinonaktifkan dan nilai skala statis (1f) digunakan. Jika tidak (saat aplikasi berjalan di perangkat/emulator), animasi akan dijalankan seperti biasa.

```kotlin
val isPreview = LocalInspectionMode.current

val scale by if (isPreview) {
    remember { mutableStateOf(1f) } // Non-aktifkan animasi di pratinjau
} else {
    // Jalankan animasi seperti biasa
    val infiniteTransition = rememberInfiniteTransition(label = "logo_anim")
    infiniteTransition.animateFloat(/*...*/) 
}
```

---

## 3. Debugging Crash Aplikasi Saat Runtime (`XmlPullParserException`)

Setelah memperbaiki pratinjau, aplikasi mengalami crash saat dijalankan di perangkat/emulator. Log menunjukkan `FATAL EXCEPTION` yang disebabkan oleh `XmlPullParserException`.

### Analisis Kesalahan:
Logcat menunjukkan kesalahan:
`org.xmlpull.v1.XmlPullParserException: Binary XML file line #6 <VectorGraphic> tag requires viewportWidth > 0`

Ini secara langsung menunjuk ke masalah pada file `logo_nova.xml` saat di-parse oleh sistem Android. Meskipun atribut `viewportWidth` dan `viewportHeight` sudah ada dan diatur ke nilai yang benar ("200dp") di file XML, kesalahan ini masih muncul. Ini menandakan kemungkinan salah satu dari dua hal:
1.  **Cache Build yang Rusak**: Gradle atau Android Studio mungkin masih menggunakan versi file yang lama atau yang sudah ter-compile secara keliru dari cache.
2.  **Masalah Tersembunyi pada XML**: Ada elemen dalam XML (seperti `<gradient>` yang kita duga sebelumnya) yang, meskipun valid, tidak dapat di-parse dengan benar oleh lingkungan runtime tertentu.

### Langkah-langkah Perbaikan:
1.  **Menyederhanakan Drawable**: Untuk mengisolasi masalah, saya mengganti konten `logo_nova.xml` dengan bentuk persegi panjang sederhana. Ini adalah cara cepat untuk memvalidasi apakah masalahnya memang berasal dari kompleksitas file tersebut.
2.  **Membersihkan Build Cache**: Ini adalah langkah kunci. Menjalankan perintah `gradlew clean` memaksa Gradle untuk menghapus semua output build sebelumnya (termasuk resource biner yang ter-compile). Ini memastikan bahwa pada build berikutnya, semua file sumber (termasuk `logo_nova.xml`) akan dibaca dan di-compile ulang dari awal.
3.  **Membangun Ulang Proyek**: Setelah membersihkan cache, saya menjalankan `gradlew :app:assembleDebug` untuk membangun ulang proyek. Proses ini membuat ulang semua file biner dari sumber yang sudah bersih.

---

## 4. Memperbaiki Kesalahan Kompilasi (`No value passed for parameter`)

Setelah membersihkan build, muncul kesalahan kompilasi baru.

### Analisis Kesalahan:
Compiler melaporkan:
`No value passed for parameter 'onNavigateToDashboard'.`

Ini terjadi di `MainActivity.kt` di dalam fungsi `LoginPreview`. Setelah kita melakukan banyak perubahan, definisi fungsi `LoginScreen` sekarang memerlukan parameter `onNavigateToDashboard` yang bertipe `(String) -> Unit`. Fungsi pratinjau kita belum diperbarui untuk menyediakan parameter ini.

### Solusi:
Saya memperbarui panggilan `LoginScreen` di dalam `LoginPreview` untuk menyediakan lambda kosong yang cocok dengan tipe parameter yang dibutuhkan.

```kotlin
// Di MainActivity.kt
@Preview(showBackground = true)
@Composable
fun LoginPreview() {
    Loanova_androidTheme {
        // Menyediakan lambda yang cocok dengan (String) -> Unit
        LoginScreen(onNavigateToDashboard = { _ -> })
    }
}
```
Simbol `_` digunakan untuk menandakan bahwa kita menerima parameter (dalam hal ini, sebuah `String`), tetapi kita tidak akan menggunakannya di dalam lambda ini.

---

## 5. Konfigurasi Dependency Injection dengan Hilt (`NetworkModule.kt`)

Terakhir, kita melihat file `NetworkModule.kt`, yang bertanggung jawab untuk menyediakan dependensi terkait jaringan (Retrofit, OkHttpClient, dll.) ke seluruh aplikasi menggunakan Hilt.

- **`@Module` dan `@InstallIn(SingletonComponent::class)`**: Anotasi ini memberitahu Hilt bahwa ini adalah modul yang menyediakan dependensi dengan siklus hidup *singleton* (hanya ada satu instance untuk seluruh aplikasi).
- **`provideGson()`**: Menyediakan instance `Gson` untuk mengubah objek Kotlin menjadi JSON dan sebaliknya.
- **`provideOkHttpClient()`**: Menyediakan `OkHttpClient`. Di sini, kita menambahkan `HttpLoggingInterceptor` yang sangat berguna untuk debugging. Interceptor ini akan mencetak semua detail request dan response jaringan (URL, header, body) ke Logcat, memudahkan kita untuk melacak masalah API.
- **`provideRetrofit()`**: Menyediakan instance `Retrofit` utama. Ini mengonfigurasi:
  - `baseUrl`: Alamat dasar dari API server.
  - `client`: Menggunakan `OkHttpClient` yang sudah kita konfigurasi.
  - `addConverterFactory`: Menggunakan `GsonConverterFactory` untuk menangani JSON.
- **`provideAuthApi()`**: Menggunakan instance Retrofit untuk membuat implementasi konkret dari interface `AuthApi`. Hilt kemudian dapat menyuntikkan `AuthApi` ini ke dalam ViewModel atau repositori yang membutuhkannya.

Dengan konfigurasi ini, setiap kali kita memerlukan `AuthApi` di bagian lain dari aplikasi, kita hanya perlu meminta Hilt untuk menyuntikkannya, dan Hilt akan secara otomatis menangani pembuatan Retrofit, OkHttpClient, dan semua dependensi lainnya.
