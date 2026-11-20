package vn.rinso.qldvyt.ui.kho

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.repo.KhoRepository

/** ViewModel kho: tồn kho + cảnh báo hết hàng + nhập kho + lịch sử. */
@HiltViewModel
class KhoViewModel @Inject constructor(private val repo: KhoRepository) : ViewModel() {
    val dsVatTu = repo.tatCa().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsSapHet = repo.sapHet().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsNhap = repo.lichSuNhap().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val thongBao = MutableStateFlow("")

    fun themVatTu(ten: String, donVi: String, tonToiThieu: Double) = viewModelScope.launch {
        runCatching {
            repo.themVatTu(ten, "", donVi, tonToiThieu)
            thongBao.value = "Đã thêm vật tư $ten"
        }.onFailure { thongBao.value = it.message ?: "Thêm thất bại" }
    }

    fun nhapKho(vatTuId: String, soLuong: Double, donGia: Long, nhaCungCap: String = "") = viewModelScope.launch {
        runCatching {
            repo.nhapKho(vatTuId, soLuong, donGia, nhaCungCap)
            thongBao.value = "Đã nhập kho"
        }.onFailure { thongBao.value = it.message ?: "Nhập kho thất bại" }
    }
}
