package vn.rinso.qldvyt.ui.nhiemVu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.local.NhanVienDao
import vn.rinso.qldvyt.data.prefs.CaiDat
import vn.rinso.qldvyt.data.repo.NhiemVuRepository
import kotlinx.coroutines.flow.first

/** ViewModel nhiệm vụ: giao việc + nhận + hoàn thành 1 chạm (tính 30%). */
@HiltViewModel
class NhiemVuViewModel @Inject constructor(
    private val repo: NhiemVuRepository,
    nhanVienDao: NhanVienDao,
    caiDat: CaiDat,
) : ViewModel() {
    private val nvDao = nhanVienDao
    val dsNv = nvDao.dangLam().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsViec = repo.tatCa().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tyLeMacDinh = caiDat.tyLeNhiemVu.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.30)
    val thongBao = MutableStateFlow("")

    suspend fun tenNv(id: String): String =
        nvDao.tatCa().first().firstOrNull { it.id == id }?.hoTen ?: "?"

    fun giaoViec(nvId: String, ten: String, loai: String, tongGia: Long, tyLe: Double) = viewModelScope.launch {
        runCatching {
            require(nvId.isNotBlank()) { "Hãy chọn nhân viên" }
            require(ten.isNotBlank()) { "Nhập tên công việc" }
            repo.giaoViec(nvId, ten, loai, tongGia, tyLe)
            thongBao.value = "Đã giao việc"
        }.onFailure { thongBao.value = it.message ?: "Giao việc thất bại" }
    }

    fun nhanViec(id: String) = viewModelScope.launch {
        runCatching { repo.nhanViec(id); thongBao.value = "NV đã nhận việc" }
            .onFailure { thongBao.value = it.message ?: "Thất bại" }
    }

    fun hoanThanh(id: String) = viewModelScope.launch {
        runCatching { repo.hoanThanh(id); thongBao.value = "Đã hoàn thành — tiền NV đã tính 30%" }
            .onFailure { thongBao.value = it.message ?: "Thất bại" }
    }

    fun huy(id: String) = viewModelScope.launch {
        runCatching { repo.huy(id); thongBao.value = "Đã hủy việc" }
            .onFailure { thongBao.value = it.message ?: "Thất bại" }
    }
}
