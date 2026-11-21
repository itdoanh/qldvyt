package vn.rinso.qldvyt.ui.khachHang

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.local.KhachHang
import vn.rinso.qldvyt.data.local.KhachHangDao
import java.util.UUID

/** ViewModel khách hàng v3: tìm theo tên/SĐT + thêm mới + kèm đơn/nợ từng KH. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class KhachHangViewModel @Inject constructor(
    private val dao: KhachHangDao,
    donDao: vn.rinso.qldvyt.data.local.DonHangDao,
) : ViewModel() {
    val tuKhoa = MutableStateFlow("")
    val danhSach = tuKhoa.flatMapLatest { tk ->
        if (tk.isBlank()) dao.tatCa() else dao.timKiem(tk.trim())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dsDon = donDao.moiNhat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    var thongBao = MutableStateFlow("")
        private set

    fun them(hoTen: String, sdt: String, diaChi: String) = viewModelScope.launch {
        runCatching {
            require(hoTen.isNotBlank()) { "Nhập họ tên khách hàng" }
            val maKh = "KH" + (System.currentTimeMillis() % 100000)
            dao.them(KhachHang(id = UUID.randomUUID().toString(), maKh = maKh, hoTen = hoTen.trim(), sdt = sdt.trim(), diaChi = diaChi.trim()))
            thongBao.value = "Đã thêm khách hàng $hoTen"
        }.onFailure { thongBao.value = it.message ?: "Thêm thất bại" }
    }

    fun sua(kh: KhachHang, hoTen: String, sdt: String, diaChi: String, ghiChu: String) = viewModelScope.launch {
        runCatching {
            require(hoTen.isNotBlank()) { "Nhập họ tên khách hàng" }
            dao.sua(kh.copy(hoTen = hoTen.trim(), sdt = sdt.trim(), diaChi = diaChi.trim(), ghiChu = ghiChu.trim()))
            thongBao.value = "Đã cập nhật khách hàng $hoTen"
        }.onFailure { thongBao.value = it.message ?: "Sửa thất bại" }
    }

    fun xoa(kh: KhachHang) = viewModelScope.launch {
        runCatching {
            dao.xoa(kh.id)
            thongBao.value = "Đã xóa khách hàng ${kh.hoTen}"
        }.onFailure { thongBao.value = "Không xóa được (KH đã có đơn) — hãy giữ lại lịch sử" }
    }
}
