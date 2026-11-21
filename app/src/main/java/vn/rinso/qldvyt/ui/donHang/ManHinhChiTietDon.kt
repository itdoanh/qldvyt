package vn.rinso.qldvyt.ui.donHang

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.first
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.data.local.DonHang
import vn.rinso.qldvyt.data.local.DonHangChiTiet
import vn.rinso.qldvyt.data.local.DonHangDao
import vn.rinso.qldvyt.data.local.KhachHang
import vn.rinso.qldvyt.data.local.KhachHangDao
import vn.rinso.qldvyt.data.local.ThanhToan
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.repo.DonHangRepository
import vn.rinso.qldvyt.ui.common.HangTienTrinh
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.mauTrangThaiDon

@HiltViewModel
class ChiTietDonViewModel @Inject constructor(
    private val repo: DonHangRepository,
    private val donDao: DonHangDao,
    private val khDao: KhachHangDao,
    private val pdf: vn.rinso.qldvyt.data.pdf.PdfExporter,
    private val caiDat: vn.rinso.qldvyt.data.prefs.CaiDat,
) : ViewModel() {
    var don by mutableStateOf<DonHang?>(null)
        private set
    var chiTiet by mutableStateOf<List<DonHangChiTiet>>(emptyList())
        private set
    var lichSuThu by mutableStateOf<List<ThanhToan>>(emptyList())
        private set
    var khach by mutableStateOf<KhachHang?>(null)
        private set
    var tb by mutableStateOf("")
        private set

    fun tai(donId: String) = viewModelScope.launch {
        runCatching {
            don = donDao.theoId(donId)
            chiTiet = repo.chiTietTheoDon(donId)
            lichSuThu = repo.thanhToanTheoDon(donId).first()
            khach = don?.let { khDao.theoId(it.khachHangId) }
        }.onFailure { tb = it.message ?: "Tải thất bại" }
    }

    fun thuTien(soTien: Long, phuongThuc: String) = viewModelScope.launch {
        runCatching {
            val id = don?.id ?: return@launch
            val conLai = (don?.conNo ?: 0L)
            require(soTien > 0) { "Số tiền phải > 0" }
            require(soTien <= conLai) { "Chỉ còn nợ ${dinhDangTien(conLai)} — nhập vượt!" }
            repo.thuTien(id, soTien, phuongThuc)
            tb = "Đã thu ${dinhDangTien(soTien)}"
            tai(id)
        }.onFailure { tb = it.message ?: "Thu thất bại" }
    }

    fun doiTrangThai(tt: String) = viewModelScope.launch {
        runCatching {
            val id = don?.id ?: return@launch
            repo.doiTrangThai(id, tt)
            tb = "Đã chuyển: ${tenTrangThaiDon(tt)}"
            tai(id)
        }.onFailure { tb = it.message ?: "Đổi thất bại" }
    }

    /** Sửa đơn: giảm giá / ghi chú / ngày hẹn — tiền tự tính lại. */
    fun suaDon(giamMoi: Long, ghiChuMoi: String) = viewModelScope.launch {
        runCatching {
            val id = don?.id ?: return@launch
            repo.suaDon(id, giamMoi, ghiChuMoi, don?.ngayHen)
            tb = "Đã cập nhật đơn (tiền tự tính lại)"
            tai(id)
        }.onFailure { tb = it.message ?: "Sửa thất bại" }
    }

    /** Xóa 1 dòng thu tiền để điều chỉnh công nợ. */
    fun xoaThu(ttId: String) = viewModelScope.launch {
        runCatching {
            val id = don?.id ?: return@launch
            repo.xoaThanhToan(ttId, id)
            tb = "Đã xóa dòng thu (công nợ tự tính lại)"
            tai(id)
        }.onFailure { tb = it.message ?: "Xóa thất bại" }
    }

    /** Xuất hóa đơn PDF (có VAT theo cài đặt) ra file do người dùng chọn. */
    fun xuatHoaDonPdf(uri: android.net.Uri) = viewModelScope.launch {
        runCatching {
            val d = don ?: error("Chưa tải đơn")
            val ten = caiDat.tenCongTy.first()
            val dc = caiDat.diaChiCongTy.first()
            val sdt = caiDat.sdtCongTy.first()
            val mst = caiDat.maSoThue.first()
            pdf.xuatHoaDon(
                uri, d, khach, chiTiet, lichSuThu,
                vn.rinso.qldvyt.data.pdf.ThongTinCongTy(ten, dc, sdt, mst),
            )
            tb = "Đã xuất hóa đơn PDF ${d.maDh}"
        }.onFailure { tb = it.message ?: "Xuất PDF thất bại" }
    }
}

/** Chi tiết đơn: tên khách hàng thật + tiến độ thu + hóa đơn PDF + lịch sử. */
@Composable
fun ManHinhChiTietDon(donId: String, vm: ChiTietDonViewModel = hiltViewModel()) {
    LaunchedEffect(donId) { vm.tai(donId) }
    val don = vm.don
    val ctx = LocalContext.current
    var oTien by remember { mutableStateOf("") }
    var phuongThuc by remember { mutableStateOf("TIENMAT") }
    val chonPdf = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf")
    ) { uri -> if (uri != null) vm.xuatHoaDonPdf(uri) }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (don == null) {
            Text("Đang tải đơn...", style = MaterialTheme.typography.bodyLarge)
            if (vm.tb.isNotEmpty()) Text(vm.tb)
            return@Column
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(don.maDh, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            NhanTrangThai(don.trangThai, tenTrangThaiDon(don.trangThai), mauTrangThaiDon(don.trangThai))
        }
        Text("Khách hàng: ${vm.khach?.hoTen ?: "Khách lẻ"} - SĐT: ${vm.khach?.sdt?.ifEmpty { "—" } ?: "—"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        if (!vm.khach?.diaChi.isNullOrEmpty()) Text(vm.khach!!.diaChi, style = MaterialTheme.typography.bodySmall)
        Text("${dinhDangNgay(don.ngayTao)}${don.ngayHen?.let { " - Hẹn: " + dinhDangNgay(it) } ?: ""}", style = MaterialTheme.typography.bodySmall)
        if (vm.tb.isNotEmpty()) Card { Text(vm.tb, Modifier.padding(12.dp)) }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tổng tiền hàng: ${dinhDangTien(don.tongTienHang)} - Giảm giá: ${dinhDangTien(don.giamGia)}")
                if (don.tienVat > 0) Text("Thuế VAT (${(don.tyLeVat * 100).toInt()}%): ${dinhDangTien(don.tienVat)}", fontWeight = FontWeight.SemiBold)
                else Text("Thuế VAT: Không áp dụng", style = MaterialTheme.typography.bodySmall)
                Text("Tổng thanh toán: ${dinhDangTien(don.tongThanhToan)}", fontWeight = FontWeight.Bold)
                Text(
                    "Đã thu: ${dinhDangTien(don.daThu)} - Còn nợ: ${dinhDangTien(don.conNo)}",
                    fontWeight = FontWeight.Bold,
                    color = if (don.conNo > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
                val tyLe = if (don.tongThanhToan > 0) don.daThu.toFloat() / don.tongThanhToan else 0f
                HangTienTrinh("Tiến độ thu tiền", tyLe, MaterialTheme.colorScheme.primary)
                OutlinedButton(onClick = {
                    val noiDung = buildString {
                        appendLine("PHIẾU THU - RINSO QLDVYT")
                        appendLine(don.maDh + " - " + dinhDangNgay(don.ngayTao))
                        appendLine("KH: ${vm.khach?.hoTen ?: ""} - ${vm.khach?.sdt ?: ""}")
                        appendLine("Tổng: ${dinhDangTien(don.tongThanhToan)} (VAT ${dinhDangTien(don.tienVat)}) - Đã thu: ${dinhDangTien(don.daThu)} - Còn nợ: ${dinhDangTien(don.conNo)}")
                        appendLine("Cảm ơn quý khách!")
                    }
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, noiDung)
                    }, "Chia sẻ phiếu thu"))
                }, modifier = Modifier.fillMaxWidth()) { Text("Chia sẻ phiếu thu (Zalo/SMS)") }
                Button(onClick = { chonPdf.launch("hoa-don-${don.maDh}.pdf") }, modifier = Modifier.fillMaxWidth()) { Text("Xuất hóa đơn PDF (có VAT)") }
            }
        }
        Text("Mặt hàng (giá snapshot chống trượt giá):", fontWeight = FontWeight.Bold)
        vm.chiTiet.forEach { ct ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Column(Modifier.weight(1f)) {
                        Text("${ct.tenHienThi} x ${ct.soLuong.toInt()}", fontWeight = FontWeight.SemiBold)
                        Text(if (ct.chiuThue) "Chịu VAT" else "Không chịu VAT", style = MaterialTheme.typography.bodySmall)
                    }
                    Text(dinhDangTien(ct.thanhTien), fontWeight = FontWeight.Bold)
                }
            }
        }
        if (vm.chiTiet.isEmpty()) Text("Đơn chưa có mặt hàng.", style = MaterialTheme.typography.bodySmall)
        if (don.conNo > 0 && don.trangThai != "HUY") {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Thu tiền (còn nợ ${dinhDangTien(don.conNo)}):", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { oTien = don.conNo.toString() }, modifier = Modifier.weight(1f)) { Text("Thu đúng còn nợ") }
                        OutlinedButton(onClick = { oTien = (don.conNo / 2).toString() }, modifier = Modifier.weight(1f)) { Text("Thu 1/2") }
                    }
                    OutlinedTextField(oTien, { oTien = it.filter(Char::isDigit) }, label = { Text("Số tiền (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { phuongThuc = "TIENMAT" }, modifier = Modifier.weight(1f)) {
                            Text(if (phuongThuc == "TIENMAT") "✓ Tiền mặt" else "Tiền mặt")
                        }
                        OutlinedButton(onClick = { phuongThuc = "CK" }, modifier = Modifier.weight(1f)) {
                            Text(if (phuongThuc == "CK") "✓ Chuyển khoản" else "Chuyển khoản")
                        }
                    }
                    Button(onClick = {
                        oTien.toLongOrNull()?.let { vm.thuTien(it, phuongThuc) }
                        oTien = ""
                    }, modifier = Modifier.fillMaxWidth()) { Text("Xác nhận thu") }
                }
            }
        }
        if (vm.lichSuThu.isNotEmpty()) {
            Text("Lịch sử thu tiền (${vm.lichSuThu.size} lần):", fontWeight = FontWeight.Bold)
            vm.lichSuThu.forEach { tt ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                    Row(Modifier.padding(10.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("- ${dinhDangNgay(tt.ngay)}: ${dinhDangTien(tt.soTien)} (${if (tt.phuongThuc == "CK") "Chuyển khoản" else "Tiền mặt"})${if (tt.ghiChu.isNotEmpty()) " - ${tt.ghiChu}" else ""}", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { vm.xoaThu(tt.id) }) { Text("Xóa") }
                    }
                }
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            var moSua by remember(don.id) { mutableStateOf(false) }
            var oGiamMoi by remember(don.id) { mutableStateOf(don.giamGia.toString()) }
            var oGhiChuMoi by remember(don.id) { mutableStateOf(don.ghiChu) }
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Điều chỉnh đơn (giảm giá / ghi chú):", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { moSua = !moSua }) { Text(if (moSua) "Đóng" else "Sửa") }
                }
                if (moSua) {
                    OutlinedTextField(oGiamMoi, { oGiamMoi = it.filter(Char::isDigit) }, label = { Text("Giảm giá mới (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(oGhiChuMoi, { oGhiChuMoi = it }, label = { Text("Ghi chú") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        vm.suaDon(oGiamMoi.toLongOrNull() ?: 0L, oGhiChuMoi)
                        moSua = false
                    }, modifier = Modifier.fillMaxWidth()) { Text("Lưu thay đổi (tiền tự tính lại)") }
                }
            }
        }
        Text("Đổi trạng thái:", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.doiTrangThai("DANG_LAM") }) { Text("Đang làm") }
            OutlinedButton(onClick = { vm.doiTrangThai("HOAN_THANH") }) { Text("Hoàn thành") }
            OutlinedButton(onClick = { vm.doiTrangThai("HUY") }) { Text("Hủy") }
        }
        if (don.ghiChu.isNotEmpty()) Text("Ghi chú: ${don.ghiChu}", style = MaterialTheme.typography.bodySmall)
    }
}
