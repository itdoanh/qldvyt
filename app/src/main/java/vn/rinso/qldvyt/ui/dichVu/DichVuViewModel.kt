package vn.rinso.qldvyt.ui.dichVu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.local.DichVu
import vn.rinso.qldvyt.data.local.DichVuDao
import vn.rinso.qldvyt.data.local.GoiDichVu
import vn.rinso.qldvyt.data.local.GoiDichVuDao
import java.util.UUID

/** ViewModel dịch vụ & gói: catalog giá + thêm mới. */
@HiltViewModel
class DichVuViewModel @Inject constructor(
    dichVuDao: DichVuDao,
    goiDao: GoiDichVuDao,
) : ViewModel() {
    private val dvDao = dichVuDao
    private val gDao = goiDao

    val dsDichVu = dvDao.tatCa().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsGoi = gDao.dangBan().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val thongBao = MutableStateFlow("")

    fun themDichVu(ten: String, gia: Long, donVi: String, nhom: String = "KHAC") = viewModelScope.launch {
        runCatching {
            require(ten.isNotBlank()) { "Nhập tên dịch vụ" }
            require(gia >= 0) { "Giá phải >= 0" }
            val ma = "DV" + (System.currentTimeMillis() % 100000)
            dvDao.them(DichVu(UUID.randomUUID().toString(), ma, ten.trim(), "", gia, donVi.ifBlank { "lần" }, "DANGBAN", nhom))
            thongBao.value = "Đã thêm dịch vụ $ten"
        }.onFailure { thongBao.value = it.message ?: "Thêm thất bại" }
    }

    fun themGoi(ten: String, gia: Long) = viewModelScope.launch {
        runCatching {
            require(ten.isNotBlank()) { "Nhập tên gói" }
            val ma = "GOI" + (System.currentTimeMillis() % 100000)
            gDao.them(GoiDichVu(UUID.randomUUID().toString(), ma, ten.trim(), "", gia))
            thongBao.value = "Đã thêm gói $ten"
        }.onFailure { thongBao.value = it.message ?: "Thêm thất bại" }
    }
}
