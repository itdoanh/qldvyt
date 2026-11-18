package vn.rinso.qldvyt.ui.khachHang

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
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong

/** Màn Khách hàng kiểu MISA: tìm nhanh + thêm + bấm để tạo đơn. */
@Composable
fun ManHinhKhachHang(
    vm: KhachHangViewModel = hiltViewModel(),
    onChon: (String) -> Unit = {},
) {
    val ds by vm.danhSach.collectAsState()
    val tk by vm.tuKhoa.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var hienThem by remember { mutableStateOf(false) }
    var oTen by remember { mutableStateOf("") }
    var oSdt by remember { mutableStateOf("") }
    var oDiaChi by remember { mutableStateOf("") }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { hienThem = !hienThem }) { Icon(Icons.Filled.Add, contentDescription = "Thêm KH") }
    }) { dem ->
        Column(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Khách hàng (${ds.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
            OTimKiem(tk, { vm.tuKhoa.value = it }, "Tìm theo tên hoặc SĐT...")
            if (hienThem) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Thêm khách hàng mới", fontWeight = FontWeight.Bold)
                        OutlinedTextField(oTen, { oTen = it }, label = { Text("Họ tên (*)") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(oSdt, { oSdt = it.filter(Char::isDigit) }, label = { Text("SĐT") }, modifier = Modifier.weight(1f), singleLine = true)
                        }
                        OutlinedTextField(oDiaChi, { oDiaChi = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            vm.them(oTen, oSdt, oDiaChi)
                            oTen = ""; oSdt = ""; oDiaChi = ""; hienThem = false
                        }, modifier = Modifier.fillMaxWidth()) { Text("Lưu khách hàng") }
                    }
                }
            }
            if (ds.isEmpty()) {
                OTrong("Chưa có khách hàng", "Bấm + để thêm khách mới.", Icons.Filled.Person)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(ds, key = { it.id }) { kh ->
                        Card(
                            onClick = { onChon(kh.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text(kh.hoTen, fontWeight = FontWeight.Bold)
                                Text("${kh.maKh} • SĐT: ${kh.sdt.ifEmpty { "—" }}", style = MaterialTheme.typography.bodySmall)
                                if (kh.diaChi.isNotEmpty()) Text(kh.diaChi, style = MaterialTheme.typography.bodySmall)
                                if (kh.ghiChu.isNotEmpty()) Text("📝 ${kh.ghiChu}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                OutlinedButton(onClick = { onChon(kh.id) }) { Text("Tạo đơn cho khách này") }
                            }
                        }
                    }
                }
            }
        }
    }
}
