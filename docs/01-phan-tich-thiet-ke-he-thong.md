# 01 — Phân tích thiết kế hệ thống QLDVYT (System Analysis & Design)

> Ngôn ngữ: Tiếng Việt chính + English phụ trong code. DB local Room, backup file, Android 10+.

## 1. Bối cảnh & phạm vi (Context & Scope)

**Bên dùng (Users — nội bộ công ty, không có khách ngoài dùng app):**
- Chủ/quản lý (Admin): xem toàn bộ doanh thu, lương, công nợ, duyệt bảng lương.
- Thu ngân / điều phối: tạo đơn hàng, gán nhiệm vụ, thu tiền, ghi công nợ.
- Nhân viên y tế (NV): xem nhiệm vụ được giao, xác nhận hoàn thành, xem thu nhập 30%.
- Thủ kho: nhập/xuất vật tư, kiểm kê.

**Ngoài phạm vi (Out of scope v1):** server/cloud sync, đăng nhập online, thanh toán online, đa chi nhánh realtime. Tất cả để cho v2 (nếu cần), v1 chỉ local + backup file.

## 2. Danh sách chức năng chi tiết (Features)

### F1. Quản lý Nhân viên & Cấp bậc (Employee & Rank)
- CRUD `Cấp bậc`: tên (VD: Thực tập, Nhân viên, Kỹ thuật viên chính, Điều dưỡng, Quản lý), lương cơ bản mặc định, mức chuyên cần chuẩn, mô tả.
- CRUD `Nhân viên`: mã NV (VD: NV001 auto), họ tên, SĐT, địa chỉ, cấp bậc, ngày vào làm, trạng thái (Đang làm / Tạm nghỉ / Nghỉ việc), lương cơ bản override (nếu khác chuẩn cấp), STK, ghi chú.
- Chấm công: theo ngày, số công (1 / 0.5 / 0), đi muộn (có/không), ghi chú. Cho phép chấm nhanh cả tháng (lưới calendar).
- Thưởng/phạt/tạm ứng: mỗi dòng gồm ngày, loại, số tiền, lý do.
- Quy tắc: lương cơ bản tháng = `luongCoBan * tongCong / congChuanThang` (mặc định `congChuanThang = 26`). Xem chi tiết công thức ở `03-cong-thuc-tinh-tien.md`.

### F2. Nhiệm vụ / Công việc của NV (Tasks — hưởng 30%)
- Loại công việc (enum): `TIEM_TRUYEN` (đi tiêm truyền), `THAY_BANG` (thay băng), `THU_TUC` (làm thủ tục), `KHAC`.
- Mỗi nhiệm vụ: mã việc, đơn hàng liên kết (nullable — vì có việc lẻ ngoài đơn), dịch vụ tham chiếu, NV thực hiện, ngày giao, ngày hoàn thành, tổng giá (VNĐ), tỷ lệ hưởng (mặc định 30% = 0.30, cho phép sửa từng ca đặc biệt), tiền NV hưởng = `round(tongGia * tyLe)`, trạng thái (Đã giao / Đang làm / Hoàn thành / Hủy).
- Ràng buộc: chỉ nhiệm vụ `HOAN_THANH` trong kỳ lương mới được tính vào bảng lương. Nhiệm vụ hủy không tính tiền.
- Nghiệp vụ 30%: VD ca tiêm truyền giá 500.000đ → NV hưởng 150.000đ. Nếu ca dùng vật tư 80.000đ (tính riêng ở đơn hàng), công ty còn lại 500−150−80 = 270.000đ lợi nhuận gộp.

### F3. Dịch vụ & Gói dịch vụ (Services & Packages)
- `Dịch vụ`: mã DV, tên (VD: Tiêm truyền tại nhà, Thay băng vết thương, Làm thủ tục hồ sơ…), mô tả, giá niêm yết, đơn vị (lần/ca/gói), trạng thái (Đang bán / Ẩn).
- `Gói dịch vụ`: mã gói, tên (VD: Gói chăm sóc sau mổ 7 ngày), giá gói (thường rẻ hơn mua lẻ), danh sách dịch vụ con + số lượng.
- Khi tạo đơn hàng được chọn dịch vụ lẻ hoặc gói (hoặc mix). Giá gói được lưu snapshot vào đơn (để sau này đổi giá catalog không ảnh hưởng đơn cũ).

### F4. Đơn hàng / Phiếu dịch vụ (Orders)
- `Đơn hàng`: mã ĐH (VD: DH20261117-001), khách hàng, ngày tạo, ngày hẹn, trạng thái, các dòng chi tiết, vật tư sử dụng, tổng tiền hàng, giảm giá, tổng thanh toán, đã thu, còn nợ, ghi chú.
- `Chi tiết đơn`: mỗi dòng snapshot tên + đơn giá tại lúc bán (chống trượt giá).
- `Vật tư sử dụng`: chọn vật tư + số lượng → tự lấy đơn giá vốn (giá nhập TB) → tính chi phí vốn của đơn.
- Trạng thái máy (state machine): `CHO_XU_LY → DANG_LAM → HOAN_THANH` (hoặc `HUY` từ 2 trạng thái đầu). Chỉ đơn `HOAN_THANH` mới tính doanh thu. Đơn hủy không tính doanh thu, vật tư đã xuất có thể hoàn kho (tùy chọn).

### F5. Khách hàng & Công nợ (Customers & Debts)
- `Khách hàng`: mã KH, họ tên, SĐT (unique), địa chỉ, ngày sinh, tiền sử/ghi chú chăm sóc.
- `Thanh toán`: mỗi lần thu tiền ghi 1 dòng (ngày, số tiền, phương thức tiền mặt/CK, người thu). Cho phép thu nhiều lần cho 1 đơn.
- Công nợ = `tongThanhToan − daThu` (tổng hợp từ đơn + thanh toán, không nhập tay). Dashboard cảnh báo nợ quá hạn (quá X ngày cấu hình, mặc định 7 ngày).
- Báo cáo công nợ theo khách, theo đơn, tuổi nợ.

### F6. Vật tư / Kho (Inventory)
- `Vật tư`: mã VT, tên, đơn vị (ống/cái/hộp…), giá nhập trung bình (bình quân gia quyền), tồn kho, tồn tối thiểu (để cảnh báo hết hàng), trạng thái.
- `Nhập kho`: ngày, số lượng, đơn giá nhập, NCC → cập nhật `giaNhapTB = (tonCu*giaCu + slNhap*donGiaNhap)/(tonCu+slNhap)` và tăng tồn.
- `Xuất kho`: gắn với đơn hàng/nhiệm vụ (hoặc xuất lẻ) → giảm tồn, ghi giá vốn tại lúc xuất.
- `Kiểm kê`: chốt số thực tế → sinh phiếu chênh lệch.
- Liên kết các phần khác: mỗi đơn hàng/nhiệm vụ đều xem được chi phí vật tư và lợi nhuận gộp.

### F7. Lương & Bảng tính tiền (Payroll)
- Kỳ lương: tháng/năm. Chốt lương → khóa (không cho sửa công/thưởng của kỳ đã chốt, trừ khi mở khóa bởi admin).
- Thành phần (xem `03-cong-thuc-tinh-tien.md`): lương cơ bản theo công + tiền nhiệm vụ 30% + hoa hồng + thưởng + chuyên cần − tạm ứng − khấu trừ khác = thực lĩnh.
- Hoa hồng: % trên doanh thu đơn mà NV giới thiệu/trực tiếp phụ trách (cấu hình từng NV hoặc từng đơn, mặc định 0%).
- Chuyên cần: đủ công chuẩn + không đi muộn → nhận mức chuẩn của cấp bậc (VD: 500.000đ).

### F8. Doanh thu & Báo cáo (Revenue & Reports)
- Doanh thu = Σ `tongThanhToan` của đơn `HOAN_THANH` theo khoảng thời gian (trừ hoàn tiền nếu có).
- Cắt theo: ngày/tuần/tháng/quý/năm, theo NV, theo dịch vụ/gói, theo phương thức thu.
- Xuất CSV (mở bằng Excel) + PDF in ấn + chia sẻ qua Share Sheet.

### F9. Hệ thống: Backup/Restore bằng file + Cài đặt (System)
- Backup: 1 chạm → app copy `qldvyt.db` + `datastore` vào 1 file `.zip` (gồm db + checksum SHA-256 + metadata ngày/app version) → người dùng chọn nơi lưu qua SAF. Tên file: `qldvyt-backup-yyyyMMdd-HHmmss.zip`.
- Restore: chọn file `.zip` → verify checksum → xác nhận ghi đè → copy ngược → restart app.
- Xuất CSV từng bảng (đơn hàng, lương, kho…) để kế toán đối chiếu.
- Cài đặt: công chuẩn tháng, số ngày cảnh báo nợ, tỷ lệ nhiệm vụ mặc định (30%), thông tin công ty + About (Rinso / Đoàn Văn Doanh / itdoanh).
- Khóa app bằng PIN nội bộ (DataStore, không phải bảo mật cấp ngân hàng — chỉ chống mở nhầm).

## 3. Phi chức năng (NFR)
- Offline 100%, mở app < 1.5s trên máy tầm trung, mọi thao tác CRUD < 300ms (DB local + Paging).
- Không crash khi DB ~50k đơn hàng (paging + index đúng cột).
- Backup/restore test với DB 200MB vẫn ổn.
- minSdk 29 (Android 10), target mới nhất, hỗ trợ màn hình 5–11 inch + xoay ngang.
- Toàn bộ UI tiếng Việt, font mặc định hệ thống, tương phản đạt WCAG AA.

## 4. Rủi ro & giảm thiểu
| Rủi ro | Giảm thiểu |
|--------|------------|
| Mất máy → mất DB local | Nhắc backup định kỳ (WorkManager) + xuất file sau mỗi ngày chốt |
| Ghi đè nhầm khi restore | Luôn auto-backup trước khi restore + dialog xác nhận 2 bước |
| Tính tiền sai do làm tròn | Chuẩn `Long` VNĐ + công thức tập trung + unit test 100% nhánh |
| Trượt giá catalog ảnh hưởng đơn cũ | Snapshot giá vào đơn/nhiệm vụ tại lúc tạo |
