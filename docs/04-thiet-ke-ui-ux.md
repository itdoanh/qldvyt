# 04 — Thiết kế UI/UX (Tiếng Việt, Material 3) — QLDVYT

## 1. Điều hướng (Navigation)
- BottomBar 5 tab: **Tổng quan, Đơn hàng, Nhiệm vụ, Kho, Thêm** (More mở Drawer: Nhân viên, Lương, Khách hàng, Công nợ, Dịch vụ/Gói, Báo cáo, Sao lưu, Cài đặt, Giới thiệu).
- Mọi màn hình có: TopAppBar tiếng Việt + FAB hành động chính + Search + Filter chip + Pull-to-refresh + Empty state (hình + nút tạo mới) + Snackbar xác nhận.
- Luồng tạo đơn 3 bước (wizard): 1) Chọn khách → 2) Chọn dịch vụ/gói + vật tư → 3) Xác nhận + gán NV + thu tiền.

## 2. Màn hình chính
| Màn hình | Nội dung | KPI test |
|----------|----------|----------|
| Tổng quan (Dashboard) | Thẻ: Doanh thu hôm nay, Đơn chờ, Công nợ quá hạn, Tồn kho thấp; biểu đồ 7 ngày; đơn mới nhất | Mở <1.5s, số liệu khớp unit test |
| Đơn hàng | List paging + filter trạng thái + tìm theo mã/SĐT; chi tiết gồm chi phí vật tư + lợi nhuận gộp | Tạo đơn 3 bước <60s thao tác |
| Nhiệm vụ | Board theo trạng thái + nút Hoàn thành 1 chạm (tự tính 30%) | Hiển thị đúng tiền 30% |
| Nhân viên | Tab: Danh sách / Chấm công / Thưởng-Phạt | Chấm công tháng <30s |
| Lương | Chọn tháng → bảng lương từng NV → Chốt/Khóa | Công thức khớp doc 03 |
| Kho | Tồn kho + cảnh báo đỏ khi < tồn tối thiểu; Nhập/Xuất/Kiểm kê | Tồn không âm |
| Sao lưu | Nút Sao lưu ra file / Khôi phục từ file / Xuất CSV | File .zip có checksum |

## 3. Chuẩn UI tiếng Việt
- Toàn bộ string trong `strings.xml` / Compose `strings`: tiếng Việt có dấu. VD: "Doanh thu hôm nay", "Chấm công", "Sao lưu ra file", "Khôi phục", "Công nợ quá hạn".
- Tiền: `NumberFormat("vi-VN")` + "đ". VD: `750.000đ`. Ngày: `dd/MM/yyyy`.
- Màu: Material 3 dynamic color + seed teal (#00796B — gợi y tế). Dark mode đầy đủ.

## 4. QA UI/UX trên emulator (không cần điện thoại)
- Script `scripts/qa-screenshot.ps1`: mở emulator `Medium_Phone_API_37.0`, cài APK, chụp từng màn hình vào `tools/qa/screenshots/`, đính kèm checklist `tools/qa/checklist-ui.md`.
- Test: Compose UI test (`createAndroidComposeRule`), Espresso cho backup SAF (dùng `IntentsTestRule` mock URI), screenshot test so sánh pixel (threshold 1%).
