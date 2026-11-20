package vn.rinso.qldvyt.ui.luong

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.pdf.PdfExporter
import vn.rinso.qldvyt.data.pdf.ThongTinCongTy
import vn.rinso.qldvyt.data.prefs.CaiDat
import vn.rinso.qldvyt.data.repo.LuongRepository

/** ViewModel lương: chọn tháng → tính + chốt/mở khóa + xuất PDF phiếu/bảng lương. */
@HiltViewModel
class LuongViewModel @Inject constructor(
    private val repo: LuongRepository,
    private val pdf: PdfExporter,
    private val caiDat: CaiDat,
    nhanVienDao: vn.rinso.qldvyt.data.local.NhanVienDao,
    private val capBacDao: vn.rinso.qldvyt.data.local.CapBacDao,
) : ViewModel() {
    val dsNv = nhanVienDao.tatCa()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun tenNv(id: String): String = dsNv.value.firstOrNull { it.id == id }?.let { "${it.hoTen} • ${it.maNv}" } ?: "NV: ${id.take(8)}…"
    fun nvTheoId(id: String) = dsNv.value.firstOrNull { it.id == id }
    suspend fun tenCapBac(capBacId: String): String = try { capBacDao.theoId(capBacId)?.ten ?: "" } catch (_: Exception) { "" }

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
            thongBao.value = "Đã tính lương ${ds.size} nhân viên tháng ${thang.value}/${nam.value} (theo công thức ở Cài đặt)"
        }.onFailure { thongBao.value = it.message ?: "Tính lương thất bại" }
    }

    fun chot(nvId: String) = viewModelScope.launch {
        runCatching { repo.chot(nvId, thang.value, nam.value); thongBao.value = "Đã chốt bảng lương" }
            .onFailure { thongBao.value = it.message ?: "Chốt thất bại" }
    }

    fun moKhoa(nvId: String) = viewModelScope.launch {
        runCatching { repo.moKhoa(nvId, thang.value, nam.value); thongBao.value = "Đã mở khóa (admin)" }
            .onFailure { thongBao.value = it.message ?: "Mở khóa thất bại" }
    }

    private suspend fun thongTinCty(): ThongTinCongTy = ThongTinCongTy(
        ten = caiDat.tenCongTy.first(), diaChi = caiDat.diaChiCongTy.first(),
        sdt = caiDat.sdtCongTy.first(), maSoThue = caiDat.maSoThue.first(),
    )

    /** Xuất phiếu lương PDF cho 1 nhân viên. */
    fun xuatPhieuLuong(nvId: String, uri: android.net.Uri) = viewModelScope.launch {
        runCatching {
            val bl = dsBangLuong.value.firstOrNull { it.nhanVienId == nvId } ?: error("Chưa có bảng lương — hãy bấm Tính lương cả tháng")
            val nv = nvTheoId(nvId) ?: error("Không tìm thấy nhân viên")
            val capTen = tenCapBac(nv.capBacId)
            pdf.xuatPhieuLuong(uri, bl, nv, capTen, thongTinCty())
            thongBao.value = "Đã xuất phiếu lương PDF: ${nv.hoTen}"
        }.onFailure { thongBao.value = it.message ?: "Xuất PDF thất bại" }
    }

    /** Xuất bảng lương tổng hợp cả tháng ra PDF. */
    fun xuatBangTongHop(uri: android.net.Uri) = viewModelScope.launch {
        runCatching {
            val ds = dsBangLuong.value
            require(ds.isNotEmpty()) { "Chưa có bảng lương — hãy bấm Tính lương cả tháng" }
            val mapNv = dsNv.value.associateBy { it.id }
            val rows = ds.mapNotNull { bl ->
                val nv = mapNv[bl.nhanVienId] ?: return@mapNotNull null
                val capTen = try { capBacDao.theoId(nv.capBacId)?.ten ?: "" } catch (_: Exception) { "" }
                Triple(nv, bl, capTen)
            }
            pdf.xuatBangLuongThang(uri, thang.value, nam.value, rows, thongTinCty())
            thongBao.value = "Đã xuất bảng lương tổng hợp PDF (${rows.size} nhân viên)"
        }.onFailure { thongBao.value = it.message ?: "Xuất PDF thất bại" }
    }
}
