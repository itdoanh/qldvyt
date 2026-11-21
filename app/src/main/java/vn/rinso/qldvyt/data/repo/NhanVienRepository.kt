package vn.rinso.qldvyt.data.repo

import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import vn.rinso.qldvyt.data.local.CapBac
import vn.rinso.qldvyt.data.local.ChamCong
import vn.rinso.qldvyt.data.local.NhanVien
import vn.rinso.qldvyt.data.local.QldvytDatabase
import vn.rinso.qldvyt.data.local.TamUng
import vn.rinso.qldvyt.data.local.ThuongPhat

/** Repository nhân sự: cấp bậc, nhân viên, chấm công, thưởng/phạt, tạm ứng. */
@Singleton
class NhanVienRepository @Inject constructor(private val db: QldvytDatabase) {

    fun capBac(): Flow<List<CapBac>> = db.capBacDao().tatCa()
    fun nhanVien(): Flow<List<NhanVien>> = db.nhanVienDao().dangLam()
    fun tatCaNv(): Flow<List<NhanVien>> = db.nhanVienDao().tatCa()

    suspend fun themCapBac(ten: String, luongCoBan: Long, chuyenCan: Long): String {
        require(ten.isNotBlank()) { "Nhập tên cấp bậc" }
        val id = "cap-" + UUID.randomUUID().toString().take(8)
        val gio = System.currentTimeMillis()
        db.capBacDao().them(CapBac(id, ten.trim(), "", luongCoBan.coerceAtLeast(0), chuyenCan.coerceAtLeast(0), gio, gio))
        return id
    }

    suspend fun themNhanVien(hoTen: String, sdt: String, capBacId: String, luongOverride: Long? = null): String {
        require(hoTen.isNotBlank()) { "Nhập họ tên nhân viên" }
        require(capBacId.isNotBlank()) { "Hãy chọn cấp bậc" }
        val id = UUID.randomUUID().toString()
        val maNv = "NV" + (System.currentTimeMillis() % 100000).toString().padStart(5, '0')
        db.nhanVienDao().them(
            NhanVien(
                id = id, maNv = maNv, hoTen = hoTen.trim(), sdt = sdt.trim(),
                capBacId = capBacId, ngayVaoLam = System.currentTimeMillis(),
                luongCoBanOverride = luongOverride,
            )
        )
        return id
    }

    suspend fun chamCong(nhanVienId: String, ngayDauNgay: Long, soCong: Double, diMuon: Boolean) {
        require(soCong in 0.0..1.0) { "Số công phải 0 / 0.5 / 1" }
        db.chamCongDao().cham(
            ChamCong(
                id = UUID.randomUUID().toString(), nhanVienId = nhanVienId,
                ngay = ngayDauNgay, soCong = soCong, diMuon = diMuon,
            )
        )
    }

    suspend fun thuongPhat(nhanVienId: String, loai: String, soTien: Long, lyDo: String) {
        require(soTien != 0L) { "Số tiền phải khác 0" }
        db.thuongPhatDao().them(
            ThuongPhat(
                id = UUID.randomUUID().toString(), nhanVienId = nhanVienId,
                ngay = System.currentTimeMillis(), loai = loai, soTien = soTien, lyDo = lyDo,
            )
        )
    }

    suspend fun tamUng(nhanVienId: String, soTien: Long, lyDo: String) {
        require(soTien > 0) { "Tạm ứng phải > 0" }
        db.tamUngDao().them(
            TamUng(
                id = UUID.randomUUID().toString(), nhanVienId = nhanVienId,
                ngay = System.currentTimeMillis(), soTien = soTien, lyDo = lyDo,
            )
        )
    }

    fun tamUngTheoKy(nvId: String, tu: Long, den: Long): Flow<List<TamUng>> =
        db.tamUngDao().theoKy(nvId, tu, den)

    fun thuongPhatTheoKy(nvId: String, tu: Long, den: Long): Flow<List<ThuongPhat>> =
        db.thuongPhatDao().theoKy(nvId, tu, den)

    suspend fun xoaTamUng(id: String) = db.tamUngDao().xoa(id)

    suspend fun xoaThuongPhat(id: String) = db.thuongPhatDao().xoa(id)
}
