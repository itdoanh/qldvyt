package vn.rinso.qldvyt.ui.nhanVien

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

/** Màn Nhân viên: cấp bậc + danh sách NV + chấm công nhanh hôm nay. */
@Composable
fun ManHinhNhanVien(vm: NhanVienViewModel = hiltViewModel()) {
    val dsCap by vm.dsCapBac.collectAsState()
    val dsNv by vm.dsNv.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var tab by remember { mutableStateOf(0) } // 0 = NV, 1 = cấp bậc
    var oTen by remember { mutableStateOf("") }
    var oSdt by remember { mutableStateOf("") }
    var oLuong by remember { mutableStateOf("") }
    var capChon by remember { mutableStateOf<String?>(null) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nhân viên", style = MaterialTheme.typography.headlineSmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { tab = 0 }, enabled = tab != 0, modifier = Modifier.weight(1f)) { Text("Nhân viên") }
            Button(onClick = { tab = 1 }, enabled = tab != 1, modifier = Modifier.weight(1f)) { Text("Cấp bậc") }
        }
        if (tab == 1) {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(oTen, { oTen = it }, label = { Text("Tên cấp bậc mới") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(oLuong, { oLuong = it.filter(Char::isDigit) }, label = { Text("Lương cơ bản (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = {
                        vm.themCapBac(oTen, oLuong.toLongOrNull() ?: 0L, 0L); oTen = ""; oLuong = ""
                    }, modifier = Modifier.fillMaxWidth()) { Text("Thêm cấp bậc") }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dsCap, key = { it.id }) { cap ->
                    Card {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(cap.ten, style = MaterialTheme.typography.titleMedium)
                            Text("Lương chuẩn: ${dinhDangTien(cap.luongCoBan)} • Chuyên cần: ${dinhDangTien(cap.chuyenCanMuc)}")
                        }
                    }
                }
                if (dsCap.isEmpty()) item { Text("Chưa có cấp bậc. Seed P1 đã tạo 3 cấp mẫu — build mới sẽ thấy.") }
            }
        } else {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(oTen, { oTen = it }, label = { Text("Họ tên NV mới") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(oSdt, { oSdt = it }, label = { Text("SĐT") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    if (dsCap.isEmpty()) Text("Hãy thêm cấp bậc trước (tab Cấp bậc).")
                    else {
                        Text("Cấp bậc: ${dsCap.firstOrNull { it.id == capChon }?.ten ?: "chưa chọn"}")
                        dsCap.forEach { cap ->
                            OutlinedButton(onClick = { capChon = cap.id }) {
                                Text(if (capChon == cap.id) "✓ ${cap.ten}" else cap.ten)
                            }
                        }
                    }
                    Button(onClick = {
                        if (capChon != null) { vm.themNhanVien(oTen, oSdt, capChon!!); oTen = ""; oSdt = "" }
                    }, enabled = capChon != null, modifier = Modifier.fillMaxWidth()) { Text("Thêm nhân viên") }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dsNv, key = { it.id }) { nv ->
                    Card {
                        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${nv.hoTen} • ${nv.maNv}", style = MaterialTheme.typography.titleMedium)
                            Text("SĐT: ${nv.sdt.ifEmpty { "—" }}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { vm.chamNhanh(nv.id, 1.0, false) }) { Text("Chấm 1 công") }
                                OutlinedButton(onClick = { vm.chamNhanh(nv.id, 0.5, false) }) { Text("0.5 công") }
                            }
                        }
                    }
                }
                if (dsNv.isEmpty()) item { Text("Chưa có nhân viên đang làm.") }
            }
        }
    }
}
