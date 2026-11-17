# Skill: tinh-tien-qldvyt (Pricing skill)

Dùng khi agent tính toán bất kỳ khoản tiền nào trong QLDVYT.

## Công thức chuẩn (Long VNĐ, round sau mỗi bước nhân tỷ lệ)
- `tienNvHuong = round(tongGia * tyLeHuong)` (mặc định 0.30, chỉ khi HOANTHANH)
- `tongThanhToan = max(0, tongTienHang - giamGia)`; `conNo = tongThanhToan - daThu`
- `loiNhuanGop = tongThanhToan - chiPhiVatTu - Σ tienNvHuong`
- `luongTheoCong = round(luongCoBan * tongCong / congChuan)` (mặc định 26)
- `thucLinh = max(0, luongTheoCong + tienNhiemVu + hoaHong + thuong + chuyenCan - tamUng - khauTru)`
- `giaNhapTB = (tonCu*giaCu + slNhap*donGiaNhap)/(tonCu+slNhap)`

## Quy tắc
1. Chỉ sửa `domain/pricing/TinhTien.kt`, không tính lẻ ở UI/DAO.
2. Mọi thay đổi phải kèm unit test trong `TinhTienTest.kt`.
3. Nguồn chân lý: `docs/03-cong-thuc-tinh-tien.md`.
