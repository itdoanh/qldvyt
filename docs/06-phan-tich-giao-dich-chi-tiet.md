# 06 — Phân tích thiết kế giao dịch chi tiết (Transaction-Level Design) — QLDVYT v2

> Mục tiêu: sửa triệt để bản v1 "rất tệ — chả có gì". Mọi giao dịch (transaction) phải có: actor → điều kiện đầu vào → luồng chuẩn/alternatives → ràng buộc DB → công thức tiền → trạng thái sau → hoàn nhập/rollback.
> Ngôn ngữ: Tiếng Việt chính. DB: Room local 100% offline. Tiền: `Long` VNĐ. Công thức duy nhất: `domain/pricing/TinhTien`.

## 1. Chuẩn giao dịch chung (áp cho mọi F)

| # | Quy tắc | Diễn giải |
|---|---------|-----------|
| T0 | Atomic + idempotent | Mỗi giao dịch chạy trong `db.withTransaction{}`. Nút bấm chống double-click (disable khi đang chạy + `thongBao`). Seed chạy幂等. |
| T1 | Snapshot chống trượt giá | Giá catalog (`dich_vu.giaNiemYet`, `goi.giaGoi`, `vatTu.giaNhapTB`) chỉ là gợi ý lúc chọn; khi lưu đơn/nhiệm vụ phải copy giá vào dòng chi tiết. Đổi giá sau không đụng đơn cũ. |
| T2 | Không nhập tay số tổng hợp | `tongThanhToan/daThu/conNo/tienNvHuong/giaNhapTB/thucLinh` đều recompute từ dòng chi tiết, không cho nhập tay. |
| T3 | State machine khóa chiều | `CHO_XU_LY→DANG_LAM→HOAN_THANH`, `DAGIAO→DANGLAM→HOANTHANH`; `HUY` là nhánh kết thúc. Đã `HOAN_THANH/HUY/CHOT` thì khóa sửa, muốn sửa phải đảo trạng thái có log. |
| T4 | Empty/loading/error đầy đủ | Mọi danh sách: skeleton lúc tải + empty state có nút tạo mới + snackbar sau CRUD + search + filter chip. |
| T5 | Audit tối thiểu | Mọi bảng có `ngay`/`ngayTao`; tiền có `daThu/conNo`; kho có `giaNhapTB/tonKho`; lương có `trangThai NHAP/CHOT`. |

## 2. Danh sách giao dịch (12 giao dịch lõi, kiểu MISA)

### GD1. Tạo đơn hàng (Order entry — 3 bước)
- Actor: Thu ngân/Điều phối. Đầu vào: `khachHangId` + ≥1 mặt hàng + vật tư dùng (optional) + `giamGia` + `ngayHen`.
- Luồng: (1) chọn khách (search SĐT/tên) → (2) chọn DV/gói (lọc nhóm sổ: TIEM_TRUYEN/DI_XE_TRONG/THU_TUC/CHAM_SOC/VAT_LY/KHAC) + số lượng + vật tư → (3) xác nhận: hiện `tongTienHang/giamGia/tongThanhToan`, nút Tạo.
- Ràng buộc: `matHang.nonEmpty`, `soLuong>0`, `donGia>=0`, tồn kho đủ (`tonKho>=sl` else báo thiếu, chặn tạo).
- DB (1 transaction): insert `don_hang` (trangThai CHO_XU_LY, daThu=0, conNo=tongThanhToan) + N `don_hang_chi_tiet` (snapshot ten/donGia/thanhTien) + M `don_hang_vat_tu` (snapshot donGiaVon=giaNhapTB) + trừ `vat_tu.tonKho`.
- Rollback: tồn không đủ → abort toàn bộ, không tạo đơn rác. Đơn HUY sau này có tùy chọn hoàn kho (v2: cộng trả tồn + ghi phiếu hoàn).

### GD2. Thu tiền nhiều lần (Collection)
- Đầu vào: `donHangId + soTien>0 + phuongThuc TIENMAT/CK + nguoiThuId`.
- Luồng: mở chi tiết đơn → bấm Thu tiền → nhập số tiền (gợi ý = conNo) → xác nhận → recompute `daThu=sum(thanh_toan)`, `conNo=tongThanhToan-daThu`. Cho thu vượt? Chặn: `soTien<=conNo` (trừ admin xác nhận hoàn thừa).
- Cảnh báo: đơn quá hạn (`ngayTao + soNgayCanhBaoNo < today && conNo>0`) hiện đỏ + chip "Quá hạn X ngày".

### GD3. Giao + hoàn thành nhiệm vụ (Task 30%)
- Đầu vào: `nhanVienId + tenCongViec + loai + tongGia + tyLe (default từ CaiDat 0.30) + donHangId?`.
- Luồng: DAGIAO → DANGLAM (bắt đầu) → HOANTHANH 1 chạm (confirm dialog hiện `tienNvHuong=round(tongGia*tyLe)` VD 500.000đ→150.000đ) → set `ngayHoanThanh`. HUY → `tienNvHuong=0`.
- Ràng buộc: chỉ HOANTHANH trong kỳ mới vào lương; HUY không tính. `tongGia<=0 → 0đ`.
- Mở rộng MISA-style: mỗi nhiệm vụ có checklist việc (đến nơi/ thực hiện/ dọn dẹp/ khách ký), ảnh/ ghi chú, đánh giá sao của khách (v2).

### GD4. Nhập kho (GRN — bình quân gia quyền)
- Đầu vào: `vatTuId + soLuong>0 + donGiaNhap>=0 + NCC`.
- Công thức: `giaNhapTB_new = round((tonCu*giaCu + slNhap*donGiaNhap)/(tonCu+slNhap))`; `tonKho+=slNhap`. Insert `nhap_kho` để trace.
- Edge: vật tư mới `tonCu=0 → gia=donGiaNhap`. Không cho giá âm, số lượng âm.

### GD5. Xuất kho lẻ / kiểm kê (Issue/Stocktake)
- Xuất lẻ (không qua đơn): chọn VT + sl → chặn tồn âm → trừ tồn + ghi `nhap_kho` âm? v1: chỉ cho xuất qua đơn + nút "xuất lẻ" ghi `ghiChu`. Kiểm kê: nhập `tonThucTe` → sinh chênh lệch `tonThucTe-tonHeThong`, confirm mới chốt.

### GD6. Chấm công tháng (Timesheet grid)
- Đầu vào: `nvId + ngay (đầu ngày) + soCong {0,0.5,1} + diMuon + ghiChu`. Upsert theo unique `(nvId,ngay)`.
- UI MISA-style: lưới 30/31 cột × NV hàng, bấm ô để cycle 0→0.5→1, long-press đánh đi muộn. Nút "chấm nhanh hôm nay cả đội".
- Khóa kỳ: tháng đã CHOT lương → chặn chấm/sửa (phải mở khóa bởi admin, có dialog xác nhận).

### GD7. Thưởng/phạt/tạm ứng (Bonus/Penalty/Advance)
- Mỗi dòng: `nvId + ngay + loai + soTien + lyDo`. Quy ước: thưởng `soTien>0`, phạt `soTien<0` (hiện đỏ). Tạm ứng là bảng riêng, trừ thẳng vào `thucLinh`.
- Ràng buộc: không cho tạm ứng vượt `luongCoBan` (cảnh báo), kỳ CHOT thì khóa thêm/sửa.

### GD8. Tính + chốt lương (Payroll close)
- Đầu vào: `thang/nam`. Luồng: bấm "Tính lương" → với mỗi NV DANGLAM: `tongCong=sum(cham_cong)`, `tienNV=sum(nhiem_vu HOANTHANH trong tháng)`, `thuong/tamUng/khauTru` từ bảng kỳ, `chuyenCan = duDieuKien? mucChuanCapBac : 0` → `thucLinh=max(0, …)` → upsert `bang_luong` trạng thái NHAP. Bấm "Chốt" → chuyển CHOT + khóa.
- Công thức duy nhất từ `TinhTien.luongTheoCong/thucLinh/duDieuKienChuyenCan`. VD doc03: 7.384.615đ phải ra đúng trong test.

### GD9. Công nợ & tuổi nợ (AR aging)
- Công nợ suy từ `don_hang.conNo`, không nhập tay. Báo cáo: theo khách (tổng nợ + số đơn nợ + đơn quá hạn nhất), theo đơn (tuổi nợ = today-ngayTao), bucket 0–7 / 8–30 / >30 ngày.
- Hành động: gọi/nhắc (ghi `ghiChu`), thu tiền nhanh từ dòng nợ, xuất CSV đối chiếu.

### GD10. Doanh thu & sổ nhóm (Revenue book — giống sổ tay)
- Doanh thu = Σ `tongThanhToan` của đơn HOAN_THANH trong kỳ. Sổ nhóm group theo `dich_vu.nhom` qua `don_hang_chi_tiet` (TIEM_TRUYEN / DI_XE_TRONG / THU_TUC / CHAM_SOC / VAT_LY / KHAC) — đúng các mục trong ảnh sổ tay.
- Biểu đồ: cột gom 3 ngày/cột trong tháng + line 12 tháng (v2). Mọi số có drill-down: bấm cột/nhóm → mở danh sách đơn cấu thành.

### GD11. Sao lưu/khôi phục/Xuất CSV (Backup)
- Backup: 1 chạm → zip `qldvyt.db (+wal/shm) + shared_prefs/cai_dat* + meta.json{app,ngay,dbVersion,sha}` → SAF CreateDocument. Hiện SHA-256 + dung lượng để đối chiếu.
- Restore: SAF OpenDocument → verify có `qldvyt.db` + đọc meta → auto-backup DB hiện tại ra cache → chép đè → yêu cầu restart. Thiếu db → báo lỗi, không ghi đè.
- CSV: BOM `\uFEFF`, header tiếng Việt không dấu? Không — giữ có dấu, Excel đọc đúng. Hỗ trợ: don_hang, bang_luong, so_doanh_thu, cong_no, ton_kho.

### GD12. Cài đặt + giao diện (Settings & Theme)
- Cài đặt: `tenCongTy, congChuanThang (1–31), tyLeNhiemVu (0–100%), soNgayCanhBaoNo (1–90), cheDoGiaoDien SANG/TOI/THEO_HE_THONG, PIN khóa app`.
- Theme: đổi là apply ngay toàn app (không restart), preview 3 thẻ màu. Dark mode phải đủ tương phản WCAG AA, biểu đồ dùng màu adapted.

## 3. Ma trận phân quyền nội bộ (v1 đơn giản — v2 mới login)

| Chức năng | Chủ/QL | Điều phối | Thủ kho | NV y tế |
|---|---|---|---|---|
| Đơn/thu tiền | ● | ● | ○ | ○ |
| Nhiệm vụ của mình | ● | ● | ○ | ● (xác nhận xong) |
| Kho nhập/xuất/kiểm kê | ● | ○ | ● | ○ |
| Lương tính/chốt/mở khóa | ● | ○ | ○ | xem của mình |
| Sao lưu/khôi phục | ● | ○ | ○ | ○ |
| Cài đặt hệ thống | ● | ○ | ○ | ○ |

## 4. Dữ liệu test bắt buộc (mỗi phần ≥10 dòng chi tiết)

- Cấp bậc ≥4, NV ≥10 (đủ 3 trạng thái + override lương + STK), chấm công ≥10 NV × 5 ngày, thưởng/phạt ≥10 dòng, tạm ứng ≥10 dòng.
- Dịch vụ ≥15 (đủ 6 nhóm sổ), gói ≥5 (có chi tiết gói), vật tư ≥15 (có tồn thấp), nhập kho ≥10 phiếu (đủ NCC), khách ≥20 (có SĐT/địa chỉ/ngày sinh), đơn ≥12 (đủ 4 trạng thái + giảm giá + hẹn giờ), chi tiết đơn ≥12, vật tư đơn ≥5, nhiệm vụ ≥10 (đủ 4 trạng thái + 30%), thanh toán ≥10 (đủ TIENMAT/CK + nhiều lần/đơn), lương do tính ra (không seed tay số tổng hợp).

## 5. Checklist UI/UX v2 (sửa "rất tệ")

- [ ] Mở app <1.5s, skeleton không màn trắng; tiếng Việt 100%, không chữ English lẻ.
- [ ] Theme SÁNG/TỐI/THEO HỆ THỐNG trong Cài đặt, đổi là đổi ngay, preview trước.
- [ ] Mọi danh sách: search + filter chip + sort + pull-refresh + empty state + FAB.
- [ ] Form: validate inline (VD tồn không đủ, tiền vượt nợ), confirm dialog cho Hủy/Chốt/Khôi phục, snackbar undo khi xóa.
- [ ] Biểu đồ: dark mode đọc được, số tiền rút gọn (tr/k), bấm vào có drill-down.
- [ ] Chữ số: `vi-VN + đ`, ngày `dd/MM/yyyy HH:mm`, SĐT bấm gọi được (`tel:` intent).
- [ ] Không vỡ layout khi font 1.3x + xoay ngang + màn 5–11 inch.
