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
import vn.rinso.qldvyt.data.local.VatTuDao

/** ViewModel Tổng quan: doanh thu hôm nay, đơn chờ, công nợ, vật tư sắp hết (số liệu thật). */
@HiltViewModel
class TongQuanViewModel @Inject constructor(
    donDao: DonHangDao,
    vatTuDao: VatTuDao,
) : ViewModel() {
    private fun dauCuoiHomNay(): Pair<Long, Long> {
        val dau = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }
        val cuoi = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 23); set(Calendar.MINUTE, 59); set(Calendar.SECOND, 59) }
        return dau.timeInMillis to cuoi.timeInMillis
    }

    private val tuDen = dauCuoiHomNay()
    val doanhThuHomNay = donDao.doanhThu(tuDen.first, tuDen.second)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val tongNo = donDao.tongCongNo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val dsSapHet = vatTuDao.sapHet()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val donCho = combine(donDao.theoTrangThai("CHO_XU_LY"), donDao.theoTrangThai("DANG_LAM")) { cho, dang ->
        cho.size + dang.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
