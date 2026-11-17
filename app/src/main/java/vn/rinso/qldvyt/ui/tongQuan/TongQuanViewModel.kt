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
import vn.rinso.qldvyt.data.local.NhanVienDao
import vn.rinso.qldvyt.data.local.VatTuDao

/** ViewModel Tổng quan: số liệu thật cho dashboard MISA-style. */
@HiltViewModel
class TongQuanViewModel @Inject constructor(
    donDao: DonHangDao,
    vatTuDao: VatTuDao,
    nhanVienDao: NhanVienDao,
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
    val dsNv = nhanVienDao.dangLam()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val donCho = combine(donDao.theoTrangThai("CHO_XU_LY"), donDao.theoTrangThai("DANG_LAM")) { cho, dang ->
        cho.size + dang.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val donXongThang = donDao.theoTrangThai("HOAN_THANH")
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
