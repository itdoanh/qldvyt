package vn.rinso.qldvyt.data.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
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

    /** Cắt ngắn chữ cho vừa khung để không tràn/chồng lấn (PDF không tự wrap). */
    private fun vuaKhung(s: String, p: Paint, maxRong: Float): String {
        var t = s.trim()
        if (p.measureText(t) <= maxRong) return t
        while (t.length > 4 && p.measureText("$t…") > maxRong) t = t.dropLast(1)
        return "$t…"
    }

    private fun veTrai(c: android.graphics.Canvas, s: String, x: Float, y: Float, p: Paint, maxRong: Float) {
        c.drawText(vuaKhung(s, p, maxRong), x, y, p)
    }

    /** Vẽ tiền căn phải trong ô [xTrai, xPhai]. */
    private fun vePhai(c: android.graphics.Canvas, s: String, xTrai: Float, xPhai: Float, y: Float, p: Paint) {
        val t = vuaKhung(s, p, xPhai - xTrai - 6f)
        c.drawText(t, xPhai - p.measureText(t) - 6f, y, p)
    }

    private fun veTrang(
        doc: PdfDocument, tieuDe: String, cty: ThongTinCongTy,
        dong: List<String>, dongDam: Set<Int> = emptySet(),
        chanTrang: List<String> = emptyList(),
    ) {
        val w = 595; val h = 842 // A4 72dpi
        var soTrang = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, soTrang).create())
        var y = 56f
        val den = 0xFF111111.toInt(); val xam = 0xFF555555.toInt()
        val pTieuDe = paint(0xFF00695C.toInt(), 20f, true)
        val pCty = paint(xam, 11f); val pThuong = paint(den, 12f); val pDam = paint(den, 12f, true)
        fun trangMoi() {
            doc.finishPage(page)
            soTrang += 1
            page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, soTrang).create())
            y = 56f
        }
        page.canvas.drawText(tieuDe, 40f, y, pTieuDe); y += 22f
        page.canvas.drawText(cty.ten, 40f, y, pDam); y += 16f
        page.canvas.drawText("Đ/c: ${cty.diaChi} — ĐT: ${cty.sdt}" + if (cty.maSoThue.isNotBlank()) " — MST: ${cty.maSoThue}" else "", 40f, y, pCty); y += 8f
        page.canvas.drawLine(40f, y, (w - 40).toFloat(), y, Paint().apply { strokeWidth = 2f; color = 0xFF00695C.toInt() })
        y += 20f
        dong.forEachIndexed { i, s ->
            if (y > h - 90) { trangMoi() }
            // Xuống dòng thủ công nếu quá dài
            var line = s; val p = if (i in dongDam) pDam else pThuong
            while (line.length > 72) {
                page.canvas.drawText(line.take(72), 40f, y, p); y += 17f
                line = line.drop(72)
                if (y > h - 90) { trangMoi() }
            }
            page.canvas.drawText(line, 40f, y, p); y += 17f
        }
        if (chanTrang.isNotEmpty()) {
            y += 10f
            if (y > h - 110) { trangMoi() }
            chanTrang.forEach { s -> page.canvas.drawText(s, 40f, y, pCty); y += 16f }
        }
        val stamp = SimpleDateFormat("dd/MM/yyyy HH:mm", VI).format(Date())
        page.canvas.drawText("In lúc $stamp — Rinso QLDVYT (offline) — Trang $soTrang", 40f, (h - 40).toFloat(), pCty)
        doc.finishPage(page)
    }

    private fun ghi(doc: PdfDocument, uri: Uri) {
        // SAF file nhị phân (PDF): dùng openOutputStream(mode "w"), writeTo + flush + fd.sync.
        // Không dùng "wt" (text) — nhiều máy ra file 0KB. Page number tăng dần để PDF nhiều trang không bị trắng.
        require(doc.pages.isNotEmpty()) { "PDF chưa có trang nào — không ghi file rỗng" }
        try {
            ctx.contentResolver.openOutputStream(uri, "w")?.use { out ->
                java.io.BufferedOutputStream(out).use { buf ->
                    doc.writeTo(buf)
                    buf.flush()
                }
                try { out.flush() } catch (_: Exception) { }
            } ?: error("Không mở được file để ghi PDF")
        } finally {
            try { doc.close() } catch (_: Exception) { }
        }
    }

    private fun ghiFile(doc: PdfDocument, file: File) {
        require(doc.pages.isNotEmpty()) { "PDF chưa có trang nào — không ghi file rỗng" }
        try {
            file.parentFile?.mkdirs()
            java.io.FileOutputStream(file).use { fos ->
                java.io.BufferedOutputStream(fos).use { buf ->
                    doc.writeTo(buf)
                    buf.flush()
                }
                try { fos.fd.sync() } catch (_: Exception) { }
            }
            require(file.exists() && file.length() > 0) { "Ghi PDF thất bại (file rỗng)" }
        } finally {
            try { doc.close() } catch (_: Exception) { }
        }
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

    // ============ PHIẾU LƯƠNG KIỂU DOANH NGHIỆP VN ============
    /** Dựng phiếu lương chi tiết 1 nhân viên — bố cục chuẩn công ty lớn tại VN. */
    fun dungPhieuLuong(bl: BangLuong, nv: NhanVien, capTen: String, cty: ThongTinCongTy): PdfDocument {
        val doc = PdfDocument()
        val w = 595; val h = 842
        var soTrang = 1
        var page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, soTrang).create())
        val c = page.canvas
        val den = 0xFF111111.toInt(); val xam = 0xFF444444.toInt()
        val xanh = 0xFF0D47A1.toInt(); val xanhNhat = 0xFFE3F2FD.toInt()
        val pTenCty = paint(xanh, 13f, true)
        val pCtyNho = paint(xam, 10f)
        val pTieuDe = paint(xanh, 19f, true)
        val pPhuDe = paint(xam, 11f)
        val pTb = paint(den, 11f); val pTbDam = paint(den, 11f, true)
        val pNho = paint(xam, 10f)
        val pTien = paint(den, 11f); val pTienDam = paint(den, 12f, true)
        val pThucLinh = paint(0xFFB71C1C.toInt(), 16f, true)
        val vien = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1.2f; color = 0xFF90A4AE.toInt() }
        val vienDam = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 1.8f; color = xanh }
        val nenTieuDe = Paint().apply { style = Paint.Style.FILL; color = xanhNhat }
        val nenThucLinh = Paint().apply { style = Paint.Style.FILL; color = 0xFFFFF3E0.toInt() }

        var y = 48f
        val le = 40f; val phai = (w - 40).toFloat()
        // --- Đầu trang công ty ---
        c.drawText(cty.ten.uppercase(VI), le, y, pTenCty); y += 15f
        c.drawText("Đ/c: ${cty.diaChi}  •  ĐT: ${cty.sdt}" + if (cty.maSoThue.isNotBlank()) "  •  MST: ${cty.maSoThue}" else "", le, y, pCtyNho); y += 6f
        c.drawLine(le, y, phai, y, Paint().apply { strokeWidth = 2.5f; color = xanh }); y += 18f
        // --- Tiêu đề phiếu ---
        c.drawText("PHIẾU THANH TOÁN TIỀN LƯƠNG", le, y, pTieuDe); y += 18f
        c.drawText("Tháng ${bl.thang}/${bl.nam}  •  Trạng thái: ${if (bl.trangThai == "CHOT") "ĐÃ CHỐT" else "BẢN NHÁP"}  •  Ngày in: ${SimpleDateFormat("dd/MM/yyyy", VI).format(Date())}", le, y, pPhuDe); y += 16f

        // --- Khung thông tin nhân viên (2 cột) ---
        val caoNV = 86f
        c.drawRect(le, y, phai, y + caoNV, vienDam)
        c.drawLine((le + phai) / 2, y, (le + phai) / 2, y + caoNV, vien)
        c.drawLine(le, y + caoNV / 2.75f, phai, y + caoNV / 2.75f, vien)
        c.drawLine(le, y + caoNV * 2 / 2.75f, phai, y + caoNV * 2 / 2.75f, vien)
        var ynv = y + 15f
        val giua = (le + phai) / 2 + 8f
        val rongTrai = (le + phai) / 2 - le - 14f
        val rongPhai = phai - giua - 8f
        veTrai(c, "Mã NV: ${nv.maNv}", le + 8f, ynv, pTbDam, rongTrai)
        veTrai(c, "Họ và tên: ${nv.hoTen}", giua, ynv, pTbDam, rongPhai); ynv += 20f
        veTrai(c, "Chức vụ: ${capTen.ifBlank { "—" }}", le + 8f, ynv, pTb, rongTrai)
        veTrai(c, "SĐT: ${nv.sdt.ifBlank { "—" }}", giua, ynv, pTb, rongPhai); ynv += 20f
        veTrai(c, "STK: ${nv.stk.ifBlank { "—" }}", le + 8f, ynv, pTb, rongTrai)
        veTrai(c, "Tổng công: ${bl.tongCong} công", giua, ynv, pTb, rongPhai); ynv += 20f
        veTrai(c, "Ngày vào làm: ${dinhDangNgay(nv.ngayVaoLam)}", le + 8f, ynv, pNho, rongTrai + rongPhai)
        y += caoNV + 14f

        // --- Bảng thu nhập / khấu trừ (2 khối) ---
        val thuNhap = bl.luongCoBan + bl.tienNhiemVu + bl.hoaHong + bl.thuong + bl.chuyenCan + bl.phuCap
        val khauTru = bl.tamUng + bl.khauTruKhac + bl.thueTncn
        val hangTN = listOf(
            "Lương theo công (${bl.tongCong} công)" to bl.luongCoBan,
            "Tiền nhiệm vụ (30%)" to bl.tienNhiemVu,
            "Hoa hồng" to bl.hoaHong,
            "Thưởng" to bl.thuong,
            "Chuyên cần" to bl.chuyenCan,
            "Phụ cấp" to bl.phuCap,
        )
        val hangKT = listOf(
            "Tạm ứng" to bl.tamUng,
            "Phạt / khấu trừ khác" to bl.khauTruKhac,
            "Thuế TNCN" to bl.thueTncn,
        )
        fun veKhoi(tieuDe: String, hang: List<Pair<String, Long>>, tong: Long, tongNhan: String): Float {
            val caoHang = 21f; val caoHead = 24f
            val cao = caoHead + hang.size * caoHang + caoHang
            val cotTienX = le + (phai - le) * 0.60f // đường chia cột tiền — mọi tiền căn phải sau vạch này
            // nền + viền
            c.drawRect(le, y, phai, y + cao, vien)
            c.drawRect(le, y, phai, y + caoHead, nenTieuDe)
            c.drawLine(le, y + caoHead, phai, y + caoHead, vien)
            c.drawLine(cotTienX, y, cotTienX, y + cao, vien)
            veTrai(c, tieuDe, le + 10f, y + 16f, pTbDam, cotTienX - le - 14f)
            vePhai(c, "Số tiền (VNĐ)", cotTienX, phai, y + 16f, pTbDam)
            var yy = y + caoHead + 15f
            hang.forEach { (ten, tien) ->
                veTrai(c, ten, le + 10f, yy, pTb, cotTienX - le - 14f)
                vePhai(c, dinhDangTien(tien), cotTienX, phai, yy, pTien)
                c.drawLine(le, yy + 6f, phai, yy + 6f, Paint().apply { strokeWidth = 0.6f; color = 0xFFCFD8DC.toInt() })
                yy += caoHang
            }
            c.drawRect(le, yy - 15f, phai, yy + 6f, Paint().apply { style = Paint.Style.FILL; color = 0xFFF5F5F5.toInt() })
            veTrai(c, tongNhan, le + 10f, yy, pTbDam, cotTienX - le - 14f)
            vePhai(c, dinhDangTien(tong), cotTienX, phai, yy, pTienDam)
            return cao + 12f
        }
        y += veKhoi("A. CÁC KHOẢN THU NHẬP", hangTN, thuNhap, "Tổng thu nhập")
        y += veKhoi("B. CÁC KHOẢN KHẤU TRỪ", hangKT, khauTru, "Tổng khấu trừ")

        // --- Thực lĩnh nổi bật ---
        val caoTL = 44f
        c.drawRect(le, y, phai, y + caoTL, vienDam)
        c.drawRect(le, y, phai, y + caoTL, nenThucLinh)
        veTrai(c, "C. THỰC LĨNH:", le + 10f, y + 27f, paint(den, 13f, true), 200f)
        vePhai(c, dinhDangTien(bl.thucLinh), le + 200f, phai, y + 28f, pThucLinh)
        y += caoTL + 8f
        // Bằng chữ: tự xuống dòng nếu dài để không tràn lề
        val bangChu = "Bằng chữ: ${docTienBangChu(bl.thucLinh)}."
        if (pNho.measureText(bangChu) <= phai - le) {
            veTrai(c, bangChu, le, y, pNho, phai - le); y += 18f
        } else {
            // cắt thành 2 dòng theo từ
            val tu = bangChu.split(" "); var d1 = ""; var i = 0
            while (i < tu.size && pNho.measureText((if (d1.isEmpty()) "" else "$d1 ") + tu[i]) <= phai - le) {
                d1 = if (d1.isEmpty()) tu[i] else "$d1 ${tu[i]}"; i++
            }
            val d2 = tu.drop(i).joinToString(" ")
            veTrai(c, d1, le, y, pNho, phai - le); y += 14f
            if (d2.isNotEmpty()) { veTrai(c, d2, le, y, pNho, phai - le); y += 14f }
            y += 4f
        }

        // --- Ký tên 3 bên (căn giữa từng cột, không chồng nhau) ---
        c.drawText("Xác nhận của các bên (ký, ghi rõ họ tên):", le, y, pTbDam); y += 18f
        val cotKy = floatArrayOf(le + (phai - le) * 0.17f, le + (phai - le) * 0.50f, le + (phai - le) * 0.83f)
        val nhanKy = listOf("Người lập phiếu", "Kế toán", "Nhân viên")
        nhanKy.forEachIndexed { idx, s -> c.drawText(s, cotKy[idx] - pTb.measureText(s) / 2f, y, pTb) }
        y += 52f
        repeat(3) { idx ->
            val s = "(Ký, họ tên)"
            c.drawText(s, cotKy[idx] - pNho.measureText(s) / 2f, y, pNho)
        }
        val stamp = SimpleDateFormat("dd/MM/yyyy HH:mm", VI).format(Date())
        c.drawText("In lúc $stamp — Rinso QLDVYT (offline) — Trang $soTrang", le, (h - 36).toFloat(), pCtyNho)
        doc.finishPage(page)
        return doc
    }

    /** Xuất phiếu lương ra SAF Uri (giữ tương thích màn Lương cũ). */
    fun xuatPhieuLuong(uri: Uri, bl: BangLuong, nv: NhanVien, capTen: String, cty: ThongTinCongTy) {
        ghi(dungPhieuLuong(bl, nv, capTen, cty), uri)
    }

    /** Xuất phiếu lương ra file local để mở xem ngay trong app. */
    fun xuatPhieuLuong(file: File, bl: BangLuong, nv: NhanVien, capTen: String, cty: ThongTinCongTy): File {
        ghiFile(dungPhieuLuong(bl, nv, capTen, cty), file)
        return file
    }

    // ============ BẢNG TỔNG HỢP LƯƠNG THÁNG (CHI TIẾT FULL) ============
    /** Dựng bảng lương tổng hợp cả công ty — bảng kẻ chi tiết từng NV + tổng từng cột + ngày in. */
    fun dungBangLuongThang(thang: Int, nam: Int, cacDong: List<Triple<NhanVien, BangLuong, String>>, cty: ThongTinCongTy): PdfDocument {
        val doc = PdfDocument()
        val w = 595; val h = 842
        val den = 0xFF111111.toInt(); val xam = 0xFF444444.toInt(); val xanh = 0xFF0D47A1.toInt()
        val pTenCty = paint(xanh, 12f, true)
        val pNho = paint(xam, 9.5f)
        val pTieuDe = paint(xanh, 16f, true)
        val pHead = paint(0xFFFFFFFF.toInt(), 8.5f, true)
        val pO = paint(den, 8.5f)
        val pODam = paint(den, 9f, true)
        val pTong = paint(0xFFB71C1C.toInt(), 10f, true)
        val vien = Paint().apply { style = Paint.Style.STROKE; strokeWidth = 0.9f; color = 0xFF78909C.toInt() }
        val nenHead = Paint().apply { style = Paint.Style.FILL; color = xanh }
        val nenTong = Paint().apply { style = Paint.Style.FILL; color = 0xFFFFF3E0.toInt() }

        val le = 24f; val phai = (w - 20).toFloat()
        val rong = phai - le
        // STT24 Ten85 Cong30 LCB50 NV50 Thuong50 CCPC50 TamUng50 Phat38 TNCN38 ThucLinh50 = 514
        val cot = floatArrayOf(24f, 105f, 30f, 48f, 48f, 48f, 48f, 48f, 36f, 36f, 64f)
        val tyLe = rong / cot.sum()
        val wCot = cot.map { it * tyLe }.toFloatArray()
        val tieuDeCot = listOf("STT", "Họ và tên", "Công", "Lương\ncông", "NV\n30%", "Thưởng\n+HH", "CCần\n+PC", "Tạm\nứng", "Phạt", "TNCN", "Thực lĩnh")
        val caoHead = 30f; val caoDong = 18f

        var soTrang = 0
        var page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, ++soTrang).create())
        var c = page.canvas
        var y = 40f
        fun headerTrang(dau: Boolean) {
            if (!dau) { doc.finishPage(page); page = doc.startPage(PdfDocument.PageInfo.Builder(w, h, ++soTrang).create()); c = page.canvas; y = 40f }
            veTrai(c, cty.ten.uppercase(VI), le, y, pTenCty, rong); y += 13f
            veTrai(c, "Đ/c: ${cty.diaChi} • ĐT: ${cty.sdt}" + if (cty.maSoThue.isNotBlank()) " • MST: ${cty.maSoThue}" else "", le, y, pNho, rong); y += 13f
            veTrai(c, "BẢNG LƯƠNG THÁNG $thang/$nam (${cacDong.size} NV)", le, y, pTieuDe, rong); y += 15f
            veTrai(c, "Ngày in: ${SimpleDateFormat("dd/MM/yyyy HH:mm", VI).format(Date())}  •  ĐVT: VNĐ", le, y, pNho, rong); y += 12f
            // vẽ header bảng
            var x = le
            c.drawRect(le, y, phai, y + caoHead, nenHead)
            tieuDeCot.forEachIndexed { i, t ->
                val parts = t.split("\n")
                if (parts.size == 1) veTrai(c, t, x + 3f, y + 19f, pHead, wCot[i] - 5f)
                else { veTrai(c, parts[0], x + 3f, y + 12f, pHead, wCot[i] - 5f); veTrai(c, parts[1], x + 3f, y + 22f, pHead, wCot[i] - 5f) }
                x += wCot[i]
                c.drawLine(x, y, x, y + caoHead, Paint().apply { strokeWidth = 0.9f; color = 0xFFFFFFFF.toInt() })
            }
            y += caoHead
        }
        headerTrang(true)
        // Vẽ từng dòng NV
        var stt = 0
        var tCong = 0.0; var tLcb = 0L; var tNv = 0L; var tThuong = 0L; var tCcPc = 0L; var tUng = 0L; var tPhat = 0L; var tTncn = 0L; var tTl = 0L
        cacDong.forEach { (nv, bl, _) ->
            if (y + caoDong > h - 160) headerTrang(false)
            stt += 1
            tCong += bl.tongCong; tLcb += bl.luongCoBan; tNv += bl.tienNhiemVu
            tThuong += bl.thuong + bl.hoaHong; tCcPc += bl.chuyenCan + bl.phuCap
            tUng += bl.tamUng; tPhat += bl.khauTruKhac; tTncn += bl.thueTncn; tTl += bl.thucLinh
            val o = listOf(
                "$stt", nv.hoTen.take(16), "${bl.tongCong}",
                rutGon(bl.luongCoBan), rutGon(bl.tienNhiemVu), rutGon(bl.thuong + bl.hoaHong),
                rutGon(bl.chuyenCan + bl.phuCap), rutGon(bl.tamUng), rutGon(bl.khauTruKhac),
                rutGon(bl.thueTncn), rutGon(bl.thucLinh),
            )
            var x = le
            o.forEachIndexed { i, s ->
                val p = if (i == 10) pODam else pO
                if (i <= 1) veTrai(c, s, x + 3f, y + 13f, p, wCot[i] - 5f)
                else vePhai(c, s, x, x + wCot[i], y + 13f, p)
                x += wCot[i]
            }
            // kẻ khung dòng
            var xx = le
            c.drawRect(le, y, phai, y + caoDong, vien)
            wCot.forEach { wc -> xx += wc; c.drawLine(xx, y, xx, y + caoDong, vien) }
            y += caoDong
        }
        // Dòng tổng cộng
        if (y + caoDong > h - 150) headerTrang(false)
        var x = le
        val oTong = listOf("Σ", "${cacDong.size} NV", "${"%.1f".format(tCong)}", rutGon(tLcb), rutGon(tNv), rutGon(tThuong), rutGon(tCcPc), rutGon(tUng), rutGon(tPhat), rutGon(tTncn), rutGon(tTl))
        c.drawRect(le, y, phai, y + caoDong + 4f, nenTong)
        oTong.forEachIndexed { i, s ->
            if (i <= 1) veTrai(c, s, x + 3f, y + 14f, pTong, wCot[i] - 5f)
            else vePhai(c, s, x, x + wCot[i], y + 14f, pTong)
            x += wCot[i]
        }
        var xx = le
        c.drawRect(le, y, phai, y + caoDong + 4f, vien)
        wCot.forEach { wc -> xx += wc; c.drawLine(xx, y, xx, y + caoDong + 4f, vien) }
        y += caoDong + 16f
        // Khối tổng hợp chi tiết
        if (y > h - 170) headerTrang(false)
        veTrai(c, "TỔNG HỢP CHI TIẾT THÁNG $thang/$nam:", le, y, pODam, rong); y += 15f
        val chiTiet = listOf(
            "Tổng quỹ lương thực lĩnh: ${dinhDangTien(tTl)}  (${cacDong.size} nhân viên)",
            "Tổng lương theo công: ${dinhDangTien(tLcb)}  •  NV 30%: ${dinhDangTien(tNv)}",
            "Thưởng + HH: ${dinhDangTien(tThuong)}  •  CCần + PC: ${dinhDangTien(tCcPc)}",
            "Tạm ứng: ${dinhDangTien(tUng)}  •  Phạt: ${dinhDangTien(tPhat)}  •  TNCN: ${dinhDangTien(tTncn)}",
            "Bình quân: ${dinhDangTien(if (cacDong.isNotEmpty()) tTl / cacDong.size else 0L)}/NV  •  Tổng công: ${"%.1f".format(tCong)}",
        )
        chiTiet.forEach { s -> veTrai(c, "• $s", le, y, pO, rong); y += 14f }
        y += 10f
        val cotKyT = floatArrayOf(le + rong * 0.17f, le + rong * 0.50f, le + rong * 0.83f)
        listOf("Người lập bảng", "Kế toán trưởng", "Giám đốc").forEachIndexed { idx, s ->
            c.drawText(s, cotKyT[idx] - pO.measureText(s) / 2f, y, pO)
        }
        y += 44f
        repeat(3) { idx ->
            val s = if (idx == 2) "(Ký, đóng dấu)" else "(Ký, họ tên)"
            c.drawText(s, cotKyT[idx] - pNho.measureText(s) / 2f, y, pNho)
        }
        val stampTong = SimpleDateFormat("dd/MM/yyyy HH:mm", VI).format(Date())
        veTrai(c, "In lúc $stampTong — Rinso QLDVYT (offline) — Trang $soTrang", le, (h - 30).toFloat(), pNho, rong)
        doc.finishPage(page)
        return doc
    }

    /** Xuất bảng tổng hợp ra SAF Uri (giữ tương thích). */
    fun xuatBangLuongThang(uri: Uri, thang: Int, nam: Int, cacDong: List<Triple<NhanVien, BangLuong, String>>, cty: ThongTinCongTy) {
        ghi(dungBangLuongThang(thang, nam, cacDong, cty), uri)
    }

    /** Xuất bảng tổng hợp ra file local để mở xem ngay trong app. */
    fun xuatBangLuongThang(file: File, thang: Int, nam: Int, cacDong: List<Triple<NhanVien, BangLuong, String>>, cty: ThongTinCongTy): File {
        ghiFile(dungBangLuongThang(thang, nam, cacDong, cty), file)
        return file
    }

    /** Rút gọn số tiền cho vừa cột bảng tổng hợp (tr, k). */
    private fun rutGon(tien: Long): String = when {
        tien == 0L -> "0"
        tien >= 1_000_000L -> {
            val tr = tien / 1_000_000L; val le = (tien % 1_000_000L) / 100_000L
            if (le == 0L) "${tr}tr" else "${tr},${le}tr"
        }
        tien >= 1_000L -> "${tien / 1_000L}k"
        else -> tien.toString()
    }

    /** Đọc số tiền bằng chữ (rút gọn, đủ dùng cho phiếu lương). */
    private fun docTienBangChu(tien: Long): String {
        if (tien <= 0L) return "Không đồng"
        val donVi = arrayOf("", "nghìn", "triệu", "tỷ")
        fun doc3(n: Int): String {
            val tram = n / 100; val chuc = (n % 100) / 10; val dv = n % 10
            val so = arrayOf("không", "một", "hai", "ba", "bốn", "năm", "sáu", "bảy", "tám", "chín")
            var s = ""
            if (tram > 0) s += "${so[tram]} trăm "
            if (chuc > 1) { s += "${so[chuc]} mươi "; s += if (dv == 1) "mốt " else if (dv == 5) "lăm " else if (dv > 0) "${so[dv]} " else "" }
            else if (chuc == 1) { s += "mười "; s += if (dv == 5) "lăm " else if (dv > 0) "${so[dv]} " else "" }
            else if (dv > 0) { if (tram > 0) s += "lẻ "; s += "${so[dv]} " }
            return s
        }
        var t = tien; var i = 0; var kq = ""
        while (t > 0 && i < donVi.size) {
            val n = (t % 1000).toInt()
            if (n > 0) kq = "${doc3(n)}${donVi[i]} $kq"
            t /= 1000; i++
        }
        return (kq.trim().replaceFirstChar { it.uppercase() } + " đồng").replace("  ", " ")
    }

    private fun tenTrangThai(tt: String) = when (tt) {
        "CHO_XU_LY" -> "Chờ xử lý"; "DANG_LAM" -> "Đang làm"
        "HOAN_THANH" -> "Hoàn thành"; "HUY" -> "Đã hủy"; else -> tt
    }
}
