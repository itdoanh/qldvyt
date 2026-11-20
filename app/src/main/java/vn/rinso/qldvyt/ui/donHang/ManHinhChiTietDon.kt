package vn.rinso.qldvyt.ui.donHang

import android.content.Intent
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
}

/** Chi tiet don v3: ten KH that + tien do thu + phieu thu share + lich su. */
@Composable
fun ManHinhChiTietDon(donId: String, vm: ChiTietDonViewModel = hiltViewModel()) {
    LaunchedEffect(donId) { vm.tai(donId) }
    val don = vm.don
    val ctx = LocalContext.current
    var oTien by remember { mutableStateOf("") }
    var phuongThuc by remember { mutableStateOf("TIENMAT") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (don == null) {
            Text("Dang tai don...", style = MaterialTheme.typography.bodyLarge)
            if (vm.tb.isNotEmpty()) Text(vm.tb)
            return@Column
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(don.maDh, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
            NhanTrangThai(don.trangThai, tenTrangThaiDon(don.trangThai), mauTrangThaiDon(don.trangThai))
        }
        Text("KH: ${vm.khach?.hoTen ?: "KH le"} - SDT: ${vm.khach?.sdt?.ifEmpty { "—" } ?: "—"}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
        if (!vm.khach?.diaChi.isNullOrEmpty()) Text(vm.khach!!.diaChi, style = MaterialTheme.typography.bodySmall)
        Text("${dinhDangNgay(don.ngayTao)}${don.ngayHen?.let { " - Hen: " + dinhDangNgay(it) } ?: ""}", style = MaterialTheme.typography.bodySmall)
        if (vm.tb.isNotEmpty()) Card { Text(vm.tb, Modifier.padding(12.dp)) }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tong hang: ${dinhDangTien(don.tongTienHang)} - Giam: ${dinhDangTien(don.giamGia)}")
                Text("Thanh toan: ${dinhDangTien(don.tongThanhToan)}", fontWeight = FontWeight.Bold)
                Text(
                    "Da thu: ${dinhDangTien(don.daThu)} - Con no: ${dinhDangTien(don.conNo)}",
                    fontWeight = FontWeight.Bold,
                    color = if (don.conNo > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
                val tyLe = if (don.tongThanhToan > 0) don.daThu.toFloat() / don.tongThanhToan else 0f
                HangTienTrinh("Tien do thu tien", tyLe, MaterialTheme.colorScheme.primary)
                OutlinedButton(onClick = {
                    val noiDung = buildString {
                        appendLine("PHIEU THU - RINSO QLDVYT")
                        appendLine(don.maDh + " - " + dinhDangNgay(don.ngayTao))
                        appendLine("KH: ${vm.khach?.hoTen ?: ""} - ${vm.khach?.sdt ?: ""}")
                        appendLine("Tong: ${dinhDangTien(don.tongThanhToan)} - Da thu: ${dinhDangTien(don.daThu)} - Con no: ${dinhDangTien(don.conNo)}")
                        appendLine("Cam on quy khach!")
                    }
                    ctx.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, noiDung)
                    }, "Chia se phieu thu"))
                }, modifier = Modifier.fillMaxWidth()) { Text("Chia se phieu thu (Zalo/SMS)") }
            }
        }
        Text("Mat hang (gia snapshot chong truot gia):", fontWeight = FontWeight.Bold)
        vm.chiTiet.forEach { ct ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${ct.tenHienThi} x ${ct.soLuong.toInt()}", modifier = Modifier.weight(1f))
                    Text(dinhDangTien(ct.thanhTien), fontWeight = FontWeight.Bold)
                }
            }
        }
        if (vm.chiTiet.isEmpty()) Text("Don chua co mat hang.", style = MaterialTheme.typography.bodySmall)
        if (don.conNo > 0 && don.trangThai != "HUY") {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Thu tien (con no ${dinhDangTien(don.conNo)}):", fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { oTien = don.conNo.toString() }, modifier = Modifier.weight(1f)) { Text("Thu dung con no") }
                        OutlinedButton(onClick = { oTien = (don.conNo / 2).toString() }, modifier = Modifier.weight(1f)) { Text("Thu 1/2") }
                    }
                    OutlinedTextField(oTien, { oTien = it.filter(Char::isDigit) }, label = { Text("So tien (VND)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(onClick = { phuongThuc = "TIENMAT" }, modifier = Modifier.weight(1f)) {
                            Text(if (phuongThuc == "TIENMAT") "✓ Tien mat" else "Tien mat")
                        }
                        OutlinedButton(onClick = { phuongThuc = "CK" }, modifier = Modifier.weight(1f)) {
                            Text(if (phuongThuc == "CK") "✓ Chuyen khoan" else "Chuyen khoan")
                        }
                    }
                    Button(onClick = {
                        oTien.toLongOrNull()?.let { vm.thuTien(it, phuongThuc) }
                        oTien = ""
                    }, modifier = Modifier.fillMaxWidth()) { Text("Xac nhan thu") }
                }
            }
        }
        if (vm.lichSuThu.isNotEmpty()) {
            Text("Lich su thu (${vm.lichSuThu.size} lan):", fontWeight = FontWeight.Bold)
            vm.lichSuThu.forEach { tt ->
                Text("- ${dinhDangNgay(tt.ngay)}: ${dinhDangTien(tt.soTien)} (${if (tt.phuongThuc == "CK") "CK" else "Tien mat"})${if (tt.ghiChu.isNotEmpty()) " - ${tt.ghiChu}" else ""}")
            }
        }
        Text("Doi trang thai:", fontWeight = FontWeight.Bold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.doiTrangThai("DANG_LAM") }) { Text("Dang lam") }
            OutlinedButton(onClick = { vm.doiTrangThai("HOAN_THANH") }) { Text("Hoan thanh") }
            OutlinedButton(onClick = { vm.doiTrangThai("HUY") }) { Text("Huy") }
        }
        if (don.ghiChu.isNotEmpty()) Text("Ghi chu: ${don.ghiChu}", style = MaterialTheme.typography.bodySmall)
    }
}
