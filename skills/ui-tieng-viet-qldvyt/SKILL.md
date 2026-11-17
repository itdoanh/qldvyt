# Skill: ui-tieng-viet-qldvyt (Vietnamese UI/UX skill)

Dùng khi agent viết hoặc sửa bất kỳ màn hình nào.

## Chuẩn
- UI 100% tiếng Việt có dấu, string trong resource, không hardcode lẻ ngoài `linhKien`.
- Tiền `NumberFormat("vi-VN") + "đ"` (VD `750.000đ`), ngày `dd/MM/yyyy`.
- Màn hình gồm: TopAppBar + Search/Filter + Empty state + Snackbar + FAB.
- Material 3, dark mode đầy đủ, hỗ trợ xoay ngang + font lớn.
- Sửa UI xong → chạy Compose test + chụp screenshot emulator vào `tools/qa/screenshots/`.

Nguồn: `docs/04-thiet-ke-ui-ux.md`.
