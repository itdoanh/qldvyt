package vn.rinso.qldvyt.ui.kho

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong

/** Màn Kho kiểu MISA: cảnh báo + tìm + nhập kho + thêm vật tư. */
@Composable
fun ManHinhKho(vm: KhoViewModel = hiltViewModel()) {
    val ds by vm.dsVatTu.collectAsState()
    val sapHet by vm.dsSapHet.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var tk by remember { mutableStateOf("") }
    var hienNhap by remember { mutableStateOf(false) }
    var oTen by remember { mutableStateOf("") }
    var oSl by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }
    var oNcc by remember { mutableStateOf("") }
    var vtChon by remember { mutableStateOf<String?>(null) }

    val dsLoc = ds.filter { tk.isBlank() || it.ten.contains(tk.trim(), true) || it.maVt.contains(tk.trim(), true) }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { hienNhap = !hienNhap }) { Icon(Icons.Filled.Add, contentDescription = "Nhập kho") }
    }) { dem ->
        Column(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Kho vật tư (${ds.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
            if (sapHet.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("⚠ Sắp hết: ${sapHet.size} mặt hàng", fontWeight = FontWeight.Bold)
                        Text(sapHet.take(4).joinToString { "${it.ten} (còn ${it.tonKho.toInt()})" }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            OTimKiem(tk, { tk = it }, "Tìm vật tư theo tên / mã VT")
            if (hienNhap) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nhập kho (giá vốn bình quân tự cập nhật)", fontWeight = FontWeight.Bold)
                        Text("Vật tư: ${ds.firstOrNull { it.id == vtChon }?.ten ?: "chọn ở danh sách dưới"}")
                        OutlinedTextField(oSl, { oSl = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("Số lượng nhập") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(oGia, { oGia = it.filter(Char::isDigit) }, label = { Text("Đơn giá nhập (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(oNcc, { oNcc = it }, label = { Text("Nhà cung cấp") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(onClick = {
                            if (vtChon != null) {
                                vm.nhapKho(vtChon!!, oSl.toDoubleOrNull() ?: 0.0, oGia.toLongOrNull() ?: 0L)
                                oSl = ""; oGia = ""; oNcc = ""; hienNhap = false
                            }
                        }, enabled = vtChon != null, modifier = Modifier.fillMaxWidth()) { Text("Xác nhận nhập kho") }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(oTen, { oTen = it }, label = { Text("Tên vật tư mới") }, modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { vm.themVatTu(oTen, "cái", 5.0); oTen = "" }) { Text("+ Thêm") }
                        }
                    }
                }
            }
            if (dsLoc.isEmpty()) {
                OTrong("Không tìm thấy vật tư", "Thử từ khóa khác hoặc thêm vật tư mới.", Icons.Filled.Inventory2)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsLoc, key = { it.id }) { vt ->
                        val het = vt.tonKho < vt.tonToiThieu
                        Card(
                            onClick = { vtChon = vt.id; hienNhap = true },
                            colors = if (het) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text((if (vtChon == vt.id) "✓ " else "") + vt.ten, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (het) Text("SẮP HẾT", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                }
                                Text("${vt.maVt} • Tồn: ${vt.tonKho.toInt()} ${vt.donVi} • Tối thiểu: ${vt.tonToiThieu.toInt()}", style = MaterialTheme.typography.bodySmall)
                                Text("Vốn TB: ${dinhDangTien(vt.giaNhapTB)} • Trị giá: ${dinhDangTien((vt.tonKho * vt.giaNhapTB).toLong())}", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
