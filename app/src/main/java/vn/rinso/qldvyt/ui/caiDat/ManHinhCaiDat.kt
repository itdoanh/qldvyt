package vn.rinso.qldvyt.ui.caiDat

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
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/** Màn Cài đặt (tiếng Việt): công chuẩn, tỷ lệ NV, cảnh báo nợ, tên công ty + chế độ Sáng/Tối. GD12. */
@Composable
fun ManHinhCaiDat(vm: CaiDatViewModel = hiltViewModel()) {
    val congChuan by vm.congChuanThang.collectAsState()
    val tyLe by vm.tyLeNhiemVu.collectAsState()
    val ngayNo by vm.soNgayCanhBaoNo.collectAsState()
    val tenCty by vm.tenCongTy.collectAsState()
    val cheDo by vm.cheDoGiaoDien.collectAsState()

    var oCong by remember(congChuan) { mutableStateOf(congChuan.toString()) }
    var oTyLe by remember(tyLe) { mutableStateOf((tyLe * 100).toString()) }
    var oNgay by remember(ngayNo) { mutableStateOf(ngayNo.toString()) }
    var oTen by remember(tenCty) { mutableStateOf(tenCty) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SnackbarHost(hostState = snack)
        Text("Cài đặt hệ thống", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Đổi là áp dụng ngay toàn app, không cần khởi động lại.", style = MaterialTheme.typography.bodySmall)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chế độ giao diện", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SANG" to "☀ Sáng", "TOI" to "🌙 Tối", "HE_THONG" to "📱 Theo máy").forEach { (ma, nhan) ->
                        FilterChip(
                            selected = cheDo == ma,
                            onClick = {
                                vm.luuCheDoGiaoDien(ma)
                                scope.launch { snack.showSnackbar("Đã chuyển giao diện: $nhan") }
                            },
                            label = { Text(nhan) },
                        )
                    }
                }
                Text(
                    when (cheDo) {
                        "SANG" -> "Đang dùng: Sáng — nền trắng xanh, chữ rõ."
                        "TOI" -> "Đang dùng: Tối — nền đen xanh, dịu mắt ban đêm."
                        else -> "Đang dùng: Theo hệ thống — máy sáng thì app sáng."
                    },
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Thông tin công ty & tính lương", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = oTen, onValueChange = { oTen = it }, label = { Text("Tên công ty") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = oCong, onValueChange = { oCong = it }, label = { Text("Công chuẩn 1 tháng (VD: 26)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = oTyLe, onValueChange = { oTyLe = it }, label = { Text("Tỷ lệ NV hưởng % (VD: 30)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = oNgay, onValueChange = { oNgay = it }, label = { Text("Số ngày cảnh báo công nợ (VD: 7)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = {
                    vm.luuTenCongTy(oTen)
                    oCong.toDoubleOrNull()?.let { vm.luuCongChuan(it) }
                    oTyLe.toDoubleOrNull()?.let { vm.luuTyLe(it / 100.0) }
                    oNgay.toIntOrNull()?.let { vm.luuNgayCanhBao(it) }
                    scope.launch { snack.showSnackbar("Đã lưu cài đặt") }
                }, modifier = Modifier.fillMaxWidth()) { Text("Lưu cài đặt") }
            }
        }
        Text("Thay đổi có hiệu lực ngay cho các bảng lương và cảnh báo mới.", style = MaterialTheme.typography.bodySmall)
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nhà phát triển: Rinso (Thuộc Rin)", fontWeight = FontWeight.SemiBold)
                Text("Lập trình viên: Đoàn Văn Doanh (itdoanh)")
                Text("App offline 100% — dữ liệu lưu trên máy, backup bằng file.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
