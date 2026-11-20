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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.baoCao.tenNhomSo
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong
import vn.rinso.qldvyt.ui.theme.MauNhomSo

val DS_NHOM = listOf("TIEM_TRUYEN", "DI_XE_TRONG", "THU_TUC", "CHAM_SOC", "VAT_LY", "KHAC")

/** Màn Dịch vụ & Gói v3: lọc nhóm + đã bán (số đơn + doanh thu) + công tắc VAT từng món. */
@Composable
fun ManHinhDichVu(vm: DichVuViewModel = hiltViewModel()) {
    val dsDv by vm.dsDichVu.collectAsState()
    val dsGoi by vm.dsGoi.collectAsState()
    val dsCt by vm.dsChiTiet.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var tk by remember { mutableStateOf("") }
    var nhomLoc by remember { mutableStateOf<String?>(null) }
    var hienForm by remember { mutableStateOf(false) }
    var oTen by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }
    var oDv by remember { mutableStateOf("lần") }
    var oChiuThue by remember { mutableStateOf(true) }

    // Thống kê đã bán từ chi tiết đơn (snapshot)
    val banTheoDv = dsCt.filter { it.dichVuId != null }.groupBy { it.dichVuId }
    val banTheoGoi = dsCt.filter { it.goiId != null }.groupBy { it.goiId }

    val dsDvLoc = dsDv.filter {
        (nhomLoc == null || it.nhom == nhomLoc) &&
            (tk.isBlank() || it.ten.contains(tk.trim(), true) || it.maDv.contains(tk.trim(), true))
    }.sortedByDescending { (banTheoDv[it.id]?.sumOf { ct -> ct.thanhTien } ?: 0L) }
    val dsGoiLoc = dsGoi.filter { tk.isBlank() || it.ten.contains(tk.trim(), true) || it.maGoi.contains(tk.trim(), true) }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { hienForm = !hienForm }) { Icon(Icons.Filled.Add, contentDescription = "Thêm") }
    }) { dem ->
        Column(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                if (tab == 0) "Dịch vụ (${dsDv.size})" else "Gói dịch vụ (${dsGoi.size})",
                style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold,
            )
            Text("Xếp theo doanh thu đã bán - giá catalog chỉ là gợi ý, bán sẽ snapshot vào đơn. Bật/tắt VAT từng món, đơn mới áp theo.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
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
                        if (tab == 0) {
                            OutlinedTextField(oDv, { oDv = it }, label = { Text("Đơn vị (lần/ca/buổi...)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Column(Modifier.weight(1f)) {
                                    Text("Chịu thuế VAT", fontWeight = FontWeight.SemiBold)
                                    Text("Tắt nếu dịch vụ được miễn VAT (VD: khám chữa bệnh)", style = MaterialTheme.typography.bodySmall)
                                }
                                Switch(checked = oChiuThue, onCheckedChange = { oChiuThue = it })
                            }
                        }
                        Button(onClick = {
                            val gia = oGia.toLongOrNull() ?: 0L
                            if (tab == 0) vm.themDichVu(oTen, gia, oDv.ifBlank { "lần" }, nhomLoc ?: "KHAC", oChiuThue)
                            else vm.themGoi(oTen, gia)
                            oTen = ""; oGia = ""; hienForm = false; oChiuThue = true
                        }, modifier = Modifier.fillMaxWidth()) { Text("Lưu") }
                        Text("Giá catalog chỉ là gợi ý - khi bán sẽ snapshot vào đơn, đổi giá không ảnh hưởng đơn cũ.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            if (tab == 0) {
                if (dsDvLoc.isEmpty()) OTrong("Không có dịch vụ", "Đổi từ khóa hoặc thêm mới.")
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsDvLoc, key = { it.id }) { dv ->
                        val cts = banTheoDv[dv.id] ?: emptyList()
                        val dt = cts.sumOf { it.thanhTien }
                        val sl = cts.size
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                            Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(dv.ten, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(dinhDangTien(dv.giaNiemYet), color = MauNhomSo.cua(dv.nhom), fontWeight = FontWeight.Bold)
                                }
                                Text("${dv.maDv} - ${tenNhomSo(dv.nhom)} - /${dv.donVi}", style = MaterialTheme.typography.bodySmall)
                                Text("Đã bán: $sl lần - Doanh thu: ${dinhDangTien(dt)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                if (dv.moTa.isNotEmpty()) Text(dv.moTa, style = MaterialTheme.typography.bodySmall)
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (dv.chiuThue) "✓ Chịu thuế VAT" else "○ Miễn VAT", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = if (dv.chiuThue) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                                    Switch(checked = dv.chiuThue, onCheckedChange = { vm.doiChiuThue(dv, it) })
                                }
                            }
                        }
                    }
                }
            } else {
                if (dsGoiLoc.isEmpty()) OTrong("Chưa có gói", "Bấm + để thêm gói mới.")
                else LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsGoiLoc, key = { it.id }) { goi ->
                        val cts = banTheoGoi[goi.id] ?: emptyList()
                        val dt = cts.sumOf { it.thanhTien }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                            Column(Modifier.padding(12.dp).fillMaxWidth()) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(goi.ten + " (GÓI)", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    Text(dinhDangTien(goi.giaGoi), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                }
                                Text("${goi.maGoi} - Đã bán: ${cts.size} lần - DT: ${dinhDangTien(dt)}", style = MaterialTheme.typography.bodySmall)
                                if (goi.moTa.isNotEmpty()) Text(goi.moTa, style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
