# Release v1.0.0 — QLDVYT (nội bộ Rinso)

- Version: 1.0.0 (versionCode 1), minSdk 29 (Android 10+), target/compile 35.
- Phạm vi v1: Khách hàng, Dịch vụ/Gói, Đơn hàng 3 bước + thu tiền nhiều lần + công nợ, Nhân viên + chấm công + thưởng/phạt/tạm ứng, Nhiệm vụ NV hưởng 30% (1 chạm hoàn thành), Kho nhập/xuất (bình quân gia quyền) + cảnh báo tồn thấp, Bảng lương tháng + chốt/khóa, Dashboard doanh thu thật, Báo cáo CSV (Excel), Sao lưu/khôi phục file .zip (SAF + SHA-256), Cài đặt, Giới thiệu Rinso.
- Công thức tiền tập trung `domain/pricing/TinhTien.kt`, kiểm thử `TinhTienTest` + `Sha256Test` pass.
- DB local Room v1 duy nhất `qldvyt.db`, seed demo 1 lần (3 cấp bậc, 5 dịch vụ, 2 gói, 6 vật tư).
- QA: `assembleDebug` + `testDebugUnitTest` pass. UI test trên thiết bị thật/emulator để lại cho máy khỏe hơn (emulator hiện khóa 1 CPU / 2GB RAM để chống lag theo yêu cầu).
- Cài đặt: lấy file `app-debug.apk` trong `app/build/outputs/apk/debug/`, chép qua máy Android 10+ để cài thử nội bộ.
- Lưu ý: dữ liệu local — sao lưu .zip thường xuyên, giữ file cẩn thận.
