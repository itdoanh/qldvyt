# 02 — Thiết kế Database local (Room / SQLite) — QLDVYT

> File DB: `qldvyt.db` (Room). Version khởi đầu: **1**. Mọi tiền tệ: `INTEGER (Long, VNĐ)`. Ngày tháng: `INTEGER epoch millis` + hiển thị `vi-VN`.

## 1. Sơ đồ quan hệ (ER — text)

```
CapBac (1) ──< (N) NhanVien (1) ──< (N) ChamCong
                              ──< (N) ThuongPhat
                              ──< (N) TamUng
                              ──< (N) NhiemVu
                              ──< (N) BangLuong

DichVu (N) >──< (N) GoiDichVu  (qua GoiChiTiet)
DichVu (1) ──< (N) DonHangChiTiet
GoiDichVu (1) ──< (N) DonHangChiTiet
KhachHang (1) ──< (N) DonHang (1) ──< (N) DonHangChiTiet
                                      ──< (N) DonHangVatTu
                                      ──< (N) NhiemVu
                                      ──< (N) ThanhToan
VatTu (1) ──< (N) NhapKho
VatTu (1) ──< (N) DonHangVatTu
NhanVien (1) ──< (N) ThanhToan (nguoiThu)
```

## 2. Chi tiết bảng (Table specs)

### 2.1 `cap_bac`
| Cột | Kiểu | Ràng buộc | Ghi chú |
|-----|------|-----------|---------|
| id | TEXT PK | uuid | Khóa chính |
| ten | TEXT | NOT NULL, UNIQUE | VD: Kỹ thuật viên |
| moTa | TEXT | | |
| luongCoBan | INTEGER | NOT NULL, >=0 | VNĐ/tháng |
| chuyenCanMuc | INTEGER | >=0 | Mức thưởng chuyên cần |
| createdAt / updatedAt | INTEGER | | epoch millis |

### 2.2 `nhan_vien`
| Cột | Kiểu | Ràng buộc |
|-----|------|-----------|
| id | TEXT PK uuid | |
| maNv | TEXT UNIQUE NOT NULL | NV001… (auto suggest) |
| hoTen | TEXT NOT NULL | |
| sdt | TEXT UNIQUE | validate 10 số VN |
| diaChi | TEXT | |
| capBacId | TEXT FK → cap_bac.id | RESTRICT khi xóa cấp còn NV |
| ngayVaoLam | INTEGER | epoch |
| trangThai | TEXT | DANGLAM/TAMNGHI/NGHIVIEC |
| luongCoBanOverride | INTEGER NULL | NULL = dùng chuẩn cấp |
| hoaHongMacDinh | REAL | 0.0–1.0, VD 0.05 = 5% |
| stk | TEXT | |
| ghiChu | TEXT | |

Index: `maNv`, `sdt`, `capBacId`.

### 2.3 `cham_cong`
| Cột | Kiểu |
|-----|------|
| id TEXT PK | |
| nhanVienId FK | CASCADE xóa NV → xóa công? (khuyến nghị SET NULL + giữ lịch sử — v1 dùng CASCADE + cảnh báo) |
| ngay INTEGER (start-of-day millis) | UNIQUE(nhanVienId, ngay) |
| soCong REAL | 0 / 0.5 / 1 |
| diMuon INTEGER 0/1 | |
| ghiChu TEXT | |

### 2.4 `thuong_phat` — loại: THUONG / PHAT / CHUYEN_CAN / KHAC
`(id, nhanVienId FK, ngay, loai, soTien INTEGER (dương với thưởng, âm với phạt — hoặc tách dấu ở UI), lyDo)`

### 2.5 `tam_ung` — `(id, nhanVienId FK, ngay, soTien INTEGER >0, lyDo)`

### 2.6 `dich_vu` — `(id, maDv UNIQUE, ten NOT NULL, moTa, giaNiemYet INTEGER, donVi, trangThai DANGBAN/AN, createdAt)`
### 2.7 `goi_dich_vu` — `(id, maGoi UNIQUE, ten, moTa, giaGoi INTEGER, trangThai)`
### 2.8 `goi_chi_tiet` — `(goiId FK, dichVuId FK, soLuong REAL, PK(goiId, dichVuId))`

### 2.9 `vat_tu`
`(id, maVt UNIQUE, ten NOT NULL, donVi, giaNhapTB INTEGER, tonKho REAL, tonToiThieu REAL, trangThai)`
### 2.10 `nhap_kho`
`(id, vatTuId FK, ngay, soLuong REAL >0, donGiaNhap INTEGER, nhaCungCap, ghiChu)` — trigger app-code cập nhật `giaNhapTB` + `tonKho`.

### 2.11 `khach_hang`
`(id, maKh UNIQUE, hoTen NOT NULL, sdt UNIQUE, diaChi, ngaySinh, ghiChu, createdAt)`

### 2.12 `don_hang`
| Cột | Kiểu | Ghi chú |
|-----|------|---------|
| id TEXT PK | | |
| maDh TEXT UNIQUE | DH20261117-001 | |
| khachHangId FK | | |
| ngayTao / ngayHen INTEGER | | |
| trangThai TEXT | CHO_XU_LY/DANG_LAM/HOAN_THANH/HUY | |
| tongTienHang INTEGER | Σ chi tiết | |
| giamGia INTEGER | >=0 | |
| tongThanhToan INTEGER | = tongTienHang − giamGia | |
| daThu INTEGER | Σ thanh_toan (denormalize để query nhanh, luôn recompute trong transaction) | |
| conNo INTEGER | = tongThanhToan − daThu | |
| ghiChu TEXT | | |

### 2.13 `don_hang_chi_tiet`
`(id, donHangId FK CASCADE, dichVuId NULL, goiId NULL, tenHienThi SNAPSHOT, soLuong, donGia SNAPSHOT, thanhTien = soLuong*donGia)` — CHECK: `dichVuId NOT NULL OR goiId NOT NULL OR tenHienThi != ''`.

### 2.14 `don_hang_vat_tu`
`(id, donHangId FK CASCADE, vatTuId FK, soLuong, donGiaVon SNAPSHOT (giaNhapTB lúc xuất), thanhTienVon)` + giảm `tonKho` trong cùng transaction.

### 2.15 `nhiem_vu`
`(id, maViec UNIQUE, donHangId NULL FK, dichVuId NULL, tenCongViec, loai TIEM_TRUYEN/THAY_BANG/THU_TUC/KHAC, nhanVienId FK, ngayGiao, ngayHoanThanh NULL, tongGia INTEGER, tyLeHuong REAL default 0.30, tienNvHuong INTEGER = round(tongGia*tyLe), trangThai DAGIAO/DANGLAM/HOANTHANH/HUY)`

### 2.16 `thanh_toan`
`(id, donHangId FK, khachHangId FK, ngay, soTien INTEGER >0, phuongThuc TIENMAT/CK, nguoiThuId NULL, ghiChu)` — sau insert/update/delete: recompute `don_hang.daThu/conNo` trong transaction.

### 2.17 `bang_luong`
`(id, nhanVienId FK, thang 1-12, nam, luongCoBan INTEGER snapshot, tongCong REAL, tienNhiemVu INTEGER, hoaHong INTEGER, thuong INTEGER, chuyenCan INTEGER, tamUng INTEGER, khauTruKhac INTEGER, thucLinh INTEGER, trangThai NHAP/CHOT, ngayChot NULL, UNIQUE(nhanVienId, thang, nam))`

### 2.18 `cai_dat` (key-value)
Keys: `congChuanThang=26`, `tyLeNhiemVuMacDinh=0.30`, `soNgayCanhBaoNo=7`, `tenCongTy`, `pinKhoaApp`, `daSeedDemo`.

## 3. Migration & seed
- Room `exportSchema=true`, giữ `app/schemas/`. Mỗi thay đổi entity → tăng version + viết `Migration`.
- Seed lần đầu: 3 cấp bậc, 5 dịch vụ, 2 gói, 6 vật tư, 1 admin cấu hình. Gắn cờ `daSeedDemo` để không seed lại.

## 4. Backup file format (.zip)
```
qldvyt-backup-YYYYMMDD-HHmmss.zip
├── qldvyt.db (+ -wal/-shm nếu có → checkpoint trước)
├── datastore/preferences.pb
├── meta.json {appVersion, dbVersion, createdAt, device, checksumSha256}
└── CHECKSUM.sha256
```
Restore: verify checksum → auto-backup hiện tại → chép đè → `Room.reopen` / restart process.

## 5. Index & hiệu năng
- Index mọi FK + `don_hang(ngayTao, trangThai)`, `nhiem_vu(nhanVienId, ngayHoanThanh, trangThai)`, `thanh_toan(donHangId, ngay)`, `vat_tu(maVt)`.
- Danh sách lớn dùng Paging 3 (`PagingSource` từ DAO), không load full vào RAM.
