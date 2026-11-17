# 05 — Kiến trúc kỹ thuật (Tech Architecture) — QLDVYT

## 1. Stack mới nhất (2026)
- **Kotlin 2.0.x**, JDK 17+ (build), JVM target 17, `minSdk 29`, `targetSdk 35`, `compileSdk 35`.
- **Compose BOM 2024.10+** + Material3 Expressive, Navigation Compose 2.7+, Adaptive (`material3-adaptive`).
- **Hilt 2.51+**, **Room 2.6+** (KSP), DataStore Preferences, Paging 3, WorkManager, Coroutines/Flow.
- Build: AGP 8.5+, Gradle 8.8+.

## 2. Cấu trúc package (Clean + MVVM)
```
vn.rinso.qldvyt/
├── MainActivity.kt, QldvytApp.kt (Hilt @HiltAndroidApp)
├── core/            // dùng chung: dinhDangTien(), dinhDangNgay(), Result, hằng
├── data/
│   ├── local/       // Room: entities, dao, QldvytDatabase, converters
│   ├── prefs/       // DataStore CaiDat
│   ├── repo/        // Repository impl (DonHangRepository, ...)
│   └── backup/      // SaoLuuManager (zip/unzip + sha256 via SAF)
├── domain/
│   ├── model/       // model UI-independent
│   ├── repo/        // interfaces
│   └── pricing/     // TinhTien.kt (công thức doc 03) + unit test
└── ui/
    ├── dieuHuong/   // NavGraph
    ├── tongQuan/, donHang/, nhiemVu/, nhanVien/, luong/, kho/,
    │   khachHang/, dichVu/, baoCao/, saoLuu/, caiDat/
    └── linhKien/    // TheTien, TheTrangThai, OTimKiem, ManHinhTrong...
```

## 3. Quan trọng
- Room là source-of-truth; UI observe `Flow<PagingData>`; mọi ghi tiền chạy trong `@Transaction`.
- `TinhTien.kt` là nơi duy nhất chứa công thức tiền — DAO/VM không tự tính lẻ.
- Backup: checkpoint WAL trước khi copy; checksum SHA-256; SAF không xin quyền storage legacy.
- Test: `testDebugUnitTest` (TinhTien 100%), `connectedDebugAndroidTest` (DAO + Compose) trên emulator API 37.
