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
    private val khoPdf: vn.rinso.qldvyt.data.pdf.PdfLuuTru,
    private val caiDat: CaiDat,
    private val nhanVienDao: vn.rinso.qldvyt.data.local.NhanVienDao,
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
    /** File PDF vừa xuất xong → màn Lương tự mở trình xem trong app. */
    val pdfMoiXuat = MutableStateFlow<java.io.File?>(null)
    fun daMoPdf() { pdfMoiXuat.value = null }

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

    /** NV đang chờ xuất phiếu (tránh lỗi closure stale khiến file SAF tạo ra 0KB mà không ghi). */
    var pendingXuatNv: String? = null

    private suspend fun layBangLuongNv(nvId: String): vn.rinso.qldvyt.data.local.BangLuong {
        val bl = try {
            repo.theoThang(thang.value, nam.value).first()
                .firstOrNull { it.nhanVienId == nvId }
        } catch (_: Exception) { null }
            ?: dsBangLuong.value.firstOrNull { it.nhanVienId == nvId }
            ?: error("Chưa có bảng lương — hãy bấm Tính lương cả tháng")
        return bl
    }

    private suspend fun layHangTongHop(): List<Triple<vn.rinso.qldvyt.data.local.NhanVien, vn.rinso.qldvyt.data.local.BangLuong, String>> {
        val ds = try {
            repo.theoThang(thang.value, nam.value).first()
        } catch (_: Exception) { dsBangLuong.value }
        val dsDung = if (ds.isNotEmpty()) ds else dsBangLuong.value
        require(dsDung.isNotEmpty()) { "Chưa có bảng lương — hãy bấm Tính lương cả tháng" }
        val mapNv = try {
            nhanVienDao.tatCa().first().associateBy { it.id }
        } catch (_: Exception) { dsNv.value.associateBy { it.id } }
        val rows = dsDung.mapNotNull { bl ->
            val nv = mapNv[bl.nhanVienId] ?: return@mapNotNull null
            val capTen = try { capBacDao.theoId(nv.capBacId)?.ten ?: "" } catch (_: Exception) { "" }
            Triple(nv, bl, capTen)
        }
        require(rows.isNotEmpty()) { "Không khớp nhân viên — hãy Tính lương cả tháng rồi xuất lại" }
        return rows
    }

    /** Xuất phiếu lương PDF cho 1 nhân viên → lưu local + tự mở xem trong app. */
    fun xuatPhieuLuong(nvId: String) = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        runCatching {
            val bl = layBangLuongNv(nvId)
            val nv = try { nhanVienDao.theoId(nvId) } catch (_: Exception) { null }
                ?: nvTheoId(nvId) ?: error("Không tìm thấy nhân viên")
            val capTen = tenCapBac(nv.capBacId)
            val file = khoPdf.filePhieu(nv.maNv.ifBlank { nvId.take(8) }, thang.value, nam.value)
            pdf.xuatPhieuLuong(file, bl, nv, capTen, thongTinCty())
            pdfMoiXuat.value = file
            thongBao.value = "Đã lưu phiếu lương: ${file.name}"
        }.onFailure { thongBao.value = it.message ?: "Xuất PDF thất bại" }
    }

    /** Xuất phiếu lương ra Uri ngoài (giữ cho nút Lưu trong trình xem). */
    fun xuatPhieuLuong(nvId: String, uri: android.net.Uri) = viewModelScope.launch {
        runCatching {
            // Lấy tươi từ DB (không phụ thuộc StateFlow màn hình) để tránh file rỗng/0KB
            // khi dsBangLuong chưa kịp load hoặc đã chuyển tháng.
            val bl = try {
                repo.theoThang(thang.value, nam.value).first()
                    .firstOrNull { it.nhanVienId == nvId }
            } catch (_: Exception) { null }
                ?: dsBangLuong.value.firstOrNull { it.nhanVienId == nvId }
                ?: error("Chưa có bảng lương — hãy bấm Tính lương cả tháng")
            val nv = try { nhanVienDao.theoId(nvId) } catch (_: Exception) { null }
                ?: nvTheoId(nvId) ?: error("Không tìm thấy nhân viên")
            val capTen = tenCapBac(nv.capBacId)
            pdf.xuatPhieuLuong(uri, bl, nv, capTen, thongTinCty())
            thongBao.value = "Đã xuất phiếu lương PDF: ${nv.hoTen}"
        }.onFailure { thongBao.value = it.message ?: "Xuất PDF thất bại" }
    }

    /** Xuất bảng tổng hợp → lưu local + tự mở xem trong app. */
    fun xuatBangTongHop() = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
        runCatching {
            val rows = layHangTongHop()
            val file = khoPdf.fileBangTong(thang.value, nam.value)
            pdf.xuatBangLuongThang(file, thang.value, nam.value, rows, thongTinCty())
            pdfMoiXuat.value = file
            thongBao.value = "Đã lưu bảng tổng hợp: ${file.name} (${rows.size} NV)"
        }.onFailure { thongBao.value = it.message ?: "Xuất PDF thất bại" }
    }

    /** Xuất bảng lương tổng hợp cả tháng ra PDF (SAF — giữ cho nút Lưu). */
    fun xuatBangTongHop(uri: android.net.Uri) = viewModelScope.launch {
        runCatching {
            val ds = try {
                repo.theoThang(thang.value, nam.value).first()
            } catch (_: Exception) { dsBangLuong.value }
            val dsDung = if (ds.isNotEmpty()) ds else dsBangLuong.value
            require(dsDung.isNotEmpty()) { "Chưa có bảng lương — hãy bấm Tính lương cả tháng" }
            val mapNv = try {
                nhanVienDao.tatCa().first().associateBy { it.id }
            } catch (_: Exception) { dsNv.value.associateBy { it.id } }
            val rows = dsDung.mapNotNull { bl ->
                val nv = mapNv[bl.nhanVienId] ?: return@mapNotNull null
                val capTen = try { capBacDao.theoId(nv.capBacId)?.ten ?: "" } catch (_: Exception) { "" }
                Triple(nv, bl, capTen)
            }
            require(rows.isNotEmpty()) { "Không khớp nhân viên — hãy Tính lương cả tháng rồi xuất lại" }
            pdf.xuatBangLuongThang(uri, thang.value, nam.value, rows, thongTinCty())
            thongBao.value = "Đã xuất bảng lương tổng hợp PDF (${rows.size} nhân viên)"
        }.onFailure { thongBao.value = it.message ?: "Xuất PDF thất bại" }
    }

    // ===== Sửa tạm ứng / thưởng-phạt từng NV trong tháng đang xem =====
    fun tamUngKy(nvId: String): kotlinx.coroutines.flow.Flow<List<vn.rinso.qldvyt.data.local.TamUng>> =
        repo.tamUngKy(nvId, thang.value, nam.value)

    fun thuongPhatKy(nvId: String): kotlinx.coroutines.flow.Flow<List<vn.rinso.qldvyt.data.local.ThuongPhat>> =
        repo.thuongPhatKy(nvId, thang.value, nam.value)

    fun themTamUngThang(nvId: String, soTien: Long, lyDo: String) = viewModelScope.launch {
        runCatching {
            repo.themTamUng(nvId, soTien, lyDo, thang.value, nam.value)
            thongBao.value = "Đã thêm tạm ứng ${vn.rinso.qldvyt.core.dinhDangTien(soTien)}"
        }.onFailure { thongBao.value = it.message ?: "Thêm tạm ứng thất bại" }
    }

    fun themThuongThang(nvId: String, soTien: Long, lyDo: String) = viewModelScope.launch {
        runCatching {
            repo.themThuongPhat(nvId, "THUONG", soTien, lyDo, thang.value, nam.value)
            thongBao.value = "Đã thêm thưởng ${vn.rinso.qldvyt.core.dinhDangTien(soTien)}"
        }.onFailure { thongBao.value = it.message ?: "Thêm thưởng thất bại" }
    }

    fun themPhatThang(nvId: String, soTien: Long, lyDo: String) = viewModelScope.launch {
        runCatching {
            repo.themThuongPhat(nvId, "PHAT", -soTien, lyDo, thang.value, nam.value)
            thongBao.value = "Đã thêm phạt ${vn.rinso.qldvyt.core.dinhDangTien(soTien)}"
        }.onFailure { thongBao.value = it.message ?: "Thêm phạt thất bại" }
    }

    fun xoaTamUngThang(id: String, nvId: String) = viewModelScope.launch {
        runCatching {
            repo.xoaTamUng(id, nvId, thang.value, nam.value)
            thongBao.value = "Đã xóa tạm ứng (lương tự tính lại)"
        }.onFailure { thongBao.value = it.message ?: "Xóa thất bại" }
    }

    fun xoaThuongPhatThang(id: String, nvId: String) = viewModelScope.launch {
        runCatching {
            repo.xoaThuongPhat(id, nvId, thang.value, nam.value)
            thongBao.value = "Đã xóa dòng thưởng/phạt (lương tự tính lại)"
        }.onFailure { thongBao.value = it.message ?: "Xóa thất bại" }
    }
}
