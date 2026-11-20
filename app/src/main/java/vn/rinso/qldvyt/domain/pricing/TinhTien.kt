package vn.rinso.qldvyt.domain.pricing

import kotlin.math.roundToLong

/**
 * Công thức tính tiền tập trung của QLDVYT (nguồn chân lý duy nhất).
 * Mọi tiền là Long (VNĐ). Xem docs/03-cong-thuc-tinh-tien.md
 *
 * Quy ước thuế VAT đơn hàng:
 *   tienTruocVat = max(0, tongTienHang - giamGia)
 *   tienVat      = round(tienTruocVat * tyLeVat) nếu bật VAT, ngược lại 0
 *   tongThanhToan = tienTruocVat + tienVat
 * Nếu tính theo từng dịch vụ: tienVat = Σ round(thanhTien_i * tyLeVat) với i chịu thuế.
 */
object TinhTien {

    /** Tiền NV hưởng trên 1 nhiệm vụ: round(tongGia * tyLeHuong), mặc định 30%. */
    fun tienNvHuong(tongGia: Long, tyLeHuong: Double = 0.30, hoanThanh: Boolean = true): Long {
        if (!hoanThanh || tongGia <= 0) return 0L
        return (tongGia * tyLeHuong.coerceIn(0.0, 1.0)).roundToLong()
    }

    /** Tổng thanh toán đơn hàng KHÔNG VAT (giữ tương thích cũ). */
    fun tongThanhToan(tongTienHang: Long, giamGia: Long): Long =
        maxOf(0L, tongTienHang - maxOf(0L, giamGia))

    /** Tiền hàng trước VAT sau giảm giá. */
    fun tienTruocVat(tongTienHang: Long, giamGia: Long): Long =
        maxOf(0L, tongTienHang - maxOf(0L, giamGia))

    /** Tiền VAT của đơn (gộp cả đơn). Tắt VAT hoặc tỷ lệ <= 0 thì trả 0. */
    fun tienVatDon(tienTruocVat: Long, tyLeVat: Double, batVat: Boolean): Long {
        if (!batVat || tienTruocVat <= 0 || tyLeVat <= 0) return 0L
        return (tienTruocVat * tyLeVat.coerceIn(0.0, 0.2)).roundToLong()
    }

    /** Tiền VAT tính theo từng dòng (chỉ dòng chịu thuế mới cộng). */
    fun tienVatTheoDong(
        cacDong: List<Pair<Long, Boolean>>,
        tyLeVat: Double,
        batVat: Boolean,
    ): Long {
        if (!batVat || tyLeVat <= 0) return 0L
        val tyLe = tyLeVat.coerceIn(0.0, 0.2)
        return cacDong.filter { it.second }.sumOf { (thanhTien, _) ->
            if (thanhTien <= 0) 0L else (thanhTien * tyLe).roundToLong()
        }
    }

    /** Tổng thanh toán đã gồm VAT. */
    fun tongThanhToanCoVat(tienTruocVat: Long, tienVat: Long): Long =
        maxOf(0L, tienTruocVat + maxOf(0L, tienVat))

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

    /** Thuế TNCN khấu trừ trên thu nhập chịu thuế (nếu bật). */
    fun thueTncn(thuNhapChiuThue: Long, tyLe: Double, bat: Boolean): Long {
        if (!bat || thuNhapChiuThue <= 0 || tyLe <= 0) return 0L
        return (thuNhapChiuThue * tyLe.coerceIn(0.0, 0.35)).roundToLong()
    }

    /** Thực lĩnh tháng (có thuế TNCN khấu trừ). */
    fun thucLinh(
        luongTheoCong: Long,
        tienNhiemVu: Long,
        hoaHong: Long,
        thuong: Long,
        chuyenCan: Long,
        tamUng: Long,
        khauTruKhac: Long,
        thueTncn: Long = 0L,
    ): Long = maxOf(
        0L,
        luongTheoCong + tienNhiemVu + hoaHong + thuong + chuyenCan - tamUng - khauTruKhac - maxOf(0L, thueTncn)
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
