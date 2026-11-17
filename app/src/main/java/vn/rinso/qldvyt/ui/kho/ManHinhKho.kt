package vn.rinso.qldvyt.ui.kho

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien

/** Màn Kho: tồn kho + cảnh báo tồn thấp (đỏ) + nhập kho + thêm vật tư. */
@Composable
fun ManHinhKho(vm: KhoViewModel = hiltViewModel()) {
    val ds by vm.dsVatTu.collectAsState()
    val sapHet by vm.dsSapHet.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var oTen by remember { mutableStateOf("") }
    var oSl by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }
    var vtNhapChon by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Kho vật tư", style = MaterialTheme.typography.headlineSmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        if (sapHet.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text("⚠ Sắp hết: ${sapHet.joinToString { "${it.ten} (còn ${it.tonKho.toInt()})" }}", Modifier.padding(12.dp))
            }
        }
        Card {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Nhập kho (giá vốn bình quân tự cập nhật)", style = MaterialTheme.typography.titleMedium)
                Text("Vật tư: ${ds.firstOrNull { it.id == vtNhapChon }?.ten ?: "chọn ở danh sách dưới"}")
                OutlinedTextField(oSl, { oSl = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Số lượng nhập") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(oGia, { oGia = it.filter(Char::isDigit) }, label = { Text("Đơn giá nhập (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = {
                    if (vtNhapChon != null) {
                        vm.nhapKho(vtNhapChon!!, oSl.toDoubleOrNull() ?: 0.0, oGia.toLongOrNull() ?: 0L)
                        oSl = ""; oGia = ""
                    }
                }, enabled = vtNhapChon != null, modifier = Modifier.fillMaxWidth()) { Text("Xác nhận nhập kho") }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(oTen, { oTen = it }, label = { Text("Tên vật tư mới") }, modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { vm.themVatTu(oTen, "cái", 5.0); oTen = "" }) { Text("+ Thêm") }
                }
            }
        }
        if (ds.isEmpty()) {
            Card { Text("Chưa có vật tư. Seed P1 đã tạo 6 vật tư mẫu — build mới sẽ thấy.", Modifier.padding(16.dp)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ds, key = { it.id }) { vt ->
                    val het = vt.tonKho < vt.tonToiThieu
                    Card(
                        onClick = { vtNhapChon = vt.id },
                        colors = if (het) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer) else CardDefaults.cardColors(),
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text((if (vtNhapChon == vt.id) "✓ " else "") + vt.ten, style = MaterialTheme.typography.titleMedium)
                            Text("Tồn: ${vt.tonKho.toInt()} ${vt.donVi} • Vốn TB: ${dinhDangTien(vt.giaNhapTB)} • Tối thiểu: ${vt.tonToiThieu.toInt()}")
                        }
                    }
                }
            }
        }
    }
}
