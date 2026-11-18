package vn.rinso.qldvyt.ui.dichVu

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
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.baoCao.tenNhomSo
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong

val DS_NHOM = listOf("TIEM_TRUYEN", "DI_XE_TRONG", "THU_TUC", "CHAM_SOC", "VAT_LY", "KHAC")

/** Màn Dịch vụ & Gói kiểu MISA: lọc nhóm sổ + tìm + thêm. */
@Composable
fun ManHinhDichVu(vm: DichVuViewModel = hiltViewModel()) {
    val dsDv by vm.dsDichVu.collectAsState()
    val dsGoi by vm.dsGoi.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var tk by remember { mutableStateOf("") }
    var nhomLoc by remember { mutableStateOf<String?>(null) }
    var hienForm by remember { mutableStateOf(false) }
    var oTen by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }
    var oDv by remember { mutableStateOf("lần") }

    val dsDvLoc = dsDv.filter {
        (nhomLoc == null || it.nhom == nhomLoc) &&
            (tk.isBlank() || it.ten.contains(tk.trim(), true) || it.maDv.contains(tk.trim(), true))
    }
    val dsGoiLoc = dsGoi.filter { tk.isBlank() || it.ten.contains(tk.trim(), true) || it.maGoi.contains(tk.trim(), true) }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { hienForm = !hienForm }) { Icon(Icons.Filled.Add, contentDescription = "Thêm") }
    }) { dem ->
        Column(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                if (tab == 0) "Dịch vụ (${dsDv.size})" else "Gói dịch vụ (${dsGoi.size})",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            )
            if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { tab = 0 }, label = { Text((if (tab == 0) "✓ " else "") + "Dịch vụ") })
                AssistChip(onClick = { tab = 1 }, label = { Text((if (tab == 1) "✓ " else "") + "Gói") })
            }
            OTimKiem(tk, { tk = it }, "Tìm theo tên / mã...")
            if (tab == 0) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { nhomLoc = null }, label = { Text((if (nhomLoc == null) "✓ " else "") + "Tất cả") })
                    DS_NHOM.take(3).forEach { n ->
                        AssistChip(onClick = { nhomLoc = n }, label = { Text((if (nhomLoc == n) "✓ " else "") + tenNhomSo(n).substringBefore(" ")) })
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DS_NHOM.drop(3).forEach { n ->
                        AssistChip(onClick = { nhomLoc = n }, label = { Text((if (nhomLoc == n) "✓ " else "") + tenNhomSo(n).substringBefore(" ")) })
                    }
                }
            }
            if (hienForm) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(if (tab == 0) "Thêm dịch vụ" else "Thêm gói", fontWeight = FontWeight.Bold)
                        OutlinedTextField(oTen, { oTen = it }, label = { Text(if (tab == 0) "Tên dịch vụ (*)" else "Tên gói (*)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(oGia, { oGia = it.filter(Char::isDigit) }, label = { Text("Giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        if (tab == 0) OutlinedTextField(oDv, { oDv = it }, label = { Text("Đơn vị (lần/ca/buổi...)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(onClick = {
                            val gia = oGia.toLongOrNull() ?: 0L
                            if (tab == 0) vm.themDichVu(oTen, gia, oDv.ifBlank { "lần" }, nhomLoc ?: "KHAC")
                            else vm.themGoi(oTen, gia)
                            oTen = ""; oGia = ""; hienForm = false
                        }, modifier = Modifier.fillMaxWidth()) { Text("Lưu") }
                        Text("Giá catalog chỉ là gợi ý — khi bán sẽ snapshot vào đơn, đổi giá không ảnh hưởng đơn cũ.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (tab == 0) {
                if (dsDvLoc.isEmpty()) OTrong("Không có dịch vụ", "Đổi từ khóa hoặc thêm mới.")
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsDvLoc, key = { it.id }) { dv ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                            Column(Modifier.padding(12.dp).fillMaxWidth()) {
                                Text(dv.ten, fontWeight = FontWeight.Bold)
                                Text("${dv.maDv} • ${tenNhomSo(dv.nhom)}", style = MaterialTheme.typography.bodySmall)
                                Text("${dinhDangTien(dv.giaNiemYet)} / ${dv.donVi}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                if (dv.moTa.isNotEmpty()) Text(dv.moTa, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            } else {
                if (dsGoiLoc.isEmpty()) OTrong("Chưa có gói", "Bấm + để thêm gói mới.")
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsGoiLoc, key = { it.id }) { goi ->
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                            Column(Modifier.padding(12.dp).fillMaxWidth()) {
                                Text(goi.ten + " (GÓI)", fontWeight = FontWeight.Bold)
                                Text(goi.maGoi, style = MaterialTheme.typography.bodySmall)
                                Text(dinhDangTien(goi.giaGoi), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                if (goi.moTa.isNotEmpty()) Text(goi.moTa, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
