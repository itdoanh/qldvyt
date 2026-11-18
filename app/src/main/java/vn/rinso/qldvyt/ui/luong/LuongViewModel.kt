package vn.rinso.qldvyt.ui.luong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.repo.LuongRepository

/** ViewModel lương: chọn tháng → tính + chốt/mở khóa. */
@HiltViewModel
class LuongViewModel @Inject constructor(
    private val repo: LuongRepository,
    nhanVienDao: vn.rinso.qldvyt.data.local.NhanVienDao,
) : ViewModel() {
    val dsNv = nhanVienDao.tatCa()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun tenNv(id: String): String = dsNv.value.firstOrNull { it.id == id }?.let { "${it.hoTen} • ${it.maNv}" } ?: "NV: ${id.take(8)}…"
    private val cal = Calendar.getInstance()
    val thang = MutableStateFlow(cal.get(Calendar.MONTH) + 1)
    val nam = MutableStateFlow(cal.get(Calendar.YEAR))
    val thongBao = MutableStateFlow("")

    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val dsBangLuong = kotlinx.coroutines.flow.combine(thang, nam) { t, n -> t to n }
        .flatMapLatest { (t, n) -> repo.theoThang(t, n) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tongThucLinh = MutableStateFlow(0L)

    fun tinhCaThang() = viewModelScope.launch {
        runCatching {
            val ds = repo.tinhCaThang(thang.value, nam.value)
            tongThucLinh.value = ds.sumOf { it.thucLinh }
            thongBao.value = "Đã tính ${ds.size} NV tháng ${thang.value}/${nam.value}"
        }.onFailure { thongBao.value = it.message ?: "Tính lương thất bại" }
    }

    fun chot(nvId: String) = viewModelScope.launch {
        runCatching { repo.chot(nvId, thang.value, nam.value); thongBao.value = "Đã chốt" }
            .onFailure { thongBao.value = it.message ?: "Chốt thất bại" }
    }

    fun moKhoa(nvId: String) = viewModelScope.launch {
        runCatching { repo.moKhoa(nvId, thang.value, nam.value); thongBao.value = "Đã mở khóa (admin)" }
            .onFailure { thongBao.value = it.message ?: "Mở khóa thất bại" }
    }
}
