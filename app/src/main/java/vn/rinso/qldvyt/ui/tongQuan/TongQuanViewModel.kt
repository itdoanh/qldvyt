package vn.rinso.qldvyt.ui.tongQuan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import vn.rinso.qldvyt.data.local.DonHangDao
import vn.rinso.qldvyt.data.local.KhachHangDao
import vn.rinso.qldvyt.data.local.NhanVienDao
import vn.rinso.qldvyt.data.local.NhiemVuDao
import vn.rinso.qldvyt.data.local.VatTuDao

/** ViewModel Tổng quan v3: dashboard điều hành + sparkline + hàng đợi việc. */
@HiltViewModel
class TongQuanViewModel @Inject constructor(
    donDao: DonHangDao,
    vatTuDao: VatTuDao,
    nhanVienDao: NhanVienDao,
    nhiemVuDao: NhiemVuDao,
    khachHangDao: KhachHangDao,
) : ViewModel() {
    private fun dauCuoiHomNay(): Pair<Long, Long> {
        val dau = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val cuoi = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }
        return dau.timeInMillis to cuoi.timeInMillis
    }

    private fun dauCuoiThang(): Pair<Long, Long> {
        val c = Calendar.getInstance()
        val tu = Calendar.getInstance().apply { set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        val den = Calendar.getInstance().apply {
            set(c.get(Calendar.YEAR), c.get(Calendar.MONTH), getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        }
        return tu.timeInMillis to den.timeInMillis
    }

    private val tuDenHomNay = dauCuoiHomNay()
    private val tuDenThang = dauCuoiThang()

    val doanhThuHomNay = donDao.doanhThu(tuDenHomNay.first, tuDenHomNay.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val doanhThuThang = donDao.doanhThu(tuDenThang.first, tuDenThang.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val tongNo = donDao.tongCongNo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val dsSapHet = vatTuDao.sapHet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsVatTu = vatTuDao.tatCa()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsNv = nhanVienDao.dangLam()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsKhach = khachHangDao.tatCa()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsViec = nhiemVuDao.tatCa()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsDonMoi = donDao.moiNhat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val donCho = combine(donDao.theoTrangThai("CHO_XU_LY"), donDao.theoTrangThai("DANG_LAM")) { cho, dang ->
        cho.size + dang.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val donXongThang = donDao.theoTrangThai("HOAN_THANH")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Doanh thu 7 ngày gần nhất (để vẽ sparkline, không cần query thêm). */
    val spark7Ngay = combine(dsDonMoi) { arr ->
        val ds = arr[0].filter { it.trangThai == "HOAN_THANH" }
        val out = MutableList(7) { 0L }
        val now = Calendar.getInstance()
        for (i in 0..6) {
            val c = (now.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, i - 6) }
            val d0 = c.apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
            val d1 = d0 + 86_400_000L
            out[i] = ds.filter { it.ngayTao in d0 until d1 }.sumOf { it.tongThanhToan }
        }
        out.toList()
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Trị giá kho = tồn × vốn TB. */
    val triGiaKho = combine(dsVatTu) { arr ->
        arr[0].sumOf { (it.tonKho * it.giaNhapTB).toLong() }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
}
