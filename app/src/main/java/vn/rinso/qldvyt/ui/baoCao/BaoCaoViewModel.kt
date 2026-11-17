package vn.rinso.qldvyt.ui.baoCao

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import javax.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.backup.SaoLuuManager
import vn.rinso.qldvyt.data.local.BangLuongDao
import vn.rinso.qldvyt.data.local.DichVuDao
import vn.rinso.qldvyt.data.local.DonHangChiTietDao
import vn.rinso.qldvyt.data.local.DonHangDao

/** Tên nhóm sổ doanh thu tiếng Việt (giống sổ tay công ty). */
fun tenNhomSo(nhom: String): String = when (nhom) {
    "TIEM_TRUYEN" -> "Tiêm truyền tại nhà"
    "DI_XE_TRONG" -> "Đi xe – Trông bệnh nhân"
    "THU_TUC" -> "Làm thủ tục – Giấy tờ"
    "CHAM_SOC" -> "Chăm sóc tại nhà"
    "VAT_LY" -> "Vật lý trị liệu"
    else -> "Khác"
}

/** 1 dòng doanh thu theo nhóm sổ. */
data class CotNhom(val nhom: String, val tong: Long, val soDon: Int)

/** 1 cột doanh thu theo cụm ngày (vẽ biểu đồ). */
data class CotNgay(val nhan: String, val tong: Long)

/** ViewModel Báo cáo tổng hợp kiểu MISA: doanh thu + nhóm sổ + biểu đồ + lương + CSV. */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class BaoCaoViewModel @Inject constructor(
    private val donDao: DonHangDao,
    private val chiTietDao: DonHangChiTietDao,
    private val dichVuDao: DichVuDao,
    private val bangLuongDao: BangLuongDao,
    private val saoLuu: SaoLuuManager,
) : ViewModel() {
    private val cal = Calendar.getInstance()
    val thang = MutableStateFlow(cal.get(Calendar.MONTH) + 1)
    val nam = MutableStateFlow(cal.get(Calendar.YEAR))
    val thongBao = MutableStateFlow("")

    private fun dauCuoiThang(t: Int, n: Int): Pair<Long, Long> {
        val tu = Calendar.getInstance().apply { set(n, t - 1, 1, 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        val den = Calendar.getInstance().apply {
            set(n, t - 1, getActualMaximum(Calendar.DAY_OF_MONTH), 23, 59, 59)
        }
        return tu.timeInMillis to den.timeInMillis
    }

    private val thangNam = combine(thang, nam) { t: Int, n: Int -> t to n }

    val doanhThuThang = thangNam.flatMapLatest { (t, n) ->
        val (tu, den) = dauCuoiThang(t, n)
        donDao.doanhThu(tu, den)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    val dsLuong = thangNam.flatMapLatest { (t, n) ->
        bangLuongDao.theoThang(t, n)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val dsDon = donDao.moiNhat()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val tongNo = donDao.tongCongNo()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    /** Toàn bộ đơn trong tháng đang xem (mọi trạng thái). */
    val dsDonThang = thangNam.flatMapLatest { (t, n) ->
        val (tu, den) = dauCuoiThang(t, n)
        flow { emit(donDao.trongKy(tu, den)) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Doanh thu group theo nhóm sổ (chỉ đơn HOAN_THANH). */
    val theoNhom = thangNam.flatMapLatest { (t, n) ->
        val (tu, den) = dauCuoiThang(t, n)
        flow {
            val cts = chiTietDao.trongKyHoanThanh(tu, den)
            val dsDv = try { dichVuDao.tatCa().first() } catch (_: Exception) { emptyList() }
            val mapNhom = dsDv.associate { it.id to it.nhom }
            val g = cts.groupBy { ct -> ct.dichVuId?.let { mapNhom[it] } ?: "KHAC" }
                .map { (nhom, list) -> CotNhom(nhom, list.sumOf { it.thanhTien }, list.size) }
                .sortedByDescending { it.tong }
            emit(g)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Biểu đồ gom 3 ngày/cột trong tháng đang xem. */
    val bieuDoNgay = combine(thang, nam, dsDonThang) { t, n, dsDonThangVal ->
        val ds = dsDonThangVal.filter { it.trangThai == "HOAN_THANH" }
        if (ds.isEmpty()) return@combine emptyList()
        val (tu, _) = dauCuoiThang(t, n)
        val c0 = Calendar.getInstance().apply { timeInMillis = tu }
        val soNgay = c0.getActualMaximum(Calendar.DAY_OF_MONTH)
        val res = mutableListOf<CotNgay>()
        var i = 1
        while (i <= soNgay) {
            val j = minOf(i + 2, soNgay)
            val tong = ds.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.ngayTao }
                val d = c.get(Calendar.DAY_OF_MONTH)
                d in i..j
            }.sumOf { it.tongThanhToan }
            res += CotNgay("$i-$j", tong)
            i += 3
        }
        res
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun xuatCsvDon(uri: Uri) = viewModelScope.launch {
        runCatching {
            val ds = dsDonThang.value
            val hang: List<Map<String, Any?>> = ds.map { don ->
                mapOf(
                    "MaDon" to don.maDh, "Ngay" to don.ngayTao,
                    "TrangThai" to don.trangThai, "TongHang" to don.tongTienHang,
                    "GiamGia" to don.giamGia, "ThanhToan" to don.tongThanhToan,
                    "DaThu" to don.daThu, "ConNo" to don.conNo,
                )
            }
            saoLuu.xuatCsv("don_hang", uri, hang)
            thongBao.value = "Đã xuất CSV ${ds.size} đơn tháng ${thang.value}/${nam.value}"
        }.onFailure { thongBao.value = it.message ?: "Xuất thất bại" }
    }

    fun xuatCsvLuong(uri: Uri) = viewModelScope.launch {
        runCatching {
            val ds = dsLuong.value
            val hang: List<Map<String, Any?>> = ds.map { bl ->
                mapOf(
                    "NV" to bl.nhanVienId.take(8), "Thang" to "${bl.thang}/${bl.nam}",
                    "Cong" to bl.tongCong, "TheoCong" to bl.luongCoBan,
                    "NhiemVu30" to bl.tienNhiemVu, "Thuong" to bl.thuong,
                    "ChuyenCan" to bl.chuyenCan, "TamUng" to bl.tamUng,
                    "Phat" to bl.khauTruKhac, "ThucLinh" to bl.thucLinh,
                    "TrangThai" to bl.trangThai,
                )
            }
            saoLuu.xuatCsv("bang_luong", uri, hang)
            thongBao.value = "Đã xuất CSV lương ${ds.size} dòng"
        }.onFailure { thongBao.value = it.message ?: "Xuất thất bại" }
    }

    fun xuatCsvSoDoanhThu(uri: Uri) = viewModelScope.launch {
        runCatching {
            val hang = theoNhom.value.map {
                mapOf("Nhom" to tenNhomSo(it.nhom), "SoDon" to it.soDon, "DoanhThu" to it.tong)
            }
            saoLuu.xuatCsv("so_doanh_thu", uri, hang)
            thongBao.value = "Đã xuất CSV sổ doanh thu (${hang.size} nhóm)"
        }.onFailure { thongBao.value = it.message ?: "Xuất thất bại" }
    }
}
