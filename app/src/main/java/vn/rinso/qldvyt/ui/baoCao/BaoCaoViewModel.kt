package vn.rinso.qldvyt.ui.baoCao

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.backup.SaoLuuManager
import vn.rinso.qldvyt.data.local.BangLuongDao
import vn.rinso.qldvyt.data.local.DonHangDao

/** ViewModel Báo cáo: doanh thu tháng + lương tháng + xuất CSV. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BaoCaoViewModel @Inject constructor(
    private val donDao: DonHangDao,
    private val bangLuongDao: BangLuongDao,
    private val saoLuu: SaoLuuManager,
) : ViewModel() {
    private val cal = Calendar.getInstance()
    val thang = MutableStateFlow(cal.get(Calendar.MONTH) + 1)
    val nam = MutableStateFlow(cal.get(Calendar.YEAR))
    val thongBao = MutableStateFlow("")

    private fun dauCuoiThang(t: Int, n: Int): Pair<Long, Long> {
        val tu = Calendar.getInstance().apply { set(n, t - 1, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        val den = Calendar.getInstance().apply {
            set(n, t - 1, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        }
        return tu.timeInMillis to den.timeInMillis
    }

    private val thangNam = combine(thang, nam) { t: Int, n: Int -> t to n }

    val doanhThuThang = thangNam.flatMapLatest { (t, n) ->
        val (tu, den) = dauCuoiThang(t, n)
        donDao.doanhThu(tu, den)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val dsLuong = thangNam.flatMapLatest { (t, n) ->
        bangLuongDao.theoThang(t, n)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dsDon = donDao.moiNhat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Xuất CSV bảng lương tháng ra file (mở bằng Excel). */
    fun xuatCsvLuong(uri: Uri) = viewModelScope.launch {
        runCatching {
            val ds = dsLuong.value
            val hang: List<Map<String, Any?>> = ds.map { bl ->
                mapOf(
                    "NV" to bl.nhanVienId.take(8), "Thang" to "${bl.thang}/${bl.nam}",
                    "Cong" to bl.tongCong, "TheoCong" to bl.luongCoBan,
                    "NhiemVu30" to bl.tienNhiemVu, "Thuong" to bl.thuong,
                    "ChuyenCan" to bl.chuyenCan, "TamUng" to bl.tamUng,
                    "Phat" to bl.khauTruKhac, "ThucLinh" to bl.thucLinh,
                    "TrangThai" to bl.trangThai,
                )
            }
            saoLuu.xuatCsv("bang_luong", uri, hang)
            thongBao.value = "Đã xuất CSV lương ${ds.size} dòng"
        }.onFailure { thongBao.value = it.message ?: "Xuất thất bại" }
    }

    /** Xuất CSV đơn hàng tháng đang xem ra file. */
    fun xuatCsvDon(uri: Uri) = viewModelScope.launch {
        runCatching {
            val (tu, den) = dauCuoiThang(thang.value, nam.value)
            val ds = dsDon.value.filter { it.ngayTao in tu..den }
            val hang: List<Map<String, Any?>> = ds.map { don ->
                mapOf(
                    "MaDon" to don.maDh, "Ngay" to don.ngayTao,
                    "TrangThai" to don.trangThai, "TongHang" to don.tongTienHang,
                    "GiamGia" to don.giamGia, "ThanhToan" to don.tongThanhToan,
                    "DaThu" to don.daThu, "ConNo" to don.conNo,
                )
            }
            saoLuu.xuatCsv("don_hang", uri, hang)
            thongBao.value = "Đã xuất CSV ${ds.size} đơn"
        }.onFailure { thongBao.value = it.message ?: "Xuất thất bại" }
    }
}
