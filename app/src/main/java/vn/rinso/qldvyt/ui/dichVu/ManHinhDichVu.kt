package vn.rinso.qldvyt.ui.dichVu

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

/** Màn Dịch vụ & Gói: catalog giá niêm yết (đổi giá không ảnh hưởng đơn cũ). */
@Composable
fun ManHinhDichVu(vm: DichVuViewModel = hiltViewModel()) {
    val dsDv by vm.dsDichVu.collectAsState()
    val dsGoi by vm.dsGoi.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var tab by remember { mutableStateOf(0) } // 0 = dịch vụ, 1 = gói
    var oTen by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Dịch vụ & Gói", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { tab = 0 }, enabled = tab != 0, modifier = Modifier.weight(1f)) { Text("Dịch vụ") }
            Button(onClick = { tab = 1 }, enabled = tab != 1, modifier = Modifier.weight(1f)) { Text("Gói dịch vụ") }
        }
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        Card {
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(oTen, { oTen = it }, label = { Text(if (tab == 0) "Tên dịch vụ mới" else "Tên gói mới") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(oGia, { oGia = it.filter(Char::isDigit) }, label = { Text("Giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = {
                    val gia = oGia.toLongOrNull() ?: 0L
                    if (tab == 0) vm.themDichVu(oTen, gia, "lần") else vm.themGoi(oTen, gia)
                    oTen = ""; oGia = ""
                }, modifier = Modifier.fillMaxWidth()) { Text("Thêm") }
            }
        }
        if (tab == 0) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dsDv, key = { it.id }) { dv ->
                    Card {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(dv.ten, style = MaterialTheme.typography.titleMedium)
                            Text("${dinhDangTien(dv.giaNiemYet)} / ${dv.donVi} • ${dv.maDv}")
                        }
                    }
                }
                if (dsDv.isEmpty()) item { Text("Chưa có dịch vụ. Seed P1 đã tạo 5 dịch vụ mẫu — build mới sẽ thấy.") }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dsGoi, key = { it.id }) { goi ->
                    Card {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(goi.ten, style = MaterialTheme.typography.titleMedium)
                            Text("${dinhDangTien(goi.giaGoi)} • ${goi.maGoi}")
                        }
                    }
                }
                if (dsGoi.isEmpty()) item { Text("Chưa có gói dịch vụ.") }
            }
        }
    }
}
