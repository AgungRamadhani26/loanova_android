# Dokumentasi Implementasi Offline-First

## Daftar Isi

1. [Overview](#overview)
2. [Arsitektur](#arsitektur)
3. [Fitur yang Mendukung Offline-First](#fitur-yang-mendukung-offline-first)
4. [Struktur Database](#struktur-database)
5. [Flow Data](#flow-data)
6. [Testing Guide](#testing-guide)
7. [Troubleshooting](#troubleshooting)

---

## Overview

Aplikasi LoanNova Android mengimplementasikan strategi **offline-first** untuk operasi READ, yang memungkinkan pengguna tetap dapat mengakses data meskipun tidak ada koneksi internet. Data disimpan di local database (Room) dan digunakan sebagai **single source of truth**.

### Prinsip Dasar

- **Cache First**: Tampilkan data dari cache local terlebih dahulu
- **Background Sync**: Sync dengan server di background
- **Single Source of Truth**: Database local adalah satu-satunya sumber data untuk UI
- **Graceful Degradation**: Tetap berfungsi meski offline dengan data yang sudah di-cache

---

## Arsitektur

```
┌─────────────┐      ┌────────────────┐      ┌─────────────┐
│   UI Layer  │ ←──→ │   Repository   │ ←──→ │  Data Layer │
│  (Compose)  │      │ (Offline-First)│      │             │
└─────────────┘      └────────────────┘      └─────────────┘
                              │                     │
                              ↓                     ↓
                     ┌────────────────┐      ┌─────────────┐
                     │  Room Database │      │  Remote API │
                     │  (Local Cache) │      │   (Network) │
                     └────────────────┘      └─────────────┘
```

### Components

| Component    | File                           | Deskripsi                               |
| ------------ | ------------------------------ | --------------------------------------- |
| AppDatabase  | `core/database/AppDatabase.kt` | Room database dengan semua entities     |
| DAOs         | `data/local/dao/*.kt`          | Data Access Objects untuk setiap entity |
| Entities     | `data/local/entity/*.kt`       | Room entities untuk local storage       |
| Mappers      | `data/mapper/DataMappers.kt`   | Mapping Entity ↔ Response               |
| Repositories | `data/repository/*.kt`         | Implementasi offline-first logic        |

---

## Fitur yang Mendukung Offline-First

### 1. User Profile (`UserProfileRepositoryImpl`)

- **Entity**: `UserProfileEntity`
- **DAO**: `UserDao`
- **Operasi**: `getMyProfile()`

### 2. Plafond List (`PlafondRepositoryImpl`)

- **Entity**: `PlafondEntity`
- **DAO**: `PlafondDao`
- **Operasi**: `getAllPlafonds()`

### 3. My Loan Applications (`LoanApplicationRepository`) ✨ NEW

- **Entity**: `LoanApplicationEntity`
- **DAO**: `LoanApplicationDao`
- **Operasi**: `getMyApplications()`

### 4. Loan Application Detail (`LoanApplicationRepository`) ✨ NEW

- **Entity**: `LoanApplicationEntity`
- **DAO**: `LoanApplicationDao`
- **Operasi**: `getApplicationDetail(id)`

### 5. Application History (`LoanApplicationRepository`) ✨ NEW

- **Entity**: `ApplicationHistoryEntity`
- **DAO**: `ApplicationHistoryDao`
- **Operasi**: `getApplicationHistory(loanId)`

### 6. Branch List (`BranchRepository`) ✨ NEW

- **Entity**: `BranchEntity`
- **DAO**: `BranchDao`
- **Operasi**: `getAllBranches()`

---

## Struktur Database

### Database Version: 3

### Entities

#### LoanApplicationEntity

```kotlin
@Entity(tableName = "loan_application_entity")
data class LoanApplicationEntity(
    @PrimaryKey val id: Long,
    val userId: Long,
    val username: String,
    val branchId: Long,
    val branchCode: String,
    val plafondId: Long,
    val plafondName: String,
    val amount: String,  // BigDecimal as String
    val tenor: Int,
    val interestRateSnapshot: String?,
    val status: String,
    val submittedAt: String,
    // ... snapshot fields
    val lastSyncedAt: Long
)
```

#### ApplicationHistoryEntity

```kotlin
@Entity(tableName = "application_history_entity")
data class ApplicationHistoryEntity(
    @PrimaryKey val id: Long,
    val loanApplicationId: Long,
    val actionByUserId: Long,
    val actionByUsername: String,
    val actionByRole: String,
    val status: String,
    val comment: String?,
    val createdAt: String,
    val lastSyncedAt: Long
)
```

#### BranchEntity

```kotlin
@Entity(tableName = "branch_entity")
data class BranchEntity(
    @PrimaryKey val id: Long,
    val branchCode: String,
    val branchName: String,
    val branchAddress: String,
    val lastSyncedAt: Long
)
```

---

## Flow Data

### Read Operation Flow (Offline-First)

```
1. UI Request Data
        ↓
2. Repository: emit(Loading)
        ↓
3. Check Local Cache (Room)
   ├── Cache exists → emit(Success(data, isFromCache=true))
   └── No cache → continue
        ↓
4. Fetch from Network (API)
   ├── Success:
   │   ├── Clear old cache
   │   ├── Insert fresh data to DB
   │   └── emitAll(DB Flow) ← Single Source of Truth
   │
   └── Error:
       ├── Has cache → emitAll(DB Flow, isFromCache=true)
       └── No cache → emit(Error)
```

### Code Example (getMyApplications)

```kotlin
override suspend fun getMyApplications(): Flow<Resource<List<LoanApplicationResponse>>> = flow {
    emit(Resource.Loading())

    // 1. Check Local Cache
    var localData = loanApplicationDao.getAllApplications().firstOrNull()
    if (!localData.isNullOrEmpty()) {
        emit(Resource.Success(localData.map { ... }, isFromCache = true))
    }

    // 2. Network Sync
    try {
        val response = api.getMyApplications()
        if (response.isSuccessful) {
            // Update cache
            loanApplicationDao.deleteAll()
            loanApplicationDao.insertAll(...)
            // Emit from DB (Single Source of Truth)
            emitAll(loanApplicationDao.getAllApplications().map { ... })
        } else {
            // Error but has cache → show cache
            if (!localData.isNullOrEmpty()) {
                emitAll(loanApplicationDao.getAllApplications().map { ... })
            } else {
                emit(Resource.Error(...))
            }
        }
    } catch (e: Exception) {
        // Network error but has cache → show cache
        if (!localData.isNullOrEmpty()) {
            emitAll(loanApplicationDao.getAllApplications().map { ... })
        } else {
            emit(Resource.Error(...))
        }
    }
}.flowOn(Dispatchers.IO)
```

---

## Testing Guide

### Prerequisites

1. Android device/emulator dengan API 26+
2. Backend server running di `http://10.0.2.2:8080` (emulator) atau URL yang sesuai
3. User sudah login dan punya loan application

### Test Scenarios

#### Test 1: Normal Online Flow

**Tujuan**: Verifikasi data load dari server dan di-cache

**Steps**:

1. Pastikan device terhubung ke internet
2. Login ke aplikasi
3. Buka halaman **Pengajuan Saya**
4. Verifikasi data muncul dengan benar
5. Check database menggunakan Database Inspector:
   - View → Tool Windows → App Inspection → Database Inspector
   - Pilih database `loanova.db`
   - Query: `SELECT * FROM loan_application_entity`

**Expected**:

- Data ditampilkan dengan benar
- Data tersimpan di local database

---

#### Test 2: Offline Mode (Cache Hit)

**Tujuan**: Verifikasi data tetap muncul saat offline

**Steps**:

1. Pastikan Test 1 sudah dilakukan (cache terisi)
2. Aktifkan **Airplane Mode**
3. Force close aplikasi
4. Buka kembali aplikasi
5. Buka halaman **Pengajuan Saya**

**Expected**:

- Data dari cache ditampilkan
- Tidak ada loading infinite
- Bisa melihat detail loan (cached)
- Bisa melihat history (jika sudah pernah dibuka)

---

#### Test 3: Offline Mode (Cache Miss)

**Tujuan**: Verifikasi error handling saat tidak ada cache

**Steps**:

1. Clear app data: Settings → Apps → LoanNova → Storage → Clear Data
2. Aktifkan **Airplane Mode**
3. Login (akan gagal tanpa internet)
4. Atau: Matikan internet setelah login tapi sebelum buka fitur loan

**Expected**:

- Menampilkan pesan error "Terjadi kesalahan jaringan"
- Tidak crash

---

#### Test 4: Sync on Reconnect

**Tujuan**: Verifikasi data di-update saat online kembali

**Steps**:

1. Buka aplikasi saat online, load data
2. Matikan internet
3. Admin mengubah status loan di backend
4. Nyalakan kembali internet
5. Pull to refresh atau navigate ulang ke halaman

**Expected**:

- Data diperbarui dari server
- Cache diupdate dengan data terbaru

---

#### Test 5: Branch List Caching

**Tujuan**: Verifikasi branch list ter-cache untuk form pengajuan

**Steps**:

1. Buka form **Ajukan Pinjaman** (saat online)
2. Verifikasi dropdown cabang terisi
3. Matikan internet
4. Navigate keluar, lalu kembali ke form

**Expected**:

- Dropdown cabang masih terisi dari cache
- Pengguna bisa mengisi form (meski tidak bisa submit saat offline)

---

#### Test 6: Application History Caching

**Tujuan**: Verifikasi history loan di-cache per loan ID

**Steps**:

1. Buka detail loan application
2. Buka tab/section **Riwayat**
3. Verifikasi history muncul
4. Matikan internet
5. Navigate keluar, lalu kembali ke detail loan yang sama

**Expected**:

- History tetap muncul dari cache
- Data sesuai dengan loan yang dipilih

---

### Database Inspection

Untuk melihat data di Room database:

1. **Android Studio**:
   - View → Tool Windows → App Inspection
   - Pilih device dan process
   - Pilih tab "Database Inspector"
   - Double-click `loanova.db`

2. **Query Examples**:

   ```sql
   -- Check loan applications
   SELECT * FROM loan_application_entity;

   -- Check history for specific loan
   SELECT * FROM application_history_entity
   WHERE loanApplicationId = 1;

   -- Check branches
   SELECT * FROM branch_entity;

   -- Check cache freshness
   SELECT id, status, lastSyncedAt,
          datetime(lastSyncedAt/1000, 'unixepoch', 'localtime') as syncTime
   FROM loan_application_entity;
   ```

---

## Troubleshooting

### 1. Data tidak ter-cache

**Penyebab**:

- Network request gagal sebelum save
- Entity mapping error

**Solusi**:

- Check logcat untuk error
- Verify mapping di `DataMappers.kt`
- Pastikan database version sudah di-increment

### 2. Cache tidak diupdate

**Penyebab**:

- `deleteAll()` tidak dipanggil sebelum `insertAll()`
- Conflict strategy tidak benar

**Solusi**:

- Pastikan pattern: `deleteAll() → insertAll()` untuk list
- Gunakan `OnConflictStrategy.REPLACE` untuk single item

### 3. Data lama masih muncul setelah logout

**Penyebab**:

- Cache tidak dibersihkan saat logout

**Solusi**:

- Implementasi `clearAllCache()` di logout flow:
  ```kotlin
  suspend fun clearAllCache() {
      loanApplicationDao.deleteAll()
      applicationHistoryDao.deleteAll()
      branchDao.deleteAll()
      plafondDao.deleteAll()
      userDao.clearProfile()
  }
  ```

### 4. Room schema mismatch

**Penyebab**:

- Entity berubah tapi version tidak di-increment

**Solusi**:

- Increment `version` di `@Database`
- Atau clear app data untuk testing

---

## Best Practices

1. **Selalu gunakan Flow dari DAO** sebagai source of truth setelah network sync berhasil
2. **Simpan BigDecimal sebagai String** untuk menjaga precision
3. **Tambahkan `lastSyncedAt` field** untuk debugging dan cache validation
4. **Gunakan `flowOn(Dispatchers.IO)`** untuk operasi database
5. **Handle semua error case**: success, API error, network exception
6. **Test dengan Airplane Mode** untuk validasi offline behavior

---

## Migration Notes

### From Version 2 to 3

**Changes**:

- Added `LoanApplicationEntity`
- Added `ApplicationHistoryEntity`
- Added `BranchEntity`
- Added new DAOs: `LoanApplicationDao`, `ApplicationHistoryDao`, `BranchDao`

**Migration Strategy**:

- Using `fallbackToDestructiveMigration()` - will clear existing data
- For production, create proper Migration:
  ```kotlin
  val MIGRATION_2_3 = object : Migration(2, 3) {
      override fun migrate(database: SupportSQLiteDatabase) {
          database.execSQL("""
              CREATE TABLE loan_application_entity (...)
          """)
          // ... other tables
      }
  }
  ```

---

_Dokumentasi ini dibuat pada implementasi offline-first untuk fitur Loan Application._
