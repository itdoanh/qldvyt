package vn.rinso.qldvyt.ui.donHang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.local.DichVuDao
import vn.rinso.qldvyt.data.local.GoiDichVuDao
import vn.rinso.qldvyt.data.local.KhachHangDao
import vn.rinso.qldvyt.data.local.VatTuDao
import vn.rinso.qldvyt.data.repo.DonHangRepository
import vn.rinso.qldvyt.data.repo.MatHangMoi
import vn.rinso.qldvyt.data.repo.VatTuDungMoi

/** ViewModel đơn hàng: danh sách + wizard tạo đơn 3 bước + thu tiền + xem trước VAT. */
@HiltViewModel
class DonHangViewModel @Inject constructor(
    private val repo: DonHangRepository,
    khDao: KhachHangDao,
    dichVuDao: DichVuDao,
    goiDao: GoiDichVuDao,
    vatTuDao: VatTuDao,
    caiDat: vn.rinso.qldvyt.data.prefs.CaiDat,
) : ViewModel() {
    val dsDon = repo.moiNhat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsKhach = khDao.tatCa().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsDichVu = dichVuDao.dangBan().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsGoi = goiDao.dangBan().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsVatTu = vatTuDao.tatCa().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    // Cài đặt VAT để xem trước ở bước 3 (đơn mới sẽ snapshot theo setting này)
    val vatBat = caiDat.vatBatToanHeThong.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)
    val vatApDungDon = caiDat.vatApDungDonHang.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val vatTheoDv = caiDat.vatTheoTungDichVu.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), true)
    val vatMacDinh = caiDat.vatMacDinh.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.08)
    val thongBao = MutableStateFlow("")

    // State wizard tạo đơn
    val khachDaChon = MutableStateFlow<String?>(null)
    val matHangDaChon = MutableStateFlow<List<MatHangMoi>>(emptyList())
    val vatTuDaChon = MutableStateFlow<List<VatTuDungMoi>>(emptyList())
    val giamGia = MutableStateFlow(0L)

    fun chonKhach(id: String) { khachDaChon.value = id }
    fun themMatHang(m: MatHangMoi) { matHangDaChon.value = matHangDaChon.value + m }
    fun xoaMatHang(index: Int) { matHangDaChon.value = matHangDaChon.value.filterIndexed { i, _ -> i != index } }
    fun themVatTu(v: VatTuDungMoi) { vatTuDaChon.value = vatTuDaChon.value + v }

    fun taoDon(onXong: (String) -> Unit) = viewModelScope.launch {
        runCatching {
            val khId = khachDaChon.value ?: error("Hãy chọn khách hàng (bước 1)")
            val mh = matHangDaChon.value
            require(mh.isNotEmpty()) { "Hãy chọn ít nhất 1 dịch vụ (bước 2)" }
            val id = repo.taoDonHang(khId, mh, giamGia.value, vatTuDaChon.value)
            matHangDaChon.value = emptyList(); vatTuDaChon.value = emptyList(); giamGia.value = 0L
            thongBao.value = "Đã tạo đơn thành công"
            onXong(id)
        }.onFailure { thongBao.value = it.message ?: "Tạo đơn thất bại" }
    }

    fun thuTien(donId: String, soTien: Long, phuongThuc: String) = viewModelScope.launch {
        runCatching {
            repo.thuTien(donId, soTien, phuongThuc)
            thongBao.value = "Đã thu tiền"
        }.onFailure { thongBao.value = it.message ?: "Thu tiền thất bại" }
    }

    fun doiTrangThai(donId: String, tt: String) = viewModelScope.launch {
        runCatching {
            repo.doiTrangThai(donId, tt)
            thongBao.value = "Đã cập nhật trạng thái"
        }.onFailure { thongBao.value = it.message ?: "Cập nhật thất bại" }
    }
}
