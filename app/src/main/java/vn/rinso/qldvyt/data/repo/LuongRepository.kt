package vn.rinso.qldvyt.data.repo

import java.util.Calendar
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import vn.rinso.qldvyt.data.local.BangLuong
import vn.rinso.qldvyt.data.local.QldvytDatabase
import vn.rinso.qldvyt.data.prefs.CaiDat
import vn.rinso.qldvyt.domain.pricing.TinhTien

/**
 * Repository lương: tính bảng lương tháng theo docs/03-cong-thuc-tinh-tien.md.
 * thucLinh = luongTheoCong + tienNhiemVu + hoaHong + thuong + chuyenCan - tamUng - khauTru.
 */
@Singleton
class LuongRepository @Inject constructor(
    private val db: QldvytDatabase,
    private val caiDat: CaiDat,
) {
    fun theoThang(thang: Int, nam: Int): Flow<List<BangLuong>> =
        db.bangLuongDao().theoThang(thang, nam)

    private fun dauCuoiThang(thang: Int, nam: Int): Pair<Long, Long> {
        val tu = Calendar.getInstance().apply { set(nam, thang - 1, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        val den = Calendar.getInstance().apply { set(nam, thang - 1, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59) }
        return tu.timeInMillis to den.timeInMillis
    }

    /** Tính (chưa chốt) bảng lương 1 NV trong tháng. */
    suspend fun tinhChoNv(nhanVienId: String, thang: Int, nam: Int): BangLuong {
        val nv = db.nhanVienDao().theoId(nhanVienId) ?: error("Không tìm thấy NV")
        val (tu, den) = dauCuoiThang(thang, nam)
        val congChuan = caiDat.congChuanThang.first()
        val tongCong = db.chamCongDao().tongCong(nhanVienId, tu, den)
        val soNgayMuon = db.chamCongDao().soNgayDiMuon(nhanVienId, tu, den)
        val capBac = db.capBacDao().theoId(nv.capBacId)
        val luongChuan = nv.luongCoBanOverride ?: capBac?.luongCoBan ?: 0L
        val luongTheoCong = TinhTien.luongTheoCong(luongChuan, tongCong, congChuan)
        val tienNv = db.nhiemVuDao().tienNhiemVuKy(nhanVienId, tu, den)
        val thuong = db.thuongPhatDao().tongThuong(nhanVienId, tu, den)
        val phat = db.thuongPhatDao().tongPhat(nhanVienId, tu, den)
        val tamUng = db.tamUngDao().tongTamUng(nhanVienId, tu, den)
        val duChuyenCan = TinhTien.duDieuKienChuyenCan(tongCong, congChuan, soNgayMuon)
        val chuyenCan = if (duChuyenCan) (capBac?.chuyenCanMuc ?: 0L) else 0L
        // Hoa hồng: tạm tính 0 ở P4 (mở rộng P5: % trên đơn giới thiệu). Giữ cột để tương thích docs/02.
        val hoaHong = 0L
        val thucLinh = TinhTien.thucLinh(luongTheoCong, tienNv, hoaHong, thuong, chuyenCan, tamUng, phat)
        val cu = db.bangLuongDao().theoNvKy(nhanVienId, thang, nam)
        return (cu ?: BangLuong(UUID.randomUUID().toString(), nhanVienId, thang, nam)).copy(
            luongCoBan = luongChuan, tongCong = tongCong, tienNhiemVu = tienNv,
            hoaHong = hoaHong, thuong = thuong, chuyenCan = chuyenCan,
            tamUng = tamUng, khauTruKhac = phat, thucLinh = thucLinh,
        )
    }

    /** Tính + lưu nháp cả công ty trong tháng. */
    suspend fun tinhCaThang(thang: Int, nam: Int): List<BangLuong> {
        val ds = db.nhanVienDao().tatCa().first()
        return ds.map { nv ->
            val bl = tinhChoNv(nv.id, thang, nam)
            if (bl.trangThai != "CHOT") db.bangLuongDao().luu(bl)
            bl
        }
    }

    /** Chốt bảng lương (khóa): chặn sửa công/thưởng của kỳ đã chốt ở UI. */
    suspend fun chot(nhanVienId: String, thang: Int, nam: Int) {
        val bl = tinhChoNv(nhanVienId, thang, nam)
        db.bangLuongDao().luu(bl.copy(trangThai = "CHOT", ngayChot = System.currentTimeMillis()))
    }

    /** Mở khóa (chỉ admin): cho sửa lại. */
    suspend fun moKhoa(nhanVienId: String, thang: Int, nam: Int) {
        val cu = db.bangLuongDao().theoNvKy(nhanVienId, thang, nam) ?: error("Chưa có bảng lương")
        db.bangLuongDao().luu(cu.copy(trangThai = "NHAP", ngayChot = null))
    }
}
