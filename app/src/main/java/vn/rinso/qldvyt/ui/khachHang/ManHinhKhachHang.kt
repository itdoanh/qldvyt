package vn.rinso.qldvyt.ui.khachHang

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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

/** Màn Khách hàng: tìm theo tên/SĐT + thêm mới + chọn để tạo đơn. */
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

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Khách hàng", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = tk, onValueChange = { vm.tuKhoa.value = it },
            label = { Text("Tìm theo tên hoặc SĐT") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
        )
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        OutlinedButton(onClick = { hienThem = !hienThem }, modifier = Modifier.fillMaxWidth()) {
            Text(if (hienThem) "Đóng form thêm" else "+ Thêm khách hàng")
        }
        if (hienThem) {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(oTen, { oTen = it }, label = { Text("Họ tên (*)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(oSdt, { oSdt = it }, label = { Text("SĐT") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    OutlinedTextField(oDiaChi, { oDiaChi = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())
                    Button(onClick = {
                        vm.them(oTen, oSdt, oDiaChi)
                        oTen = ""; oSdt = ""; oDiaChi = ""
                    }, modifier = Modifier.fillMaxWidth()) { Text("Lưu khách hàng") }
                }
            }
        }
        if (ds.isEmpty()) {
            Card { Text("Chưa có khách hàng nào. Hãy thêm mới.", Modifier.padding(16.dp)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ds, key = { it.id }) { kh ->
                    Card(onClick = { onChon(kh.id) }) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(kh.hoTen, style = MaterialTheme.typography.titleMedium)
                            Text("SĐT: ${kh.sdt.ifEmpty { "—" }} • ${kh.maKh}")
                            if (kh.diaChi.isNotEmpty()) Text(kh.diaChi, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
        }
    }
}
