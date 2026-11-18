package vn.rinso.qldvyt.ui.caiDat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.prefs.CaiDat

/** ViewModel màn Cài đặt: công chuẩn, tỷ lệ NV 30%, cảnh báo nợ, tên công ty, giao diện. */
@HiltViewModel
class CaiDatViewModel @Inject constructor(private val caiDat: CaiDat) : ViewModel() {
    val congChuanThang = caiDat.congChuanThang.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 26.0)
    val tyLeNhiemVu = caiDat.tyLeNhiemVu.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.30)
    val soNgayCanhBaoNo = caiDat.soNgayCanhBaoNo.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    val tenCongTy = caiDat.tenCongTy.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val cheDoGiaoDien = caiDat.cheDoGiaoDien.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "HE_THONG")

    fun luuCongChuan(g: Double) = viewModelScope.launch { caiDat.luuCongChuan(g) }
    fun luuTyLe(tyLe: Double) = viewModelScope.launch { caiDat.luuTyLeNhiemVu(tyLe) }
    fun luuNgayCanhBao(n: Int) = viewModelScope.launch { caiDat.luuSoNgayCanhBaoNo(n) }
    fun luuTenCongTy(t: String) = viewModelScope.launch { caiDat.luuTenCongTy(t) }
    fun luuCheDoGiaoDien(cheDo: String) = viewModelScope.launch { caiDat.luuCheDoGiaoDien(cheDo) }
}
