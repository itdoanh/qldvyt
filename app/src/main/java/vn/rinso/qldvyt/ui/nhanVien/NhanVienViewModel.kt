package vn.rinso.qldvyt.ui.nhanVien

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.repo.NhanVienRepository

/** ViewModel nhân viên: cấp bậc, danh sách NV, chấm công nhanh. */
@HiltViewModel
class NhanVienViewModel @Inject constructor(private val repo: NhanVienRepository) : ViewModel() {
    val dsCapBac = repo.capBac().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsNv = repo.nhanVien().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val thongBao = MutableStateFlow("")

    fun themCapBac(ten: String, luong: Long, chuyenCan: Long) = viewModelScope.launch {
        runCatching {
            repo.themCapBac(ten, luong, chuyenCan)
            thongBao.value = "Đã thêm cấp bậc $ten"
        }.onFailure { thongBao.value = it.message ?: "Thêm thất bại" }
    }

    fun themNhanVien(hoTen: String, sdt: String, capBacId: String, diaChi: String = "") = viewModelScope.launch {
        runCatching {
            repo.themNhanVien(hoTen, sdt, capBacId, diaChi)
            thongBao.value = "Đã thêm nhân viên $hoTen"
        }.onFailure { thongBao.value = it.message ?: "Thêm thất bại" }
    }

    /** Sửa thông tin từng nhân viên (tên / SĐT / địa chỉ / cấp bậc / STK / ghi chú / trạng thái). */
    fun suaNhanVien(
        nvId: String, hoTen: String, sdt: String, diaChi: String,
        capBacId: String, stk: String, ghiChu: String, trangThai: String,
    ) = viewModelScope.launch {
        runCatching {
            repo.suaNhanVien(nvId, hoTen, sdt, diaChi, capBacId, trangThai, ghiChu, stk)
            thongBao.value = "Đã cập nhật thông tin nhân viên"
        }.onFailure { thongBao.value = it.message ?: "Sửa thất bại" }
    }

    fun xoaNhanVien(nvId: String) = viewModelScope.launch {
        runCatching {
            repo.xoaNhanVien(nvId)
            thongBao.value = "Đã chuyển nhân viên sang nghỉ việc (giữ lịch sử lương)"
        }.onFailure { thongBao.value = it.message ?: "Xóa thất bại" }
    }

    fun chamNhanh(nvId: String, soCong: Double, diMuon: Boolean) = viewModelScope.launch {
        runCatching {
            // Đầu ngày hôm nay (millis)
            val cal = java.util.Calendar.getInstance().apply {
                set(java.util.Calendar.HOUR_OF_DAY, 0); set(java.util.Calendar.MINUTE, 0)
                set(java.util.Calendar.SECOND, 0); set(java.util.Calendar.MILLISECOND, 0)
            }
            repo.chamCong(nvId, cal.timeInMillis, soCong, diMuon)
            thongBao.value = "Đã chấm công hôm nay"
        }.onFailure { thongBao.value = it.message ?: "Chấm công thất bại" }
    }

    fun thuongPhat(nvId: String, loai: String, soTien: Long, lyDo: String) = viewModelScope.launch {
        runCatching {
            repo.thuongPhat(nvId, loai, soTien, lyDo)
            thongBao.value = "Đã ghi thưởng/phạt"
        }.onFailure { thongBao.value = it.message ?: "Ghi thất bại" }
    }

    fun tamUng(nvId: String, soTien: Long, lyDo: String) = viewModelScope.launch {
        runCatching {
            repo.tamUng(nvId, soTien, lyDo)
            thongBao.value = "Đã ghi tạm ứng"
        }.onFailure { thongBao.value = it.message ?: "Ghi thất bại" }
    }
}
