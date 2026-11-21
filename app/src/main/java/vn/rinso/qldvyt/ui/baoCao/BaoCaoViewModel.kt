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

/** 1 cột doanh thu theo từng ngày trong tháng (chi tiết từng ngày 1..31). */
data class CotNgay(val ngay: Int, val nhan: String, val tong: Long, val soDon: Int, val tienVat: Long)

/** Thống kê tổng hợp tháng để hiển thị KPI. */
data class ThongKeThang(
    val doanhThu: Long = 0L,
    val tienVat: Long = 0L,
    val truocVat: Long = 0L,
    val soDonHoanThanh: Int = 0,
    val soDonCho: Int = 0,
    val soDonDangLam: Int = 0,
    val soDonHuy: Int = 0,
    val tongNo: Long = 0L,
    val trungBinhDon: Long = 0L,
    val ngayCaoNhat: Int = 0,
    val doanhThuCaoNhat: Long = 0L,
)

/** ViewModel Báo cáo tổng hợp: doanh thu từng ngày + nhóm sổ + lương + CSV. */
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
    /** Ngày đang chọn để xem chi tiết (null = chưa chọn). */
    val ngayChon = MutableStateFlow<Int?>(null)

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

    /** Thống kê KPI tháng: VAT, số đơn theo trạng thái, trung bình, ngày cao nhất. */
    val thongKe = combine(dsDonThang, tongNo) { ds, no ->
        val xong = ds.filter { it.trangThai == "HOAN_THANH" }
        val vat = xong.sumOf { it.tienVat }
        val dt = xong.sumOf { it.tongThanhToan }
        val ngayMax = xong.groupBy {
            Calendar.getInstance().apply { timeInMillis = it.ngayTao }.get(Calendar.DAY_OF_MONTH)
        }.maxByOrNull { (_, l) -> l.sumOf { d -> d.tongThanhToan } }
        ThongKeThang(
            doanhThu = dt,
            tienVat = vat,
            truocVat = dt - vat,
            soDonHoanThanh = xong.size,
            soDonCho = ds.count { it.trangThai == "CHO_XU_LY" },
            soDonDangLam = ds.count { it.trangThai == "DANG_LAM" },
            soDonHuy = ds.count { it.trangThai == "HUY" },
            tongNo = no,
            trungBinhDon = if (xong.isNotEmpty()) dt / xong.size else 0L,
            ngayCaoNhat = ngayMax?.key ?: 0,
            doanhThuCaoNhat = ngayMax?.value?.sumOf { it.tongThanhToan } ?: 0L,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ThongKeThang())

    /** Doanh thu group theo nhóm sổ (chỉ đơn HOÀN THÀNH). */
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

    /** Biểu đồ chi tiết từng ngày 1..31 trong tháng đang xem. */
    val bieuDoNgay = combine(thang, nam, dsDonThang) { t, n, dsDonThangVal ->
        val ds = dsDonThangVal.filter { it.trangThai == "HOAN_THANH" }
        val (tu, _) = dauCuoiThang(t, n)
        val c0 = Calendar.getInstance().apply { timeInMillis = tu }
        val soNgay = c0.getActualMaximum(Calendar.DAY_OF_MONTH)
        (1..soNgay).map { d ->
            val list = ds.filter {
                val c = Calendar.getInstance().apply { timeInMillis = it.ngayTao }
                c.get(Calendar.DAY_OF_MONTH) == d
            }
            CotNgay(d, d.toString(), list.sumOf { it.tongThanhToan }, list.size, list.sumOf { it.tienVat })
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    /** Doanh thu tháng trước (để so sánh). */
    val doanhThuThangTruoc = thangNam.flatMapLatest { (t, n) ->
        val tt = if (t == 1) 12 else t - 1
        val nn = if (t == 1) n - 1 else n
        val (tu, den) = dauCuoiThang(tt, nn)
        donDao.doanhThu(tu, den)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0L)

    /** Xu hướng 6 tháng gần nhất (để vẽ biểu đồ đường): tháng + doanh thu. */
    data class CotThang(val nhan: String, val tong: Long)
    val xuHuong6Thang = thangNam.flatMapLatest { (t, n) ->
        flow {
            val list = (5 downTo 0).map { lui ->
                var tt = t - lui; var nn = n
                while (tt <= 0) { tt += 12; nn -= 1 }
                val (tu, den) = dauCuoiThang(tt, nn)
                val dt = try { donDao.doanhThuMotLan(tu, den) } catch (_: Exception) { 0L }
                CotThang("$tt", dt)
            }
            emit(list)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun xuatCsvDon(uri: Uri) = viewModelScope.launch {
        runCatching {
            val ds = dsDonThang.value
            val hang: List<Map<String, Any?>> = ds.map { don ->
                mapOf(
                    "MaDon" to don.maDh, "Ngay" to don.ngayTao,
                    "TrangThai" to don.trangThai, "TongHang" to don.tongTienHang,
                    "GiamGia" to don.giamGia, "TienVAT" to don.tienVat,
                    "TyLeVAT" to don.tyLeVat, "ThanhToan" to don.tongThanhToan,
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
                    "NhiemVu30" to bl.tienNhiemVu, "HoaHong" to bl.hoaHong,
                    "Thuong" to bl.thuong, "ChuyenCan" to bl.chuyenCan,
                    "PhuCap" to bl.phuCap, "TamUng" to bl.tamUng,
                    "Phat" to bl.khauTruKhac, "ThueTNCN" to bl.thueTncn,
                    "ThucLinh" to bl.thucLinh, "TrangThai" to bl.trangThai,
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

    /** Xuất CSV chi tiết từng ngày trong tháng (ngày, số đơn, VAT, doanh thu). */
    fun xuatCsvNgay(uri: Uri) = viewModelScope.launch {
        runCatching {
            val hang = bieuDoNgay.value.map {
                mapOf("Ngay" to it.ngay, "SoDon" to it.soDon, "TienVAT" to it.tienVat, "DoanhThu" to it.tong)
            }
            saoLuu.xuatCsv("doanh_thu_ngay", uri, hang)
            thongBao.value = "Đã xuất CSV doanh thu từng ngày (${hang.size} ngày)"
        }.onFailure { thongBao.value = it.message ?: "Xuất thất bại" }
    }
}
