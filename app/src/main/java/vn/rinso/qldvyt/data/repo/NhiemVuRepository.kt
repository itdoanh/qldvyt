package vn.rinso.qldvyt.data.repo

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import vn.rinso.qldvyt.data.local.NhiemVu
import vn.rinso.qldvyt.data.local.QldvytDatabase
import vn.rinso.qldvyt.domain.pricing.TinhTien

/**
 * Repository nhiệm vụ (công) — NV hưởng 30% tổng giá khi hoàn thành.
 * Công thức duy nhất từ TinhTien (docs/03).
 */
@Singleton
class NhiemVuRepository @Inject constructor(private val db: QldvytDatabase) {

    fun tatCa(): Flow<List<NhiemVu>> = db.nhiemVuDao().tatCa()
    fun theoTrangThai(tt: String): Flow<List<NhiemVu>> = db.nhiemVuDao().theoTrangThai(tt)

    private suspend fun tim(id: String): NhiemVu =
        db.nhiemVuDao().tatCa().first().firstOrNull { it.id == id }
            ?: error("Không tìm thấy nhiệm vụ")

    /** Giao việc mới cho NV. */
    suspend fun giaoViec(
        nhanVienId: String,
        tenCongViec: String,
        loai: String,
        tongGia: Long,
        tyLeHuong: Double = 0.30,
        donHangId: String? = null,
    ): String {
        require(tongGia >= 0) { "Tổng giá phải >= 0" }
        val id = UUID.randomUUID().toString()
        val maViec = "CV" + (System.currentTimeMillis() % 1000000)
        db.nhiemVuDao().them(
            NhiemVu(
                id = id, maViec = maViec, donHangId = donHangId,
                tenCongViec = tenCongViec.trim(), loai = loai,
                nhanVienId = nhanVienId, ngayGiao = System.currentTimeMillis(),
                tongGia = tongGia, tyLeHuong = tyLeHuong.coerceIn(0.0, 1.0),
                tienNvHuong = 0L, trangThai = "DAGIAO",
            )
        )
        return id
    }

    /** NV nhận việc (DAGIAO -> DANGLAM). */
    suspend fun nhanViec(id: String) {
        val nv = tim(id)
        require(nv.trangThai == "DAGIAO") { "Chỉ nhận việc đang ở trạng thái Đã giao" }
        db.nhiemVuDao().sua(nv.copy(trangThai = "DANGLAM"))
    }

    /** Hoàn thành 1 chạm: tính tiền NV = round(tongGia * tyLe). */
    suspend fun hoanThanh(id: String) {
        val nv = tim(id)
        require(nv.trangThai != "HUY") { "Việc đã hủy không hoàn thành được" }
        require(nv.trangThai != "HOANTHANH") { "Việc đã hoàn thành rồi" }
        val tien = TinhTien.tienNvHuong(nv.tongGia, nv.tyLeHuong, true)
        db.nhiemVuDao().sua(
            nv.copy(
                trangThai = "HOANTHANH",
                ngayHoanThanh = System.currentTimeMillis(),
                tienNvHuong = tien,
            )
        )
    }

    /** Hủy việc: tiền NV = 0. */
    suspend fun huy(id: String) {
        val nv = tim(id)
        db.nhiemVuDao().sua(nv.copy(trangThai = "HUY", tienNvHuong = 0L))
    }

    /** Sửa nhiệm vụ: tên / NV phụ trách / tổng giá / tỷ lệ hưởng (tính lại tiền NV nếu đã xong). */
    suspend fun suaViec(id: String, tenMoi: String, nhanVienIdMoi: String, tongGiaMoi: Long, tyLeMoi: Double) {
        require(tenMoi.isNotBlank()) { "Nhập tên công việc" }
        require(tongGiaMoi >= 0) { "Tổng giá phải >= 0" }
        val cu = tim(id)
        val tyLe = tyLeMoi.coerceIn(0.0, 1.0)
        val tienMoi = if (cu.trangThai == "HOANTHANH") TinhTien.tienNvHuong(tongGiaMoi, tyLe, true) else 0L
        db.nhiemVuDao().sua(
            cu.copy(
                tenCongViec = tenMoi.trim(), nhanVienId = nhanVienIdMoi.ifBlank { cu.nhanVienId },
                tongGia = tongGiaMoi, tyLeHuong = tyLe, tienNvHuong = tienMoi,
            )
        )
    }

    /** Xóa nhiệm vụ (chỉ khi chưa hoàn thành để giữ tiền lương đúng). */
    suspend fun xoaViec(id: String) {
        val cu = tim(id)
        if (cu.trangThai == "HOANTHANH") error("Việc đã hoàn thành — hãy Hủy thay vì xóa để giữ lịch sử lương")
        db.nhiemVuDao().xoa(id)
    }
}
