package vn.rinso.qldvyt.data.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.data.local.BangLuong
import vn.rinso.qldvyt.data.local.DonHang
import vn.rinso.qldvyt.data.local.DonHangChiTiet
import vn.rinso.qldvyt.data.local.KhachHang
import vn.rinso.qldvyt.data.local.NhanVien
import vn.rinso.qldvyt.data.local.ThanhToan

private val VI = Locale("vi", "VN")

/** Dữ liệu đầu trang công ty để in hóa đơn / phiếu lương. */
data class ThongTinCongTy(
    val ten: String = "Công ty Dịch vụ Chăm sóc Sức khỏe Rinso",
    val diaChi: String = "Hà Nội, Việt Nam",
    val sdt: String = "0903 111 000",
    val maSoThue: String = "",
)

/**
 * Xuất PDF offline bằng PdfDocument (không cần mạng, không cần lib ngoài).
 * Font hệ thống Android đã hỗ trợ tiếng Việt có dấu.
 * Ghi qua Storage Access Framework (CreateDocument "application/pdf").
 */
@Singleton
class PdfExporter @Inject constructor(@ApplicationContext private val ctx: Context) {

    private fun paint(co: Int, size: Float, bold: Boolean = false): Paint =
        Paint().apply {
            color = co
            textSize = size
            typeface = if (bold) Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            else Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            isAntiAlias = true
        }

    private fun veTrang(
        doc: PdfDocument, tieuDe: String, cty: ThongTinCongTy,
        dong: List<String>, dongDam: Set<Int> = emptySet(),
        chanTrang: List<String> = emptyList(),
    ) {
        val w = 595; val h = 842 // A4 72dpi
        var page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, 1).create())
        var y = 56f
        val den = 0xFF111111.toInt(); val xam = 0xFF555555.toInt()
        val pTieuDe = paint(0xFF00695C.toInt(), 20f, true)
        val pCty = paint(xam, 11f); val pThuong = paint(den, 12f); val pDam = paint(den, 12f, true)
        page.canvas.drawText(tieuDe, 40f, y, pTieuDe); y += 22f
        page.canvas.drawText(cty.ten, 40f, y, pDam); y += 16f
        page.canvas.drawText("Đ/c: ${cty.diaChi} — ĐT: ${cty.sdt}" + if (cty.maSoThue.isNotBlank()) " — MST: ${cty.maSoThue}" else "", 40f, y, pCty); y += 8f
        page.canvas.drawLine(40f, y, (w - 40).toFloat(), y, Paint().apply { strokeWidth = 2f; color = 0xFF00695C.toInt() })
        y += 20f
        dong.forEachIndexed { i, s ->
            if (y > h - 90) { doc.finishPage(page); page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, 1).create()); y = 56f }
            // Xuống dòng thủ công nếu quá dài
            var line = s; val p = if (i in dongDam) pDam else pThuong
            while (line.length > 72) {
                page.canvas.drawText(line.take(72), 40f, y, p); y += 17f
                line = line.drop(72)
                if (y > h - 90) { doc.finishPage(page); page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, 1).create()); y = 56f }
            }
            page.canvas.drawText(line, 40f, y, p); y += 17f
        }
        if (chanTrang.isNotEmpty()) {
            y += 10f
            chanTrang.forEach { s -> page.canvas.drawText(s, 40f, y, pCty); y += 16f }
        }
        val stamp = SimpleDateFormat("dd/MM/yyyy HH:mm", VI).format(Date())
        page.canvas.drawText("In lúc $stamp — Rinso QLDVYT (offline)", 40f, (h - 40).toFloat(), pCty)
        doc.finishPage(page)
    }

    private fun ghi(doc: PdfDocument, uri: Uri) {
        ctx.contentResolver.openOutputStream(uri, "wt")?.use { doc.writeTo(it) }
        doc.close()
    }

    /** Hóa đơn bán hàng / phiếu thu cho 1 đơn (có VAT theo cài đặt). */
    fun xuatHoaDon(
        uri: Uri, don: DonHang, kh: KhachHang?, chiTiet: List<DonHangChiTiet>,
        lichSuThu: List<ThanhToan>, cty: ThongTinCongTy,
    ) {
        val doc = PdfDocument()
        val dong = mutableListOf<String>()
        dong += "HÓA ĐƠN DỊCH VỤ  ${don.maDh}"
        dong += "Ngày lập: ${dinhDangNgay(don.ngayTao)}" + (don.ngayHen?.let { " — Hẹn: ${dinhDangNgay(it)}" } ?: "")
        dong += "Khách hàng: ${kh?.hoTen ?: "Khách lẻ"} — SĐT: ${kh?.sdt?.ifBlank { "—" } ?: "—"}"
        if (!kh?.diaChi.isNullOrEmpty()) dong += "Địa chỉ: ${kh!!.diaChi}"
        dong += "Trạng thái: ${tenTrangThai(don.trangThai)}"
        dong += "------------------------------------------------------"
        dong += "STT | Dịch vụ | SL | Đơn giá | Thành tiền"
        chiTiet.forEachIndexed { i, ct ->
            val thue = if (ct.chiuThue) "" else " (không VAT)"
            dong += "${i + 1}. ${ct.tenHienThi}$thue — ${ct.soLuong.toInt()} x ${dinhDangTien(ct.donGia)} = ${dinhDangTien(ct.thanhTien)}"
        }
        dong += "------------------------------------------------------"
        dong += "Tổng tiền hàng: ${dinhDangTien(don.tongTienHang)}"
        dong += "Giảm giá: ${dinhDangTien(don.giamGia)}"
        if (don.tienVat > 0) dong += "Thuế VAT (${(don.tyLeVat * 100).toInt()}%): ${dinhDangTien(don.tienVat)}"
        else dong += "Thuế VAT: Không áp dụng"
        dong += "TỔNG THANH TOÁN: ${dinhDangTien(don.tongThanhToan)}"
        dong += "Đã thu: ${dinhDangTien(don.daThu)} — Còn nợ: ${dinhDangTien(don.conNo)}"
        if (lichSuThu.isNotEmpty()) {
            dong += "Lịch sử thu (${lichSuThu.size} lần):"
            lichSuThu.forEach { dong += "- ${dinhDangNgay(it.ngay)}: ${dinhDangTien(it.soTien)} (${if (it.phuongThuc == "CK") "Chuyển khoản" else "Tiền mặt"})" }
        }
        if (don.ghiChu.isNotBlank()) dong += "Ghi chú: ${don.ghiChu}"
        veTrang(doc, "RINSO — HÓA ĐƠN", cty, dong, setOf(0, dong.size - 4),
            listOf("Cảm ơn quý khách đã tin tưởng dịch vụ y tế Rinso!", "Người lập phiếu: ....................   Khách hàng: ...................."))
        ghi(doc, uri)
    }

    /** Phiếu lương chi tiết 1 nhân viên (có thuế TNCN + phụ cấp theo cài đặt). */
    fun xuatPhieuLuong(uri: Uri, bl: BangLuong, nv: NhanVien, capTen: String, cty: ThongTinCongTy) {
        val doc = PdfDocument()
        val thuNhap = bl.luongCoBan + bl.tienNhiemVu + bl.hoaHong + bl.thuong + bl.chuyenCan + bl.phuCap
        val dong = mutableListOf(
            "PHIẾU LƯƠNG THÁNG ${bl.thang}/${bl.nam}",
            "Nhân viên: ${nv.hoTen} — Mã: ${nv.maNv} — Cấp bậc: $capTen",
            "SĐT: ${nv.sdt.ifBlank { "—" }} — Ngày vào làm: ${dinhDangNgay(nv.ngayVaoLam)}",
            "------------------------------------------------------",
            "Lương theo công (${bl.tongCong} công): ${dinhDangTien(bl.luongCoBan)}",
            "Tiền nhiệm vụ 30%: ${dinhDangTien(bl.tienNhiemVu)}",
            "Hoa hồng: ${dinhDangTien(bl.hoaHong)}",
            "Thưởng: ${dinhDangTien(bl.thuong)}",
            "Chuyên cần: ${dinhDangTien(bl.chuyenCan)}",
            "Phụ cấp: ${dinhDangTien(bl.phuCap)}",
            "Tổng thu nhập: ${dinhDangTien(thuNhap)}",
            "Thuế TNCN khấu trừ: ${dinhDangTien(bl.thueTncn)}",
            "Tạm ứng: ${dinhDangTien(bl.tamUng)}",
            "Khấu trừ khác (phạt...): ${dinhDangTien(bl.khauTruKhac)}",
            "------------------------------------------------------",
            "THỰC LĨNH: ${dinhDangTien(bl.thucLinh)}",
            "Trạng thái: ${if (bl.trangThai == "CHOT") "ĐÃ CHỐT" else "Bản nháp"}",
        )
        veTrang(doc, "RINSO — PHIẾU LƯƠNG", cty, dong, setOf(0, dong.size - 2),
            listOf("Kế toán: ....................   Nhân viên: ...................."))
        ghi(doc, uri)
    }

    /** Bảng lương tổng hợp cả công ty trong tháng. */
    fun xuatBangLuongThang(uri: Uri, thang: Int, nam: Int, cacDong: List<Triple<NhanVien, BangLuong, String>>, cty: ThongTinCongTy) {
        val doc = PdfDocument()
        val dong = mutableListOf("BẢNG LƯƠNG TỔNG HỢP THÁNG $thang/$nam (${cacDong.size} NV)")
        var tong = 0L
        cacDong.forEachIndexed { i, (nv, bl, _) ->
            dong += "${i + 1}. ${nv.hoTen} (${nv.maNv}) — Công ${bl.tongCong} — NV30 ${dinhDangTien(bl.tienNhiemVu)} — Thực lĩnh ${dinhDangTien(bl.thucLinh)}${if (bl.thueTncn > 0) " (TNCN ${dinhDangTien(bl.thueTncn)})" else ""}"
            tong += bl.thucLinh
        }
        dong += "------------------------------------------------------"
        dong += "TỔNG QUỸ LƯƠNG: ${dinhDangTien(tong)}"
        veTrang(doc, "RINSO — BẢNG LƯƠNG", cty, dong, setOf(0, dong.size - 1), emptyList())
        ghi(doc, uri)
    }

    private fun tenTrangThai(tt: String) = when (tt) {
        "CHO_XU_LY" -> "Chờ xử lý"; "DANG_LAM" -> "Đang làm"
        "HOAN_THANH" -> "Hoàn thành"; "HUY" -> "Đã hủy"; else -> tt
    }
}
