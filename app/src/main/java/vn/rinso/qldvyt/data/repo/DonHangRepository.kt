package vn.rinso.qldvyt.data.repo

import androidx.room.withTransaction
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
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
class DonHangRepository @Inject constructor(private val db: QldvytDatabase) {

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
        val tongThanhToan = TinhTien.tongThanhToan(tongTienHang, giamGia)
        db.withTransaction {
            db.donHangDao().them(
                DonHang(
                    id = donId, maDh = maDh, khachHangId = khachHangId,
                    ngayTao = System.currentTimeMillis(), ngayHen = ngayHen,
                    trangThai = "CHO_XU_LY", tongTienHang = tongTienHang,
                    giamGia = giamGia, tongThanhToan = tongThanhToan,
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

    /** Thu tiền 1 đơn (cho phép thu nhiều lần). Tự recompute daThu/conNo. */
    suspend fun thuTien(
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

    private fun sinhMaDon(): String {
        val ngay = SimpleDateFormat("yyyyMMdd", Locale("vi", "VN")).format(Date())
        val duoi = (100..999).random()
        return "DH$ngay-$duoi"
    }
}
