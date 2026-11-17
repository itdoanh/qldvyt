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

/** ViewModel khách hàng: tìm theo tên/SĐT + thêm mới. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class KhachHangViewModel @Inject constructor(private val dao: KhachHangDao) : ViewModel() {
    val tuKhoa = MutableStateFlow("")
    val danhSach = tuKhoa.flatMapLatest { tk ->
        if (tk.isBlank()) dao.tatCa() else dao.timKiem(tk.trim())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

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
}
