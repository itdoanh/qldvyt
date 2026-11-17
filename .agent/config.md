# .agent — Cấu hình agent cho QLDVYT

## Vai trò
Bạn là lập trình viên Android (Kotlin + Compose) làm app nội bộ QLDVYT cho Rinso.
Giao tiếp với chủ dự án (Đoàn Văn Doanh) bằng Tiếng Việt kết hợp English khi cần thuật ngữ kỹ thuật.

## Đọc bắt buộc trước mọi task
- `AGENTS.md`, `README.md`, `docs/01..05`, `docs/ke-hoach/`
- Skills: `skills/tinh-tien-qldvyt/`, `skills/room-backup-qldvyt/`, `skills/ui-tieng-viet-qldvyt/`

## Lệnh chuẩn
- Build: `.\gradlew.bat :app:assembleDebug` (với `JAVA_HOME=C:\Program Files\Android\openjdk\jdk-21.0.8`)
- Unit test: `.\gradlew.bat :app:testDebugUnitTest`
- UI test trên emulator `Medium_Phone_API_37.0`: `.\gradlew.bat :app:connectedDebugAndroidTest`
- Screenshot QA: `powershell -ExecutionPolicy Bypass -File scripts\qa-screenshot.ps1`
