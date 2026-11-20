# 07 — Deep Research: Thiết kế vượt MISA cho QLDVYT (v3 Expressive)

> Ngày: 20/11/2026. Mục tiêu: sửa triệt để “giao diện chưa trực quan, tính năng chỉ cho có”.
> Phương pháp: đối chiếu MISA AMIS (Công việc / HRM / Kế toán Mobile / CRM Mobile) + chuẩn home-care quốc tế (scheduling, EVV, billing-payroll link) + Material 3 Expressive 2026.

## 1. MISA đang làm gì (tóm tắt research)

| Phân hệ MISA | Điểm mạnh | Điểm yếu chí mạng (cơ hội vượt) |
|---|---|---|
| AMIS Công việc | Kanban/Timeline/Dashboard, AI giao việc giọng nói, cảnh báo trễ, báo cáo 360° theo NV/dự án | Chỉ quản việc văn phòng, **không gắn tiền công 30% + vật tư + lương** thành 1 vòng kín |
| AMIS HRM | Chấm công, tính lương, BHXH, thuế TNCN, đánh giá KPI/OKR | Nặng, cần server; **không có lương theo ca y tế + thưởng ca khó + chuyên cần** |
| AMIS Kế toán Mobile | Tra cứu tồn kho, công nợ, báo cáo realtime | **Chủ yếu tra cứu/xem**, không tạo nghiệp vụ kho/lương trên mobile |
| AMIS CRM Mobile | Thêm KH, lên đơn, tra cứu tồn, trả hàng; chi tiết KH có tab Liên quan/Thống kê/Trao đổi, gọi/Zalo/check-in | **Không cảnh báo vượt tồn / vượt hạn mức nợ khi lên đơn**; không snapshot giá |
| AMIS Mobile tổng | Danh bạ, chấm công, AVA hỏi đáp, phê duyệt | Mở app chậm (review 4.4★ phàn nàn), cần mạng |

Kết luận: MISA mạnh ở **liên thông văn phòng**, yếu ở **nghiệp vụ y tế tại nhà offline-first**: ca trực, vật tư tiêu hao theo ca, lương 30%, tuổi nợ theo bệnh nhân.

## 2. Chuẩn home-care quốc tế (để vượt MISA)

1. **Scheduling quyết định**: gợi ý NV theo kỹ năng + chứng chỉ + khoảng cách + ca trống + giới hạn overtime; open-shift ai nhận trước được.
2. **Visit → Billing → Payroll cùng 1 bản ghi đã duyệt**: ca hoàn thành mới được tính tiền; tránh trả lương ca chưa thu.
3. **EVV tối giản offline**: check-in/out + ghi chú + ảnh, có supervisor duyệt ngoại lệ.
4. **Authorization**: gói giờ/tháng của KH, cảnh báo vượt trước khi xếp lịch.
5. **Denial queue**: đơn/claim lỗi có hàng đợi xử lý riêng (áp dụng: đơn nợ quá hạn, kho âm).

## 3. Audit app hiện tại (chấm điểm vs MISA, thang 10)

| Màn | Điểm | Vấn đề cụ thể | MISA có gì hơn |
|---|---|---|---|
| Tổng quan | 4 | Header gradient thô, 4 thẻ `TheSoLieu` chữ nhỏ, “Lối tắt” 6 nút chữ trơn, đơn mới nhất là Card lồng Card | AMIS Bàn làm việc có số + biểu đồ mini + cảnh báo trễ |
| Đơn hàng | 4 | Chỉ mã đơn + tiền, không thấy tên KH, NV, địa chỉ, giờ hẹn; lọc chip 1 hàng | CRM Mobile có tên KH + trạng thái thanh toán/xuất kho |
| Tạo đơn 3 bước | 5 | Đúng luồng nhưng chọn DV dạng Row chữ, không ảnh/giá nổi, vật tư là chip `+` khó hiểu | MISA không cảnh báo vượt tồn — ta phải làm tốt hơn: chặn + gợi ý thay thế |
| Nhiệm vụ | 4 | List chữ, chọn NV bằng 12 AssistChip xếp dọc rất dài; không Kanban, không deadline | AMIS Công việc có Kanban/Timeline + AI gợi ý người |
| Kho | 5 | Có cảnh báo + vốn TB tốt; form nhập kho nằm chung, NCC nhập tay không lưu | Kế toán Mobile chỉ tra cứu — ta đã hơn ở tạo mới, cần thêm lịch sử nhập/xuất |
| Nhân viên | 4 | Chấm công chỉ “hôm nay”, không lưới tháng; tab Cấp bậc/NV bằng chip | AMIS Chấm công có bảng công + đơn từ + phân ca |
| Lương | 5 | Đã hiện tên thật + chốt tốt; nhưng không giải thích công thức, không drill-down | AMIS Tiền lương có diễn giải + lịch sử chi trả |
| Công nợ | 6 | Tốt nhất app: tuổi nợ + quá hạn đỏ; thiếu tên KH, SĐT gọi nhanh, tổng theo KH | CRM Mobile tra cứu nợ từ chi tiết KH — ta thiếu chiều ngược lại |
| Dịch vụ | 4 | List chữ, lọc nhóm 2 hàng chip dài | Hàng hóa MISA có giá + tồn — ta cần thêm “đã bán bao nhiêu” |
| Báo cáo | 5 | Biểu đồ cột gom 3 ngày thô, không bấm vào được; sổ nhóm chỉ chữ | AMIS báo cáo có biểu đồ + drill-down + tạo báo cáo tùy chọn |
| Nav/Theme | 5 | BottomBar 5 tab ổn; Thêm là lưới 2 cột chữ; Sáng/Tối có nhưng màu chưa Expressive | AMIS Mobile có AVA + bảng tin — ta không cần, nhưng cần hero moment |

**Chẩn đoán chung**: thiếu (1) tên người thật ở mọi nơi, (2) số liệu mini/sparkline, (3) Kanban/lịch, (4) drill-down, (5) diễn giải công thức, (6) hero moment Expressive.

## 4. Thiết kế v3 “vượt MISA” (đã chốt, đang落地)

### 4.1 Design system Expressive (y tế, offline)
- Seed `#00796B` giữ; thêm `MauYTe` (6 màu nhóm sổ), `MauTrangThai` thống nhất, shape 10/16/24, hero gradient chỉ 1 nơi (Tổng quan).
- Component mới trong `ThanhPhanChung.kt`: `TheHero`, `HangTienTrinh`, `DuongDanCongThuc`, `OTrangThai` (loading/empty/error/offline), `Sparkline` (vẽ tay bằng Canvas, không thêm lib), `TheCanhBao`.
- Quy tắc: 1 màn ≤ 2 màu nhấn; số tiền luôn `dinhDangTien` + rút gọn `tr/k` trên biểu đồ; dark mode tương phản WCAG AA.

### 4.2 IA mới (không đổi route, đổi cảm giác)
- Tổng quan = **trung tâm điều hành**: hero hôm nay (doanh thu + tiến độ tháng), 4 thẻ có sparkline mini, hàng “Cần làm ngay” (đơn chờ + nợ quá hạn + kho hết + việc trễ) dạng queue có nút 1 chạm, lịch ca hôm nay, top NV/NHóm.
- Đơn hàng: card hiện **tên KH + SĐT gọi được + giờ hẹn + NV phụ trách**; chip lọc + sắp xếp (mới nhất / nợ nhiều / sắp hẹn).
- Nhiệm vụ: thêm **Kanban 4 cột** (Đã giao/Đang làm/Xong/Hủy) + gợi ý NV (ai rảnh + đúng kỹ năng) — bản offline: gợi ý theo số việc đang làm ít nhất.
- Kho: thêm **lịch sử nhập kho** (từ `nhap_kho`) + trị giá kho + top hao nhiều.
- Nhân viên: thêm **lưới chấm công tháng** (30 ô bấm cycle 0→0.5→1) + tổng công/muộn ngay trên đầu.
- Lương: thêm **diễn giải công thức từng dòng** (bấm vào xem `TinhTien.*`) + so với tháng trước.
- Công nợ: group theo KH (tổng nợ + số đơn + nút gọi) + tuổi nợ bucket.
- Dịch vụ: thêm **“đã bán”** (số đơn + doanh thu từ `don_hang_chi_tiet`) mỗi DV.
- Báo cáo: biểu đồ **bấm cột để xem đơn cấu thành** + so sánh tháng trước + lãi gộp thật (DT − vốn vật tư − lương NV).

### 4.3 Tính năng thực dụng vượt MISA (offline-first)
1. **Chặn vượt tồn + gợi ý thay thế** khi tạo đơn (MISA mobile không có).
2. **Chặn thu vượt nợ** (đã có) + gợi ý “thu đúng còn nợ 1 chạm”.
3. **Gợi ý NV rảnh** khi giao việc (ít việc đang làm nhất).
4. **Nhắc nợ 1 chạm**: tạo sẵn tin nhắn “Anh/chị còn nợ X…” để gọi/Zalo.
5. **Khóa kỳ lương** đã có → thêm **so sánh tháng** để thấy tăng/giảm.
6. **Xuất CSV BOM** đã có → thêm **in phiếu thu A5** (text share) cho bệnh nhân ký.
7. **Lịch ca hôm nay** trên Tổng quan từ `nhiem_vu.ngayGiao` (MISA phải mua thêm phân hệ).

### 4.4 Không làm (giữ offline v1)
Server/cloud, login online, thanh toán online, đa chi nhánh realtime, AI giọng nói (để v4 khi có mạng).

## 5. Checklist nghiệm thu v3
- [ ] Mở app < 1.5s, không màn trắng; tiếng Việt 100%.
- [ ] Mọi list có tên người thật + SĐT gọi được; không còn “NV: abc123…”.
- [ ] Biểu đồ bấm được + có so sánh tháng trước.
- [ ] Tạo đơn bị chặn khi tồn không đủ, báo rõ thiếu bao nhiêu.
- [ ] Dark mode đọc được mọi thẻ/biểu đồ.
- [ ] Build + testDebugUnitTest xanh, push `itdoanh`.
