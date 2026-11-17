package vn.rinso.qldvyt.ui.congNo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import vn.rinso.qldvyt.data.local.DonHangDao
import vn.rinso.qldvyt.data.prefs.CaiDat

/** ViewModel công nợ: đơn còn nợ + cảnh báo quá hạn theo cài đặt. */
@HiltViewModel
class CongNoViewModel @Inject constructor(
    donDao: DonHangDao,
    caiDat: CaiDat,
) : ViewModel() {
    val tongNo = donDao.tongCongNo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val dsNo = combine(donDao.moiNhat(), caiDat.soNgayCanhBaoNo) { ds, _ ->
        ds.filter { it.conNo > 0 && it.trangThai != "HUY" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}
