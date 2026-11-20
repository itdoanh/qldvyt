package vn.rinso.qldvyt.ui.nhiemVu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
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
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.HangTienTrinh
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong
import vn.rinso.qldvyt.ui.common.mauTrangThaiViec

fun tenLoaiViec(loai: String): String = when (loai) {
    "TIEM_TRUYEN" -> "Tiêm truyền"
    "THAY_BANG" -> "Thay băng"
    "THU_TUC" -> "Làm thủ tục"
    else -> "Việc khác"
}

fun tenTrangThaiViec(tt: String): String = when (tt) {
    "DAGIAO" -> "Đã giao"
    "DANGLAM" -> "Đang làm"
    "HOANTHANH" -> "Hoàn thành"
    "HUY" -> "Đã hủy"
    else -> tt
}

/** Màn Nhiệm vụ v3: Kanban mini + tên NV thật + gợi ý NV rảnh + tiền 30%. */
@Composable
fun ManHinhNhiemVu(vm: NhiemVuViewModel = hiltViewModel()) {
    val dsViec by vm.dsViec.collectAsState()
    val dsNv by vm.dsNv.collectAsState()
    val tyLeMacDinh by vm.tyLeMacDinh.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var loc by remember { mutableStateOf<String?>(null) }
    var tk by remember { mutableStateOf("") }
    var hienForm by remember { mutableStateOf(false) }
    var nvChon by remember { mutableStateOf<String?>(null) }
    var oTen by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }
    var loaiChon by remember { mutableStateOf("TIEM_TRUYEN") }

    val mapNv = dsNv.associateBy { it.id }
    // Gợi ý NV rảnh: đếm việc chưa xong của từng NV
    val viecDangLam = dsViec.filter { it.trangThai == "DAGIAO" || it.trangThai == "DANGLAM" }
        .groupingBy { it.nhanVienId }.eachCount()
    val nvGoiY = dsNv.sortedBy { viecDangLam[it.id] ?: 0 }.take(3)

    val dsLoc = dsViec.filter {
        (loc == null || it.trangThai == loc) &&
            (tk.isBlank() || it.tenCongViec.contains(tk.trim(), true) || it.maViec.contains(tk.trim(), true))
    }
    val xong = dsViec.count { it.trangThai == "HOANTHANH" }
    val tyLeXong = if (dsViec.isNotEmpty()) xong.toFloat() / dsViec.size else 0f
    val tongTienNv = dsViec.filter { it.trangThai == "HOANTHANH" }.sumOf { it.tienNvHuong }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { hienForm = !hienForm }) { Icon(Icons.Filled.Add, contentDescription = "Giao việc") }
    }) { dem ->
        Column(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Nhiệm vụ (${dsViec.size}) - NV hưởng ${(tyLeMacDinh * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    HangTienTrinh("Hoàn thành $xong/${dsViec.size} - Tiền NV ${dinhDangTien(tongTienNv)}", tyLeXong, MaterialTheme.colorScheme.primary)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("DAGIAO" to "Đã giao", "DANGLAM" to "Đang làm", "HOANTHANH" to "Xong", "HUY" to "Hủy").forEach { (ma, ten) ->
                            item {
                                val sl = dsViec.count { it.trangThai == ma }
                                AssistChip(onClick = { loc = if (loc == ma) null else ma }, label = { Text("$ten ($sl)") })
                            }
                        }
                    }
                }
            }
            if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
            OTimKiem(tk, { tk = it }, "Tìm theo tên việc / mã việc")
            if (hienForm) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Giao việc mới", fontWeight = FontWeight.Bold)
                        if (dsNv.isEmpty()) Text("Chưa có NV đang làm.")
                        else {
                            Text("Gợi ý NV rảnh: " + nvGoiY.joinToString { "${it.hoTen} (${viecDangLam[it.id] ?: 0} việc)" })
                            dsNv.take(12).forEach { nv ->
                                AssistChip(onClick = { nvChon = nv.id }, label = { Text((if (nvChon == nv.id) "✓ " else "") + "${nv.hoTen} - ${nv.maNv} (${viecDangLam[nv.id] ?: 0})") })
                            }
                        }
                        OutlinedTextField(oTen, { oTen = it }, label = { Text("Tên công việc (*)") }, modifier = Modifier.fillMaxWidth())
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("TIEM_TRUYEN", "THAY_BANG", "THU_TUC", "KHAC").forEach { loai ->
                                AssistChip(onClick = { loaiChon = loai }, label = { Text((if (loaiChon == loai) "✓ " else "") + tenLoaiViec(loai)) })
                            }
                        }
                        OutlinedTextField(oGia, { oGia = it.filter(Char::isDigit) }, label = { Text("Tổng giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        val giaLong = oGia.toLongOrNull() ?: 0L
                        if (giaLong > 0) Text("NV sẽ hưởng: ${dinhDangTien((giaLong * tyLeMacDinh).toLong())} (${(tyLeMacDinh * 100).toInt()}%)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        Button(onClick = {
                            if (nvChon != null) {
                                vm.giaoViec(nvChon!!, oTen, loaiChon, giaLong, tyLeMacDinh)
                                oTen = ""; oGia = ""; hienForm = false
                            }
                        }, enabled = nvChon != null, modifier = Modifier.fillMaxWidth()) { Text("Giao việc") }
                    }
                }
            }
            if (dsLoc.isEmpty()) {
                OTrong("Không có nhiệm vụ", "Bấm + để giao việc mới.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsLoc, key = { it.id }) { viec ->
                        val tenNv = mapNv[viec.nhanVienId]?.hoTen ?: "NV ${viec.nhanVienId.take(6)}"
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                            Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(viec.tenCongViec, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    NhanTrangThai(viec.trangThai, tenTrangThaiViec(viec.trangThai), mauTrangThaiViec(viec.trangThai))
                                }
                                Text("$tenNv - ${viec.maViec} - ${tenLoaiViec(viec.loai)} - Giá: ${dinhDangTien(viec.tongGia)}", style = MaterialTheme.typography.bodySmall)
                                if (viec.trangThai == "HOANTHANH") {
                                    Text("NV hưởng: ${dinhDangTien(viec.tienNvHuong)} (${(viec.tyLeHuong * 100).toInt()}%)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                } else if (viec.tongGia > 0) {
                                    Text("Dự kiến NV hưởng: ${dinhDangTien((viec.tongGia * viec.tyLeHuong).toLong())}", style = MaterialTheme.typography.bodySmall)
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    if (viec.trangThai == "DAGIAO") OutlinedButton(onClick = { vm.nhanViec(viec.id) }) { Text("Nhận việc") }
                                    if (viec.trangThai == "DAGIAO" || viec.trangThai == "DANGLAM") {
                                        Button(onClick = { vm.hoanThanh(viec.id) }) { Text("Hoàn thành") }
                                        OutlinedButton(onClick = { vm.huy(viec.id) }) { Text("Hủy") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
