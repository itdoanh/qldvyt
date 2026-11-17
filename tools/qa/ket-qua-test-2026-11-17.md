# Kết quả tự test — QLDVYT (17/11/2026)

## 1. Unit test (JVM) — PASS 7/7
- Lệnh: `./gradlew.bat :app:testDebugUnitTest`
- `TinhTienTest`: 5/5 pass
  - Nhiệm vụ 500k × 30% = 150k ✅
  - Nhiệm vụ hủy = 0 ✅
  - Lương VD docs = 7.384.615 ✅
  - Lợi nhuận gộp VD docs ✅
  - Giá nhập bình quân ✅
- `Sha256Test`: 2/2 pass (chuỗi rỗng + "abc" chuẩn)
- Build: `BUILD SUCCESSFUL`, APK `app-debug.apk` ~61.9MB.

## 2. Test cài đặt + khởi động trên Emulator — PASS
- Emulator: `Medium_Phone_API_37.0`, cấu hình nhẹ `hw.cpu.ncore=1`, `hw.ramSize=2048`, `vm.heapSize=336`.
- `adb install -r app-debug.apk` → `Success`.
- `am start -n vn.rinso.qldvyt.debug/...MainActivity` → pid `1991` sống.
- `logcat --pid=1991`: không có `FATAL` / crash của `qldvyt`.
- Chỉ có warning thiếu `com.google.android.gms.fonts` (emulator không có GMS) — không ảnh hưởng app offline.

## 3. Test UI chụp màn hình — CHƯA ĐẠT (do máy yếu)
- `screencap` + `uiautomator dump` chỉ bắt được dialog ANR của hệ thống:
  - `Pixel Launcher isn't responding` → `System UI isn't responding`.
- Nguyên nhân: emulator 1 CPU render SwiftShader quá chậm, System UI ANR che mất app (app vẫn alive phía sau).
- Ảnh `tools/qa/screenshots/01-tong-quan.png` (131KB) hiện chỉ là màn hình chờ, chưa thấy được 4 thẻ số Tổng quan.
- Checklist `tools/qa/checklist-ui.md` giữ nguyên chưa tick — cần test lại.

## 4. Đã dọn dẹp
- `adb emu kill` + kill `qemu-system-x86_64`, xóa `.lock` — laptop hết lag.

## 5. Khuyến nghị test lại (không lag máy)
- Cách 1 (nhẹ nhất): test UI trên điện thoại thật qua USB (`adb install -r` + mở app chụp tay).
- Cách 2: mở emulator lúc máy rảnh, thêm `-gpu swiftshader_indirect -no-window`, chờ boot 5 phút rồi mới `screencap`.
- Cách 3: bổ sung Compose `@Preview` screenshot test (Paparazzi/Roborazzi) để验 UI không cần emulator.
