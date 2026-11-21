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
    private val donHangDao: vn.rinso.qldvyt.data.local.DonHangDao,
    private val chiTietDao: vn.rinso.qldvyt.data.local.DonHangChiTietDao,
    private val khachHangDao: vn.rinso.qldvyt.data.local.KhachHangDao,
    caiDat: CaiDat,
) : ViewModel() {
    private val nvDao = nhanVienDao
    val dsNv = nvDao.dangLam().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsViec = repo.tatCa().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val tyLeMacDinh = caiDat.tyLeNhiemVu.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.30)
    val thongBao = MutableStateFlow("")
    /** Đơn đang cần làm (Chờ xử lý + Đang làm) để giao việc theo đơn thật. */
    val dsDon = donHangDao.moiNhat().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    val dsKhach = khachHangDao.tatCa().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
    private val cacheChiTiet = MutableStateFlow<Map<String, List<vn.rinso.qldvyt.data.local.DonHangChiTiet>>>(emptyMap())
    val chiTietTheoDon = cacheChiTiet

    suspend fun tenNv(id: String): String =
        nvDao.tatCa().first().firstOrNull { it.id == id }?.hoTen ?: "?"

    fun giaoViec(nvId: String, ten: String, loai: String, tongGia: Long, tyLe: Double, donHangId: String? = null) = viewModelScope.launch {
        runCatching {
            require(nvId.isNotBlank()) { "Hãy chọn nhân viên" }
            require(ten.isNotBlank()) { "Nhập tên công việc" }
            repo.giaoViec(nvId, ten, loai, tongGia, tyLe, donHangId)
            thongBao.value = if (donHangId.isNullOrBlank()) "Đã giao việc" else "Đã giao việc theo đơn"
        }.onFailure { thongBao.value = it.message ?: "Giao việc thất bại" }
    }

    /** Tải chi tiết 1 đơn để chọn dòng dịch vụ giao việc. */
    fun taiChiTiet(donId: String) = viewModelScope.launch {
        runCatching {
            if (cacheChiTiet.value.containsKey(donId)) return@launch
            val ct = chiTietDao.theoDon(donId)
            cacheChiTiet.value = cacheChiTiet.value + (donId to ct)
        }.onFailure { thongBao.value = it.message ?: "Tải chi tiết đơn thất bại" }
    }

    /** Giao nhanh 1 dòng dịch vụ trong đơn cho NV (tên + giá lấy từ dòng đơn). */
    fun giaoViecTheoDongDon(nvId: String, donId: String, chiTietId: String, tyLe: Double) = viewModelScope.launch {
        runCatching {
            require(nvId.isNotBlank()) { "Hãy chọn nhân viên" }
            val ct = (cacheChiTiet.value[donId] ?: chiTietDao.theoDon(donId))
                .firstOrNull { it.id == chiTietId } ?: error("Không tìm thấy dòng dịch vụ")
            val loai = when {
                ct.tenHienThi.contains("tiêm", true) || ct.tenHienThi.contains("truyền", true) -> "TIEM_TRUYEN"
                ct.tenHienThi.contains("băng", true) -> "THAY_BANG"
                ct.tenHienThi.contains("thủ tục", true) || ct.tenHienThi.contains("hồ sơ", true) -> "THU_TUC"
                else -> "KHAC"
            }
            repo.giaoViec(nvId, ct.tenHienThi, loai, ct.thanhTien, tyLe, donId)
            cacheChiTiet.value = cacheChiTiet.value // giữ nguyên cache
            thongBao.value = "Đã giao '${ct.tenHienThi}' cho NV"
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

    fun suaViec(id: String, ten: String, nvId: String, tongGia: Long, tyLe: Double) = viewModelScope.launch {
        runCatching {
            repo.suaViec(id, ten, nvId, tongGia, tyLe)
            thongBao.value = "Đã cập nhật nhiệm vụ"
        }.onFailure { thongBao.value = it.message ?: "Sửa thất bại" }
    }

    fun xoaViec(id: String) = viewModelScope.launch {
        runCatching { repo.xoaViec(id); thongBao.value = "Đã xóa nhiệm vụ" }
            .onFailure { thongBao.value = it.message ?: "Xóa thất bại" }
    }
}
