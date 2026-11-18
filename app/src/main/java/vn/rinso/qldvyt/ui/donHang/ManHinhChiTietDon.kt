package vn.rinso.qldvyt.ui.donHang

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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.flow.first
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.data.local.DonHang
import vn.rinso.qldvyt.data.local.DonHangChiTiet
import vn.rinso.qldvyt.data.local.DonHangDao
import vn.rinso.qldvyt.data.local.ThanhToan
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.repo.DonHangRepository
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.mauTrangThaiDon

@HiltViewModel
class ChiTietDonViewModel @Inject constructor(
    private val repo: DonHangRepository,
    private val donDao: DonHangDao,
) : ViewModel() {
    var don by mutableStateOf<DonHang?>(null)
        private set
    var chiTiet by mutableStateOf<List<DonHangChiTiet>>(emptyList())
        private set
    var lichSuThu by mutableStateOf<List<ThanhToan>>(emptyList())
        private set
    var tb by mutableStateOf("")
        private set

    fun tai(donId: String) = viewModelScope.launch {
        runCatching {
            don = donDao.theoId(donId)
            chiTiet = repo.chiTietTheoDon(donId)
            lichSuThu = repo.thanhToanTheoDon(donId).first()
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

/** Chi tiết đơn kiểu MISA: tiền + mặt hàng + thu nhiều lần + lịch sử + đổi trạng thái. GD1/GD2. */
@Composable
fun ManHinhChiTietDon(donId: String, vm: ChiTietDonViewModel = hiltViewModel()) {
    LaunchedEffect(donId) { vm.tai(donId) }
    val don = vm.don
    var oTien by remember { mutableStateOf("") }
    var phuongThuc by remember { mutableStateOf("TIENMAT") }

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
        Text("${dinhDangNgay(don.ngayTao)}${don.ngayHen?.let { " • Hẹn: " + dinhDangNgay(it) } ?: ""}", style = MaterialTheme.typography.bodySmall)
        if (vm.tb.isNotEmpty()) Card { Text(vm.tb, Modifier.padding(12.dp)) }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tổng hàng: ${dinhDangTien(don.tongTienHang)} • Giảm: ${dinhDangTien(don.giamGia)}")
                Text("Thanh toán: ${dinhDangTien(don.tongThanhToan)}", fontWeight = FontWeight.Bold)
                Text(
                    "Đã thu: ${dinhDangTien(don.daThu)} • Còn nợ: ${dinhDangTien(don.conNo)}",
                    fontWeight = FontWeight.Bold,
                    color = if (don.conNo > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                )
            }
        }
        Text("Mặt hàng (giá snapshot chống trượt giá):", fontWeight = FontWeight.Bold)
        vm.chiTiet.forEach { ct ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${ct.tenHienThi} × ${ct.soLuong.toInt()}", modifier = Modifier.weight(1f))
                    Text(dinhDangTien(ct.thanhTien), fontWeight = FontWeight.Bold)
                }
            }
        }
        if (vm.chiTiet.isEmpty()) Text("Đơn chưa có mặt hàng.", style = MaterialTheme.typography.bodySmall)
        if (don.conNo > 0 && don.trangThai != "HUY") {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Thu tiền (còn nợ ${dinhDangTien(don.conNo)}):", fontWeight = FontWeight.Bold)
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
            Text("Lịch sử thu (${vm.lichSuThu.size} lần):", fontWeight = FontWeight.Bold)
            vm.lichSuThu.forEach { tt ->
                Text("• ${dinhDangNgay(tt.ngay)}: ${dinhDangTien(tt.soTien)} (${if (tt.phuongThuc == "CK") "CK" else "Tiền mặt"})${if (tt.ghiChu.isNotEmpty()) " — ${tt.ghiChu}" else ""}")
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
