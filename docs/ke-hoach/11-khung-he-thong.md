# 11 — Khung hệ thống (System Frame) — QLDVYT

## 1. Sơ đồ module
```
app (Compose UI + ViewModel + Nav)
  → domain (TinhTien + interfaces, pure Kotlin, test được không cần Android)
  → data (Room + DataStore + Backup + Repository impl)
```

## 2. Điều hướng (NavGraph tuyến)
`tongQuan → donHang (chiTietDon/{id}, taoDon) → nhiemVu → kho → more {nhanVien, luong, khachHang, congNo, dichVu, baoCao, saoLuu, caiDat, gioiThieu}`

## 3. Khung dữ liệu
- Room entities đúng `docs/02`. DAO trả `Flow`/`PagingSource`. Mọi ghi tiền trong `@Transaction` + recompute `daThu/conNo/tonKho/giaNhapTB`.
- DataStore keys: `congChuanThang, tyLeNhiemVu, soNgayCanhBaoNo, tenCongTy, pinKhoaApp, daSeedDemo`.

## 4. Khung backup file
`SaoLuuManager.taoBackup(uri) / khoiPhuc(uri) / xuatCsv(bang, uri)` — zip gồm db + prefs + meta.json + sha256. Xem `docs/02 §4`.

## 5. Khung UI
Mỗi feature 1 package `ui/<ten>/`: `ManHinh.kt (Route+Screen) + ViewModel (Hilt) + linhKien riêng`. Chuẩn: TopAppBar + Search + Filter + Empty + Snackbar.
