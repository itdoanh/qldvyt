# Checklist QA UI/UX — QLDVYT (đánh dấu sau khi test trên emulator)

Emulator: `Medium_Phone_API_37.0` (x86_64, Android 16 / API 37 — đại diện, app vẫn minSdk 29).

## Chung
- [ ] Mở app < 1.5s, không crash, tiếng Việt đầy đủ, không chữ lồng English lẻ.
- [ ] Dark mode + xoay ngang + font lớn (1.3x) không vỡ layout.
- [ ] Mọi màn có Empty state + Snackbar xác nhận sau CRUD.

## Từng màn
- [ ] Tổng quan: 4 thẻ số + biểu đồ 7 ngày khớp DB seed.
- [ ] Đơn hàng: tạo wizard 3 bước < 60s; lợi nhuận gộp hiển thị đúng.
- [ ] Nhiệm vụ: hoàn thành 1 chạm, tiền 30% đúng (VD 500.000đ → 150.000đ).
- [ ] Nhân viên: chấm công lưới tháng lưu đúng; thưởng/phạt/tạm ứng vào lương.
- [ ] Lương: bảng tháng khớp VD doc 03 (7.384.615đ); chốt/khóa chặn sửa.
- [ ] Kho: nhập/xuất tồn đúng, chặn tồn âm, cảnh báo tồn thấp màu đỏ.
- [ ] Khách hàng/Công nợ: nợ đúng, cảnh báo quá hạn > 7 ngày.
- [ ] Sao lưu: tạo .zip + khôi phục round-trip không mất dữ liệu.
- [ ] Cài đặt/Giới thiệu: hiển thị Rinso (Thuộc Rin) + Đoàn Văn Doanh + itdoanh.

## Screenshots
Lưu vào `tools/qa/screenshots/` theo tên `01-tong-quan.png`, `02-don-hang.png`, ...
