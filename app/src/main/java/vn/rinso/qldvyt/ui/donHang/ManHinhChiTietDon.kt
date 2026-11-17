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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
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

    fun tai(donId: String) = viewModelScope.launch {
        don = donDao.theoId(donId)
        chiTiet = repo.chiTietTheoDon(donId)
        lichSuThu = repo.thanhToanTheoDon(donId).first()
    }

    fun thuTien(soTien: Long, phuongThuc: String) = viewModelScope.launch {
        val id = don?.id ?: return@launch
        repo.thuTien(id, soTien, phuongThuc)
        tai(id)
    }

    fun doiTrangThai(tt: String) = viewModelScope.launch {
        val id = don?.id ?: return@launch
        repo.doiTrangThai(id, tt)
        tai(id)
    }
}

/** Màn chi tiết đơn: mặt hàng snapshot + vật tư vốn + thu tiền + đổi trạng thái. */
@Composable
fun ManHinhChiTietDon(donId: String, vm: ChiTietDonViewModel = hiltViewModel()) {
    LaunchedEffect(donId) { vm.tai(donId) }
    val don = vm.don
    var oTien by remember { mutableStateOf("") }
    var phuongThuc by remember { mutableStateOf("TIENMAT") }

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        if (don == null) {
            Text("Đang tải đơn...")
            return@Column
        }
        Text(don.maDh, style = MaterialTheme.typography.headlineSmall)
        Text("${dinhDangNgay(don.ngayTao)} • ${tenTrangThaiDon(don.trangThai)}")
        Card {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Tổng tiền hàng: ${dinhDangTien(don.tongTienHang)}")
                Text("Giảm giá: ${dinhDangTien(don.giamGia)}")
                Text("Thanh toán: ${dinhDangTien(don.tongThanhToan)}", style = MaterialTheme.typography.titleMedium)
                Text("Đã thu: ${dinhDangTien(don.daThu)} • Còn nợ: ${dinhDangTien(don.conNo)}", color = MaterialTheme.colorScheme.primary)
            }
        }
        Text("Mặt hàng (giá snapshot):", style = MaterialTheme.typography.titleMedium)
        vm.chiTiet.forEach { ct ->
            Text("• ${ct.tenHienThi} × ${ct.soLuong.toInt()} = ${dinhDangTien(ct.thanhTien)}")
        }
        Text("Thu tiền:", style = MaterialTheme.typography.titleMedium)
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
            oTien.toLongOrNull()?.let { if (it > 0) vm.thuTien(it, phuongThuc) }
            oTien = ""
        }, modifier = Modifier.fillMaxWidth()) { Text("Xác nhận thu") }
        if (vm.lichSuThu.isNotEmpty()) {
            Text("Lịch sử thu:", style = MaterialTheme.typography.titleMedium)
            vm.lichSuThu.forEach { tt ->
                Text("• ${dinhDangNgay(tt.ngay)}: ${dinhDangTien(tt.soTien)} (${if (tt.phuongThuc == "CK") "CK" else "Tiền mặt"})")
            }
        }
        Text("Trạng thái:", style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { vm.doiTrangThai("DANG_LAM") }) { Text("Đang làm") }
            OutlinedButton(onClick = { vm.doiTrangThai("HOAN_THANH") }) { Text("Hoàn thành") }
            OutlinedButton(onClick = { vm.doiTrangThai("HUY") }) { Text("Hủy") }
        }
    }
}
