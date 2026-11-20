package vn.rinso.qldvyt.ui.caiDat

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.prefs.CaiDat

/** ViewModel màn Cài đặt: công thức lương, VAT/thuế toàn hệ thống + từng phần, công ty, giao diện. */
@HiltViewModel
class CaiDatViewModel @Inject constructor(private val caiDat: CaiDat) : ViewModel() {
    val congChuanThang = caiDat.congChuanThang.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 26.0)
    val tyLeNhiemVu = caiDat.tyLeNhiemVu.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.30)
    val soNgayCanhBaoNo = caiDat.soNgayCanhBaoNo.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 7)
    val tenCongTy = caiDat.tenCongTy.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val diaChiCongTy = caiDat.diaChiCongTy.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val sdtCongTy = caiDat.sdtCongTy.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val maSoThue = caiDat.maSoThue.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")
    val cheDoGiaoDien = caiDat.cheDoGiaoDien.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "HE_THONG")
    // VAT / thuế
    val vatBat = caiDat.vatBatToanHeThong.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val vatMacDinh = caiDat.vatMacDinh.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.08)
    val vatApDungDon = caiDat.vatApDungDonHang.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val vatTheoDv = caiDat.vatTheoTungDichVu.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val thueTncnBat = caiDat.thueTncnBat.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val thueTncn = caiDat.thueTncnMacDinh.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val hoaHong = caiDat.hoaHongMacDinh.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)
    val phuCap = caiDat.phuCapMacDinh.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val phatMuon = caiDat.phatDiMuon.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)
    val mucTieu = caiDat.mucTieuThang.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 50_000_000L)

    fun luuCongChuan(g: Double) = viewModelScope.launch { caiDat.luuCongChuan(g) }
    fun luuTyLe(tyLe: Double) = viewModelScope.launch { caiDat.luuTyLeNhiemVu(tyLe) }
    fun luuNgayCanhBao(n: Int) = viewModelScope.launch { caiDat.luuSoNgayCanhBaoNo(n) }
    fun luuTenCongTy(t: String) = viewModelScope.launch { caiDat.luuTenCongTy(t) }
    fun luuDiaChi(d: String) = viewModelScope.launch { caiDat.luuDiaChiCongTy(d) }
    fun luuSdt(s: String) = viewModelScope.launch { caiDat.luuSdtCongTy(s) }
    fun luuMst(m: String) = viewModelScope.launch { caiDat.luuMaSoThue(m) }
    fun luuCheDoGiaoDien(cheDo: String) = viewModelScope.launch { caiDat.luuCheDoGiaoDien(cheDo) }
    fun luuVatBat(b: Boolean) = viewModelScope.launch { caiDat.luuVatBat(b) }
    fun luuVatMacDinh(t: Double) = viewModelScope.launch { caiDat.luuVatMacDinh(t) }
    fun luuVatApDungDon(b: Boolean) = viewModelScope.launch { caiDat.luuVatApDungDon(b) }
    fun luuVatTheoDv(b: Boolean) = viewModelScope.launch { caiDat.luuVatTheoDichVu(b) }
    fun luuThueBat(b: Boolean) = viewModelScope.launch { caiDat.luuThueTncnBat(b) }
    fun luuThueTncn(t: Double) = viewModelScope.launch { caiDat.luuThueTncnMacDinh(t) }
    fun luuHoaHong(t: Double) = viewModelScope.launch { caiDat.luuHoaHongMacDinh(t) }
    fun luuPhuCap(t: Long) = viewModelScope.launch { caiDat.luuPhuCapMacDinh(t) }
    fun luuPhatMuon(t: Long) = viewModelScope.launch { caiDat.luuPhatDiMuon(t) }
    fun luuMucTieu(t: Long) = viewModelScope.launch { caiDat.luuMucTieuThang(t) }
}
