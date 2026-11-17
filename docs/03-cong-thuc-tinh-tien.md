# 03 — Công thức tính tiền (Pricing Formulas) — QLDVYT

> **Chuẩn:** mọi tiền là `Long` VNĐ. Tỷ lệ là `Double` 0..1. Làm tròn: `round()` về đồng sau mỗi bước nhân tỷ lệ. Code tập trung ở `domain/pricing/TinhTien.kt`, kèm unit test.

## 1. Nhiệm vụ NV hưởng 30% (Task commission)

```
tienNvHuong = round(tongGia * tyLeHuong)   // tyLeHuong mặc định 0.30
```

- VD: `tongGia = 500_000`, `tyLe = 0.30` → `150_000đ`.
- Chỉ tính khi `trangThai == HOANTHANH` và `ngayHoanThanh` nằm trong kỳ lương.
- Nếu nhiệm vụ bị hủy → `tienNvHuong = 0`.

## 2. Đơn hàng (Order totals)

```
tongTienHang   = Σ (soLuong_i * donGia_i)            // từng dòng chi tiết
tongThanhToan  = max(0, tongTienHang - giamGia)
daThu          = Σ thanh_toan.soTien (theo đơn)
conNo          = tongThanhToan - daThu               // >0 là khách còn nợ
chiPhiVatTu    = Σ (soLuong_j * donGiaVon_j)         // vật tư đã xuất
loiNhuanGop    = tongThanhToan - chiPhiVatTu - Σ tienNvHuong(các NV của đơn)
```

VD đơn: 2 dòng (500k + 300k) = 800k, giảm 50k → thanh toán 750k. Vật tư 80k, 1 nhiệm vụ NV 150k → lợi nhuận gộp = 750 − 80 − 150 = **520k**.

## 3. Lương tháng NV (Payroll)

```
congChuan            = cai_dat.congChuanThang (mặc định 26)
luongCoBanThang      = snapshot (override hoặc chuẩn cấp)
luongTheoCong        = round(luongCoBanThang * tongCong / congChuan)
tienNhiemVu          = Σ tienNvHuong (HOANTHANH trong tháng)
hoaHong              = Σ round(doanhThuGioiThieu_k * tyLeHoaHong_k)
chuyenCan            = duDieuKien ? mucChuyenCan(cấp) : 0
  duDieuKien = tongCong >= congChuan && soNgayDiMuon == 0 && khongBiPhatNghiemTrong
thucLinh             = luongTheoCong + tienNhiemVu + hoaHong + thuong + chuyenCan
                       - tamUng - khauTruKhac(phạt)
thucLinh             = max(0, thucLinh)
```

VD: lương cơ bản 8tr, công 24/26 → 7.384.615đ; nhiệm vụ 1.5tr; hoa hồng 200k; thưởng 300k; chuyên cần 0 (đi muộn 1 lần); tạm ứng 2tr → thực lĩnh = 7.384.615+1.500.000+200.000+300.000−2.000.000 = **7.384.615đ**.

## 4. Doanh thu (Revenue)

```
doanhThu(khoang) = Σ don_hang.tongThanhToan
                   WHERE trangThai == HOANTHANH
                   AND ngayTao IN khoang
```

- Không tính đơn hủy/chưa hoàn thành. Hoàn tiền (nếu có) ghi `thanh_toan` âm riêng + trừ khỏi doanh thu thuần.
- Báo cáo cắt lát: theo ngày/tuần/tháng, theo NV (qua nhiệm vụ + hoa hồng), theo dịch vụ/gói (qua chi tiết), theo phương thức thu.

## 5. Công nợ (Debt)

```
conNo(don)  = tongThanhToan - daThu
noQuaHan    = conNo > 0 && now - ngayTao > soNgayCanhBaoNo (mặc định 7 ngày)
tongNo(khach) = Σ conNo các đơn chưa thanh toán đủ
```

## 6. Giá vốn vật tư bình quân (Moving average)

```
giaNhapTB_moi = (tonCu * giaCu + slNhap * donGiaNhap) / (tonCu + slNhap)
tonMoi        = tonCu + slNhap (nhập)  |  tonCu - slXuat (xuất)
```

## 7. Quy tắc làm tròn & kiểm thử
- Mọi phép nhân tỷ lệ → `kotlin.math.roundToLong()`.
- Unit test bắt buộc: nhiệm vụ 30% lẻ (VD 333.333×0.3), lương công lẻ, đơn giảm giá > tổng, nợ âm, tồn kho âm (chặn).
