package vn.rinso.qldvyt.domain.pricing

import kotlin.math.roundToLong

/**
 * Công thức tính tiền tập trung của QLDVYT (nguồn chân lý duy nhất).
 * Mọi tiền là Long (VNĐ). Xem docs/03-cong-thuc-tinh-tien.md
 */
object TinhTien {

    /** Tiền NV hưởng trên 1 nhiệm vụ: round(tongGia * tyLeHuong), mặc định 30%. */
    fun tienNvHuong(tongGia: Long, tyLeHuong: Double = 0.30, hoanThanh: Boolean = true): Long {
        if (!hoanThanh || tongGia <= 0) return 0L
        return (tongGia * tyLeHuong.coerceIn(0.0, 1.0)).roundToLong()
    }

    /** Tổng thanh toán đơn hàng = max(0, tongTienHang - giamGia). */
    fun tongThanhToan(tongTienHang: Long, giamGia: Long): Long =
        maxOf(0L, tongTienHang - maxOf(0L, giamGia))

    /** Công nợ đơn = tongThanhToan - daThu. */
    fun conNo(tongThanhToan: Long, daThu: Long): Long = tongThanhToan - daThu

    /** Lợi nhuận gộp đơn = thanh toán - vốn vật tư - tiền NV. */
    fun loiNhuanGop(tongThanhToan: Long, chiPhiVatTu: Long, tienNv: Long): Long =
        tongThanhToan - chiPhiVatTu - tienNv

    /** Lương theo công = round(luongCoBan * tongCong / congChuan). */
    fun luongTheoCong(luongCoBan: Long, tongCong: Double, congChuan: Double = 26.0): Long {
        if (tongCong <= 0 || congChuan <= 0) return 0L
        return (luongCoBan * tongCong / congChuan).roundToLong()
    }

    /** Thực lĩnh tháng. */
    fun thucLinh(
        luongTheoCong: Long,
        tienNhiemVu: Long,
        hoaHong: Long,
        thuong: Long,
        chuyenCan: Long,
        tamUng: Long,
        khauTruKhac: Long,
    ): Long = maxOf(
        0L,
        luongTheoCong + tienNhiemVu + hoaHong + thuong + chuyenCan - tamUng - khauTruKhac
    )

    /** Điều kiện chuyên cần: đủ công chuẩn + không đi muộn. */
    fun duDieuKienChuyenCan(tongCong: Double, congChuan: Double, soNgayDiMuon: Int): Boolean =
        tongCong >= congChuan && soNgayDiMuon == 0

    /** Giá nhập bình quân gia quyền sau mỗi lần nhập kho. */
    fun giaNhapBinhQuan(tonCu: Double, giaCu: Long, slNhap: Double, donGiaNhap: Long): Long {
        val tong = tonCu + slNhap
        if (tong <= 0) return donGiaNhap
        return ((tonCu * giaCu + slNhap * donGiaNhap) / tong).roundToLong()
    }
}
