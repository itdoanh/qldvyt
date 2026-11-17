# QLDVYT — Phần mềm Android quản lý Dịch vụ Chăm sóc Sức khỏe (nội bộ)

> **Ngôn ngữ ứng dụng:** Tiếng Việt 100% (UI + báo cáo + docs)
> **Nhà phát triển (Developer/Org):** Rinso (Thuộc Rin)
> **Lập trình viên (Programmer):** Đoàn Văn Doanh — GitHub: [@itdoanh](https://github.com/itdoanh)
> **Repo:** https://github.com/itdoanh/qldvyt.git
> **Mô hình triển khai:** Nội bộ công ty, **100% offline-first, DB local trên máy (Room/SQLite), không cần server**. Backup/restore bằng **file** (.db / .zip / .csv) qua Storage Access Framework.
> **Hỗ trợ:** Android 10+ (API 29+). Target/compile mới nhất (API 35/36). UI: Jetpack Compose + Material 3.

## 1. Mục tiêu (Goal)

Xây dựng 1 app Android duy nhất cho nhân viên nội bộ công ty dịch vụ y tế/chăm sóc sức khỏe để:

1. Quản lý **nhân viên các cấp** (cấp bậc, lương cơ bản, hoa hồng, thưởng, chuyên cần, công/chấm công).
2. Quản lý **nhiệm vụ (công)** của NV: đi tiêm truyền, thay băng, làm thủ tục… — NV thực hiện **hưởng 30% trên tổng giá** nhiệm vụ.
3. Quản lý **dịch vụ, gói dịch vụ** (catalog giá).
4. Quản lý **đơn hàng (order/phiếu dịch vụ)**, **khách hàng**, **công nợ khách**.
5. Quản lý **vật tư** (kho, giá vốn/chi phí, giá bán, liên kết với đơn hàng & nhiệm vụ để tính lãi/lỗ).
6. Tính **lương** NV các cấp, **doanh thu** (theo ngày/tuần/tháng/NV/dịch vụ), **bảng tính tiền** đầy đủ, xuất báo cáo + backup file.

## 2. Nguyên tắc thiết kế (Design Principles)

| # | Nguyên tắc | Diễn giải |
|---|-----------|-----------|
| 1 | Offline-first, local-first | Room (SQLite) là source-of-truth duy nhất. Không có backend, không tracking, không tài khoản online. |
| 2 | Tiền tệ chuẩn VNĐ | Mọi tiền dùng `Long` (đồng), không dùng Float/Double. Format `vi-VN`. Làm tròn về đồng. |
| 3 | Công thức tập trung | Mọi công thức tiền (30% nhiệm vụ, lương, doanh thu, công nợ) nằm ở `domain/pricing` + unit test, UI chỉ hiển thị. |
| 4 | Liên kết vật tư chặt chẽ | Mỗi đơn hàng/nhiệm vụ đều quy được về chi phí vật tư → tính được lợi nhuận gộp. |
| 5 | Tiếng Việt nhất quán | Mọi key UI, docs, comment code quan trọng đều song ngữ Việt (chính) + English (phụ trong code). |
| 6 | Backup là tính năng sống còn | 1 chạm xuất file backup, 1 chạm khôi phục, kèm checksum + xác nhận ghi đè. |

## 3. Công nghệ mới nhất (Tech Stack — 2026)

| Layer | Lựa chọn | Lý do |
|-------|---------|-------|
| Ngôn ngữ | **Kotlin 2.x** (JVM 17, minSdk 29) | Concise, null-safety, coroutine/Flow |
| UI | **Jetpack Compose + Material 3 Expressive**, Navigation Compose, Adaptive layout | Declarative, test UI dễ (Compose Test + screenshot) |
| DI | **Hilt (Dagger)** | Chuẩn Google, dễ test |
| DB local | **Room (SQLite) + Paging 3, DataStore Preferences** | Offline, migration có version, backup = copy .db |
| Async | Coroutines + Flow + WorkManager (backup định kỳ/nhắc việc) | Không block UI |
| Báo cáo | Export CSV + PDF (via Android Print / iText-free impl) + Share Sheet | Kế toán đọc được bằng Excel |
| Backup file | SAF (ACTION_CREATE_DOCUMENT / OPEN_DOCUMENT) + Zip (java.util.zip) + checksum SHA-256 | Không cần quyền storage nguy hiểm |
| Test | JUnit4/5, Room in-memory test, Turbine, Compose UI Test, Espresso, Screenshot test | Test kỹ UI/UX trên emulator, không cần điện thoại thật |
| Build | Gradle 8.x + AGP 8.5+, JDK 17/21, compileSdk 35, minSdk 29 | Mới nhất mà vẫn chạy Android 10 |

Chi tiết xem `docs/05-kien-truc-ky-thuat.md`.

## 4. Tổng quan module (Modules)

```
Dashboard (doanh thu hôm nay, công nợ, tồn kho cảnh báo)
├── Nhân viên (CapBac, NhanVien, ChamCong, Thuong, TamUng)
├── Nhiệm vụ / Công việc (NhiemVu: tiem-truyen, thay-bang, thu-tuc...)
├── Dịch vụ & Gói (DichVu, GoiDichVu, GoiChiTiet)
├── Đơn hàng (DonHang + ChiTiet + VatTu su dung)
├── Khách hàng & Công nợ (KhachHang, CongNo, ThanhToan)
├── Vật tư / Kho (VatTu, NhapKho, XuatKho, KiemKe)
├── Lương & Bảng tính tiền (BangLuong: co ban + 30% NV + HH + thuong + chuyen can)
├── Doanh thu & Báo cáo (theo ngay/tuan/thang/NV/dich vu/goi)
└── Hệ thống (Backup/Restore file, Cai dat, Gioi thieu Rinso)
```

Luồng tiền chuẩn:

```
Khách đặt Đơn hàng (DichVu/Goi, giá bán)
  → Gán Nhiệm vụ cho NV (ai làm → hưởng 30% tổng giá nhiệm vụ)
  → Trừ Vật tư đã dùng (giá vốn) → Lợi nhuận gộp = giá bán − 30% NV − vốn vật tư
  → Thu tiền (tiền mặt/CK) → nếu thiếu → sinh Công nợ
  → Cuối tháng: Chấm công + Chuyên cần + Thưởng → Bảng lương
  → Dashboard Doanh thu = Σ đơn hoàn thành − giảm giá/hoàn tiền
```

## 5. Cấu trúc repo

```
qldvyt/
├── README.md                    ← file này
├── AGENTS.md                    ← hướng dẫn cho AI agent
├── docs/                        ← phân tích thiết kế + kế hoạch
│   ├── 00-tong-quan-du-an.md (symlink concept → README)
│   ├── 01-phan-tich-thiet-ke-he-thong.md
│   ├── 02-thiet-ke-database-room.md
│   ├── 03-cong-thuc-tinh-tien.md
│   ├── 04-thiet-ke-ui-ux.md
│   ├── 05-kien-truc-ky-thuat.md
│   └── ke-hoach/
├── skills/                      ← skills cho agent
├── .agent/                      ← cấu hình agent
├── .cursor/rules/               ← rules cho Cursor
├── app/                         ← source Android (Kotlin + Compose)
├── scripts/                     ← script test emulator, screenshot, backup
└── tools/qa/                    ← checklist QA UI/UX
```

## 6. Cách chạy nhanh (Quickstart — test trên máy, không cần điện thoại)

Yêu cầu: Windows + Android Studio + SDK + emulator `Medium_Phone_API_37.0` (đã có sẵn trên máy).

```powershell
# 1. Mở emulator (không cần điện thoại thật)
& "$env:LOCALAPPDATA\Android\Sdk\emulator\emulator.exe" -avd Medium_Phone_API_37.0

# 2. Build + cài app lên emulator
$env:JAVA_HOME="C:\Program Files\Android\openjdk\jdk-21.0.8"
.\gradlew.bat :app:installDebug

# 3. Chạy test (unit + UI)
.\gradlew.bat :app:testDebugUnitTest :app:connectedDebugAndroidTest

# 4. Chụp màn hình kiểm UI (lưu vào tools/qa/screenshots/)
powershell -ExecutionPolicy Bypass -File scripts\qa-screenshot.ps1
```

## 7. Thông tin phần mềm (About — hiển thị trong app)

- **Tên:** QLDVYT — Quản lý Dịch vụ Y tế
- **Nhà phát triển:** Rinso (Thuộc Rin)
- **Lập trình viên:** Đoàn Văn Doanh (itdoanh)
- **Phiên bản:** xem `app/build.gradle.kts` (`versionName`, `versionCode`)
- **Giấy phép nội bộ:** Chỉ dùng nội bộ công ty, cấm phát hành công khai khi chưa có cho phép của Rinso.
- **Hỗ trợ:** Android 10 (API 29) trở lên. Dữ liệu lưu local, backup bằng file do người dùng tự giữ.

---
*Tài liệu chi tiết: xem thư mục `docs/`. Kế hoạch thực hiện: `docs/ke-hoach/`. Hướng dẫn agent: `AGENTS.md`.*
