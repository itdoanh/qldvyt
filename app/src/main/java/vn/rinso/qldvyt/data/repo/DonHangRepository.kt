package vn.rinso.qldvyt.data.repo

import androidx.room.withTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import vn.rinso.qldvyt.data.local.DonHang
import vn.rinso.qldvyt.data.local.DonHangChiTiet
import vn.rinso.qldvyt.data.local.DonHangVatTu
import vn.rinso.qldvyt.data.local.QldvytDatabase
import vn.rinso.qldvyt.data.local.ThanhToan
import vn.rinso.qldvyt.domain.pricing.TinhTien

/** Dòng hàng khi tạo đơn: chọn dịch vụ hoặc gói (snapshot giá lúc bán). */
data class MatHangMoi(
    val dichVuId: String? = null,
    val goiId: String? = null,
    val tenHienThi: String,
    val soLuong: Double = 1.0,
    val donGia: Long,
    /** Dòng này có chịu VAT không (mặc định theo dịch vụ). */
    val chiuThue: Boolean = true,
)

/** Vật tư dùng cho đơn: trừ tồn kho + snapshot giá vốn. */
data class VatTuDungMoi(
    val vatTuId: String,
    val soLuong: Double,
)

/**
 * Repository đơn hàng: tạo đơn, thu tiền, đổi trạng thái — mọi ghi tiền
 * chạy trong transaction + recompute daThu/conNo/tonKho.
 * Công thức duy nhất từ TinhTien (docs/03).
 */
@Singleton
class DonHangRepository @Inject constructor(
    private val db: QldvytDatabase,
    private val caiDat: vn.rinso.qldvyt.data.prefs.CaiDat,
) {

    fun moiNhat(): Flow<List<DonHang>> = db.donHangDao().moiNhat()

    fun theoTrangThai(tt: String): Flow<List<DonHang>> = db.donHangDao().theoTrangThai(tt)

    fun doanhThu(tu: Long, den: Long): Flow<Long> = db.donHangDao().doanhThu(tu, den)

    fun tongCongNo(): Flow<Long> = db.donHangDao().tongCongNo()

    fun thanhToanTheoDon(donId: String): Flow<List<ThanhToan>> =
        db.thanhToanDao().theoDon(donId)

    suspend fun chiTietTheoDon(donId: String): List<DonHangChiTiet> =
        db.donHangChiTietDao().theoDon(donId)

    suspend fun vatTuTheoDon(donId: String): List<DonHangVatTu> =
        db.donHangVatTuDao().theoDon(donId)

    /** Tạo đơn mới + trừ kho trong 1 transaction. Trả về id đơn. */
    suspend fun taoDonHang(
        khachHangId: String,
        matHang: List<MatHangMoi>,
        giamGia: Long = 0L,
        vatTuDung: List<VatTuDungMoi> = emptyList(),
        ngayHen: Long? = null,
        ghiChu: String = "",
    ): String {
        require(matHang.isNotEmpty()) { "Đơn hàng phải có ít nhất 1 dịch vụ/gói" }
        val donId = UUID.randomUUID().toString()
        val maDh = sinhMaDon()
        val tongTienHang = matHang.sumOf { (it.soLuong * it.donGia).toLong() }
        val truocVat = TinhTien.tienTruocVat(tongTienHang, giamGia)
        // Đọc setting VAT: công tắc toàn hệ thống + áp cho đơn + theo từng dịch vụ
        val vatBatHeThong = try { caiDat.vatBatToanHeThong.first() } catch (_: Exception) { false }
        val vatApDungDon = try { caiDat.vatApDungDonHang.first() } catch (_: Exception) { true }
        val vatTheoDv = try { caiDat.vatTheoTungDichVu.first() } catch (_: Exception) { true }
        val vatMacDinh = try { caiDat.vatMacDinh.first() } catch (_: Exception) { 0.08 }
        val batVat = vatBatHeThong && vatApDungDon
        val tienVat = if (!batVat) 0L else if (vatTheoDv) {
            val chiTietThue = matHang.map { m ->
                val thanhTien = (m.soLuong * m.donGia).toLong()
                // Giảm giá phân bổ đều: VAT tính trên (thanhTien - giam*tyTrong)
                val tyTrong = if (tongTienHang > 0) thanhTien.toDouble() / tongTienHang else 0.0
                val giamChoDong = (maxOf(0L, giamGia) * tyTrong).toLong()
                (maxOf(0L, thanhTien - giamChoDong)) to m.chiuThue
            }
            TinhTien.tienVatTheoDong(chiTietThue, vatMacDinh, true)
        } else {
            TinhTien.tienVatDon(truocVat, vatMacDinh, true)
        }
        val tyLeVatAp = if (batVat) vatMacDinh else 0.0
        val tongThanhToan = TinhTien.tongThanhToanCoVat(truocVat, tienVat)
        db.withTransaction {
            db.donHangDao().them(
                DonHang(
                    id = donId, maDh = maDh, khachHangId = khachHangId,
                    ngayTao = System.currentTimeMillis(), ngayHen = ngayHen,
                    trangThai = "CHO_XU_LY", tongTienHang = tongTienHang,
                    giamGia = giamGia, tienVat = tienVat, tyLeVat = tyLeVatAp,
                    tongThanhToan = tongThanhToan,
                    daThu = 0L, conNo = tongThanhToan, ghiChu = ghiChu,
                )
            )
            matHang.forEach { m ->
                val thanhTien = (m.soLuong * m.donGia).toLong()
                db.donHangChiTietDao().them(
                    DonHangChiTiet(
                        id = UUID.randomUUID().toString(), donHangId = donId,
                        dichVuId = m.dichVuId, goiId = m.goiId,
                        tenHienThi = m.tenHienThi, soLuong = m.soLuong,
                        donGia = m.donGia, thanhTien = thanhTien,
                        chiuThue = m.chiuThue,
                    )
                )
            }
            vatTuDung.forEach { v ->
                val vt = db.vatTuDao().theoId(v.vatTuId)
                    ?: error("Không tìm thấy vật tư")
                require(vt.tonKho >= v.soLuong) { "Tồn kho ${vt.ten} không đủ (còn ${vt.tonKho})" }
                val thanhTienVon = (v.soLuong * vt.giaNhapTB).toLong()
                db.donHangVatTuDao().them(
                    DonHangVatTu(
                        id = UUID.randomUUID().toString(), donHangId = donId,
                        vatTuId = v.vatTuId, soLuong = v.soLuong,
                        donGiaVon = vt.giaNhapTB, thanhTienVon = thanhTienVon,
                    )
                )
                db.vatTuDao().sua(vt.copy(tonKho = vt.tonKho - v.soLuong))
            }
        }
        return donId
    }

    /** Thu tiền 1 đơn (cho phép thu nhiều lần). Tự recompute daThu/conNo. */    suspend fun thuTien(
        donHangId: String,
        soTien: Long,
        phuongThuc: String = "TIENMAT",
        nguoiThuId: String? = null,
        ghiChu: String = "",
    ) {
        require(soTien > 0) { "Số tiền thu phải > 0" }
        db.withTransaction {
            val don = db.donHangDao().theoId(donHangId) ?: error("Không tìm thấy đơn")
            db.thanhToanDao().them(
                ThanhToan(
                    id = UUID.randomUUID().toString(), donHangId = donHangId,
                    khachHangId = don.khachHangId, ngay = System.currentTimeMillis(),
                    soTien = soTien, phuongThuc = phuongThuc,
                    nguoiThuId = nguoiThuId, ghiChu = ghiChu,
                )
            )
            val daThu = db.thanhToanDao().daThu(donHangId)
            db.donHangDao().sua(don.copy(daThu = daThu, conNo = TinhTien.conNo(don.tongThanhToan, daThu)))
        }
    }

    /** Đổi trạng thái đơn (CHO_XU_LY / DANG_LAM / HOAN_THANH / HUY). */
    suspend fun doiTrangThai(donHangId: String, trangThai: String) {
        db.withTransaction {
            val don = db.donHangDao().theoId(donHangId) ?: error("Không tìm thấy đơn")
            db.donHangDao().sua(don.copy(trangThai = trangThai))
        }
    }

    /** Tạo đơn cho khách vãng lai: chỉ cần ghi tên (+SĐT), app tự tạo hồ sơ KH vãng lai. */
    suspend fun taoDonKhachVangLai(
        tenKhach: String,
        sdt: String = "",
        matHang: List<MatHangMoi>,
        giamGia: Long = 0L,
        vatTuDung: List<VatTuDungMoi> = emptyList(),
        ngayHen: Long? = null,
        ghiChu: String = "",
    ): String {
        require(tenKhach.isNotBlank()) { "Nhập tên khách vãng lai" }
        require(matHang.isNotEmpty()) { "Đơn hàng phải có ít nhất 1 dịch vụ/gói" }
        val sdtTrim = sdt.trim()
        // Nếu SĐT đã có trong danh bạ thì dùng lại KH đó (tránh lỗi UNIQUE sdt).
        if (sdtTrim.isNotEmpty()) {
            val tonTai = try { db.khachHangDao().theoSdt(sdtTrim) } catch (_: Exception) { null }
            if (tonTai != null) return taoDonHang(tonTai.id, matHang, giamGia, vatTuDung, ngayHen, ghiChu)
        }
        // Tạo KH vãng lai mới — chỉ thử tạo KH trong vòng lặp, tạo đơn ở ngoài
        // để lỗi tồn kho không đẻ thêm KH rác.
        var khId = ""
        var lan = 0
        var loiCuoi: Exception? = null
        while (lan < 3) {
            lan++
            val maKh = "KVL" + (System.currentTimeMillis() % 100000) + (if (lan > 1) "-$lan" else "")
            val sdtMoi = if (sdtTrim.isEmpty()) {
                "09" + (System.currentTimeMillis() % 100000000L).toString().padStart(8, '0') + (if (lan > 1) "$lan" else "")
            } else if (lan == 1) sdtTrim else sdtTrim.take(9) + lan.toString()
            khId = UUID.randomUUID().toString()
            try {
                db.khachHangDao().them(
                    vn.rinso.qldvyt.data.local.KhachHang(
                        id = khId, maKh = maKh,
                        hoTen = tenKhach.trim() + " (vãng lai)",
                        sdt = sdtMoi, diaChi = "",
                        ghiChu = "Khách vãng lai — tạo nhanh lúc bán",
                    )
                )
                loiCuoi = null
                break
            } catch (e: Exception) {
                loiCuoi = e // trùng mã/SĐT (UNIQUE) thì thử lại với hậu tố khác
            }
        }
        if (loiCuoi != null) error("Không tạo được khách vãng lai (trùng SĐT?) — hãy nhập SĐT khác")
        return taoDonHang(khId, matHang, giamGia, vatTuDung, ngayHen, ghiChu)
    }

    /** Sửa đơn: giảm giá / ghi chú / ngày hẹn — tự tính lại VAT + còn nợ. */
    suspend fun suaDon(donHangId: String, giamGiaMoi: Long, ghiChuMoi: String, ngayHenMoi: Long?) {
        require(giamGiaMoi >= 0) { "Giảm giá phải >= 0" }
        db.withTransaction {
            val don = db.donHangDao().theoId(donHangId) ?: error("Không tìm thấy đơn")
            val matHang = db.donHangChiTietDao().theoDon(donHangId)
            val tongTienHang = matHang.sumOf { it.thanhTien }
            val truocVat = TinhTien.tienTruocVat(tongTienHang, giamGiaMoi)
            val batVat = don.tyLeVat > 0
            val tienVat = if (!batVat) 0L else if (matHang.any { !it.chiuThue }) {
                // Tính theo dòng chịu thuế, giảm giá phân bổ đều
                val chiTietThue = matHang.map { ct ->
                    val tyTrong = if (tongTienHang > 0) ct.thanhTien.toDouble() / tongTienHang else 0.0
                    val giamChoDong = (maxOf(0L, giamGiaMoi) * tyTrong).toLong()
                    (maxOf(0L, ct.thanhTien - giamChoDong)) to ct.chiuThue
                }
                TinhTien.tienVatTheoDong(chiTietThue, don.tyLeVat, true)
            } else {
                TinhTien.tienVatDon(truocVat, don.tyLeVat, true)
            }
            val tongMoi = TinhTien.tongThanhToanCoVat(truocVat, tienVat)
            db.donHangDao().sua(
                don.copy(
                    giamGia = giamGiaMoi, tienVat = tienVat, tongThanhToan = tongMoi,
                    conNo = TinhTien.conNo(tongMoi, don.daThu),
                    ghiChu = ghiChuMoi.trim(), ngayHen = ngayHenMoi,
                )
            )
        }
    }

    /** Tính lại tiền đơn sau khi thêm/sửa/xóa dòng dịch vụ. */
    private suspend fun tinhLaiTienDon(donHangId: String) {
        val don = db.donHangDao().theoId(donHangId) ?: error("Không tìm thấy đơn")
        val matHang = db.donHangChiTietDao().theoDon(donHangId)
        val tongTienHang = matHang.sumOf { it.thanhTien }
        // Giữ nguyên giảm giá cũ (kẹp không vượt tổng hàng)
        val giam = don.giamGia.coerceIn(0L, tongTienHang)
        val truocVat = TinhTien.tienTruocVat(tongTienHang, giam)
        val batVat = don.tyLeVat > 0
        val tienVat = if (!batVat || matHang.isEmpty()) 0L else if (matHang.any { !it.chiuThue }) {
            val chiTietThue = matHang.map { ct ->
                val tyTrong = if (tongTienHang > 0) ct.thanhTien.toDouble() / tongTienHang else 0.0
                val giamChoDong = (giam * tyTrong).toLong()
                (maxOf(0L, ct.thanhTien - giamChoDong)) to ct.chiuThue
            }
            TinhTien.tienVatTheoDong(chiTietThue, don.tyLeVat, true)
        } else {
            TinhTien.tienVatDon(truocVat, don.tyLeVat, true)
        }
        val tongMoi = TinhTien.tongThanhToanCoVat(truocVat, tienVat)
        db.donHangDao().sua(
            don.copy(
                tongTienHang = tongTienHang, giamGia = giam, tienVat = tienVat,
                tongThanhToan = tongMoi, conNo = TinhTien.conNo(tongMoi, don.daThu),
            )
        )
    }

    /** Thêm 1 dịch vụ/gói vào đơn đã có (cho phép sửa đơn: thêm món). */
    suspend fun themDichVuVaoDon(donHangId: String, m: MatHangMoi) {
        require(m.soLuong > 0) { "Số lượng phải > 0" }
        db.withTransaction {
            db.donHangDao().theoId(donHangId) ?: error("Không tìm thấy đơn")
            val thanhTien = (m.soLuong * m.donGia).toLong()
            db.donHangChiTietDao().them(
                DonHangChiTiet(
                    id = UUID.randomUUID().toString(), donHangId = donHangId,
                    dichVuId = m.dichVuId, goiId = m.goiId,
                    tenHienThi = m.tenHienThi, soLuong = m.soLuong,
                    donGia = m.donGia, thanhTien = thanhTien, chiuThue = m.chiuThue,
                )
            )
            tinhLaiTienDon(donHangId)
        }
    }

    /** Sửa số lượng 1 dòng dịch vụ trong đơn. */
    suspend fun suaSoLuongChiTiet(chiTietId: String, donHangId: String, soLuongMoi: Double) {
        require(soLuongMoi > 0) { "Số lượng phải > 0" }
        db.withTransaction {
            val ds = db.donHangChiTietDao().theoDon(donHangId)
            val ct = ds.firstOrNull { it.id == chiTietId } ?: error("Không tìm thấy dòng hàng")
            val thanhMoi = (soLuongMoi * ct.donGia).toLong()
            db.donHangChiTietDao().sua(ct.copy(soLuong = soLuongMoi, thanhTien = thanhMoi))
            tinhLaiTienDon(donHangId)
        }
    }

    /** Xóa 1 dòng dịch vụ khỏi đơn (giữ ít nhất 1 dòng). */
    suspend fun xoaChiTiet(chiTietId: String, donHangId: String) {
        db.withTransaction {
            val ds = db.donHangChiTietDao().theoDon(donHangId)
            require(ds.size > 1) { "Đơn phải giữ ít nhất 1 dịch vụ" }
            db.donHangChiTietDao().xoa(chiTietId)
            tinhLaiTienDon(donHangId)
        }
    }

    /** Xóa 1 dòng thu tiền (điều chỉnh công nợ) — tự tính lại đã thu / còn nợ. */
    suspend fun xoaThanhToan(thanhToanId: String, donHangId: String) {
        db.withTransaction {
            db.thanhToanDao().xoa(thanhToanId)
            val don = db.donHangDao().theoId(donHangId) ?: error("Không tìm thấy đơn")
            val daThu = db.thanhToanDao().daThu(donHangId)
            db.donHangDao().sua(don.copy(daThu = daThu, conNo = TinhTien.conNo(don.tongThanhToan, daThu)))
        }
    }

    private fun sinhMaDon(): String {
        val ngay = SimpleDateFormat("yyyyMMdd", Locale("vi", "VN")).format(Date())
        val duoi = (100..999).random()
        return "DH$ngay-$duoi"
    }
}
