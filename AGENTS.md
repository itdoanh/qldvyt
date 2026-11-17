# AGENTS.md — Hướng dẫn cho AI agent (QLDVYT)

> Dự án: app Android nội bộ QLDVYT. Ngôn ngữ giao tiếp + UI: **Tiếng Việt (chính) + English (phụ trong code)**. Nhà phát triển: **Rinso (Thuộc Rin)**. Lập trình viên: **Đoàn Văn Doanh (itdoanh)**.

## 1. Đọc trước khi code
1. `README.md`, `docs/01..05`, `docs/ke-hoach/10..17`.
2. Công thức tiền DUY NHẤT ở `domain/pricing/TinhTien.kt` theo `docs/03-cong-thuc-tinh-tien.md`. Cấm tính tiền lẻ ở UI/DAO.
3. DB: Room, tiền `Long` VNĐ, snapshot giá vào đơn/nhiệm vụ, mọi FK có index.

## 2. Quy tắc code
- Kotlin + Compose + Hilt + Coroutines/Flow. UI tiếng Việt 100%, format `vi-VN`.
- `minSdk 29`, không dùng API chỉ có trên Android 14+ nếu không có fallback.
- Mỗi feature: `Route + Screen + ViewModel + strings`. Không hardcode tiền/tỷ lệ.
- Commit tiếng Việt, tên committer `itdoanh` theo config repo.

## 3. Quy tắc test (bắt buộc)
- Sửa công thức → chạy `testDebugUnitTest`. Sửa UI → chạy Compose test + chụp screenshot emulator.
- Không cần điện thoại thật: dùng emulator `Medium_Phone_API_37.0` + `scripts/qa-screenshot.ps1`.

## 4. Cấm
- Cấm thêm server/cloud/auth online vào v1. Cấm xin quyền storage legacy (dùng SAF). Cấm đổi DB version mà không viết Migration.
