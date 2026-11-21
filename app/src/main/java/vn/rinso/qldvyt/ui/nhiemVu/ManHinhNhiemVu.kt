package vn.rinso.qldvyt.ui.nhiemVu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManHinhNhiemVu(vm: NhiemVuViewModel = hiltViewModel()) {
    val dsViec by vm.dsViec.collectAsState()
    val dsNv by vm.dsNv.collectAsState()
    val tyLeMacDinh by vm.tyLeMacDinh.collectAsState()
    val tb by vm.thongBao.collectAsState()
    val dsDon by vm.dsDon.collectAsState()
    val dsKh by vm.dsKhach.collectAsState()
    val cacheCt by vm.chiTietTheoDon.collectAsState()
    var loc by remember { mutableStateOf<String?>(null) }
    var tk by remember { mutableStateOf("") }
    var hienForm by remember { mutableStateOf(false) }
    var cheDo by remember { mutableStateOf("DON") }
    var tkDon by remember { mutableStateOf("") }
    var donChon by remember { mutableStateOf<String?>(null) }
    var nvChon by remember { mutableStateOf<String?>(null) }
    var oTen by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }
    var loaiChon by remember { mutableStateOf("TIEM_TRUYEN") }
    var suaId by remember { mutableStateOf<String?>(null) }
    var sTen by remember { mutableStateOf("") }
    var sGia by remember { mutableStateOf("") }

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
        // LazyColumn gốc để form dài không bao giờ làm kẹt/cứng màn hình
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(dem).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item {
                Text("Nhiệm vụ (${dsViec.size}) - NV hưởng ${(tyLeMacDinh * 100).toInt()}%", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            }
            item {
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
            }
            if (tb.isNotEmpty()) {
                item { Card { Text(tb, Modifier.padding(12.dp)) } }
            }
            item {
                OTimKiem(tk, { tk = it }, "Tìm theo tên việc / mã việc")
            }
            if (hienForm) {
                item {
                    val mapKh = dsKh.associateBy { it.id }
                    val donCanLam = dsDon.filter {
                        (it.trangThai == "CHO_XU_LY" || it.trangThai == "DANG_LAM") &&
                            (tkDon.isBlank() || it.maDh.contains(tkDon.trim(), true) ||
                                (mapKh[it.khachHangId]?.hoTen?.contains(tkDon.trim(), true) == true))
                    }.take(10)
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Giao việc mới", fontWeight = FontWeight.Bold)
                                OutlinedButton(onClick = { hienForm = false }) { Text("Đóng") }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                AssistChip(onClick = { cheDo = "DON" }, label = { Text((if (cheDo == "DON") "✓ " else "") + "Theo đơn hàng") })
                                AssistChip(onClick = { cheDo = "TUDO" }, label = { Text((if (cheDo == "TUDO") "✓ " else "") + "Việc tự do") })
                            }
                            if (dsNv.isEmpty()) Text("Chưa có NV đang làm.")
                            else {
                                Text("B1 — chọn NV: " + nvGoiY.joinToString { "${it.hoTen} (${viecDangLam[it.id] ?: 0} việc)" }, style = MaterialTheme.typography.bodySmall)
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    dsNv.take(12).forEach { nv ->
                                        AssistChip(onClick = { nvChon = nv.id }, label = { Text((if (nvChon == nv.id) "✓ " else "") + "${nv.hoTen} - ${nv.maNv} (${viecDangLam[nv.id] ?: 0})") })
                                    }
                                }
                            }
                            if (cheDo == "DON") {
                                Text("B2 — chọn đơn đang cần làm (${donCanLam.size}):", fontWeight = FontWeight.Bold)
                                OTimKiem(tkDon, { tkDon = it }, "Tìm đơn theo mã / tên KH...")
                                if (donCanLam.isEmpty()) {
                                    Text("Không còn đơn Chờ xử lý / Đang làm — hãy tạo đơn mới.", style = MaterialTheme.typography.bodySmall)
                                } else {
                                    donCanLam.forEach { don ->
                                        val kh = mapKh[don.khachHangId]
                                        Card(
                                            onClick = {
                                                donChon = if (donChon == don.id) null else don.id
                                                if (donChon == don.id) vm.taiChiTiet(don.id)
                                            },
                                            colors = CardDefaults.cardColors(
                                                containerColor = if (donChon == don.id) MaterialTheme.colorScheme.secondaryContainer
                                                else MaterialTheme.colorScheme.surfaceContainerHighest
                                            ),
                                        ) {
                                            Column(Modifier.padding(10.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                                Text((if (donChon == don.id) "✓ " else "") + "${don.maDh} — ${kh?.hoTen ?: "KH lẻ"}", fontWeight = FontWeight.SemiBold)
                                                Text("TT: ${don.trangThai} — Tiền: ${dinhDangTien(don.tongThanhToan)}", style = MaterialTheme.typography.bodySmall)
                                            }
                                        }
                                    }
                                }
                                if (donChon != null) {
                                    val cts = cacheCt[donChon]
                                    Text("B3 — chọn dòng dịch vụ để giao:", fontWeight = FontWeight.Bold)
                                    if (cts == null) {
                                        androidx.compose.runtime.LaunchedEffect(donChon) { vm.taiChiTiet(donChon!!) }
                                        Text("Đang tải chi tiết đơn...", style = MaterialTheme.typography.bodySmall)
                                    } else if (cts.isEmpty()) {
                                        Text("Đơn này chưa có dòng dịch vụ.", style = MaterialTheme.typography.bodySmall)
                                    } else {
                                        cts.forEach { ct ->
                                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                                Column(Modifier.weight(1f)) {
                                                    Text(ct.tenHienThi, fontWeight = FontWeight.SemiBold)
                                                    Text("SL ${ct.soLuong.toInt()} × ${dinhDangTien(ct.donGia)} = ${dinhDangTien(ct.thanhTien)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                                }
                                                OutlinedButton(
                                                    onClick = {
                                                        if (nvChon != null) vm.giaoViecTheoDongDon(nvChon!!, donChon!!, ct.id, tyLeMacDinh)
                                                    },
                                                    enabled = nvChon != null,
                                                ) { Text("Giao") }
                                            }
                                        }
                                        if (nvChon == null) Text("Hãy chọn NV ở B1 trước khi bấm Giao.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                    }
                                }
                            } else {
                                OutlinedTextField(oTen, { oTen = it }, label = { Text("Tên công việc (*)") }, modifier = Modifier.fillMaxWidth())
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf("TIEM_TRUYEN", "THAY_BANG", "THU_TUC", "KHAC").forEach { loai ->
                                        AssistChip(onClick = { loaiChon = loai }, label = { Text((if (loaiChon == loai) "✓ " else "") + tenLoaiViec(loai)) })
                                    }
                                }
                                OutlinedTextField(oGia, { oGia = it.filter(Char::isDigit) }, label = { Text("Tổng giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                val giaLong = oGia.toLongOrNull() ?: 0L
                                if (giaLong > 0) Text("NV sẽ hưởng: ${dinhDangTien((giaLong * tyLeMacDinh).toLong())} (${(tyLeMacDinh * 100).toInt()}%)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                if (nvChon == null) Text("Hãy chọn 1 nhân viên ở trên.", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                Button(onClick = {
                                    if (nvChon != null) {
                                        vm.giaoViec(nvChon!!, oTen, loaiChon, giaLong, tyLeMacDinh, null)
                                        oTen = ""; oGia = ""; nvChon = null; hienForm = false
                                    }
                                }, enabled = nvChon != null, modifier = Modifier.fillMaxWidth()) { Text("Giao việc tự do") }
                            }
                        }
                    }
                }
            }
            if (dsLoc.isEmpty()) {
                item {
                    OTrong("Không có nhiệm vụ", "Bấm + để giao việc mới.")
                }
            } else {
                items(dsLoc, key = { it.id }) { viec ->
                    val tenNv = mapNv[viec.nhanVienId]?.hoTen ?: "NV ${viec.nhanVienId.take(6)}"
                    val donLienQuan = dsDon.firstOrNull { it.id == viec.donHangId }
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(viec.tenCongViec, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                NhanTrangThai(viec.trangThai, tenTrangThaiViec(viec.trangThai), mauTrangThaiViec(viec.trangThai))
                            }
                            Text("$tenNv - ${viec.maViec} - ${tenLoaiViec(viec.loai)} - Giá: ${dinhDangTien(viec.tongGia)}", style = MaterialTheme.typography.bodySmall)
                            if (donLienQuan != null) {
                                Text("Theo đơn: ${donLienQuan.maDh} (${donLienQuan.trangThai})", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                            }
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
                                OutlinedButton(onClick = {
                                    if (suaId == viec.id) suaId = null
                                    else { suaId = viec.id; sTen = viec.tenCongViec; sGia = viec.tongGia.toString() }
                                }) { Text(if (suaId == viec.id) "Đóng" else "Sửa") }
                                if (viec.trangThai != "HOANTHANH") OutlinedButton(onClick = { vm.xoaViec(viec.id) }) { Text("Xóa") }
                            }
                            if (suaId == viec.id) {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedTextField(sTen, { sTen = it }, label = { Text("Tên công việc (*)") }, modifier = Modifier.fillMaxWidth())
                                        OutlinedTextField(sGia, { sGia = it.filter(Char::isDigit) }, label = { Text("Tổng giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                        Text("Đổi NV phụ trách:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                        LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                            items(dsNv.take(12), key = { it.id }) { nv ->
                                                AssistChip(onClick = { vm.suaViec(viec.id, sTen.ifBlank { viec.tenCongViec }, nv.id, sGia.toLongOrNull() ?: viec.tongGia, viec.tyLeHuong); suaId = null }, label = { Text(nv.hoTen) })
                                            }
                                        }
                                        Button(onClick = {
                                            vm.suaViec(viec.id, sTen.ifBlank { viec.tenCongViec }, viec.nhanVienId, sGia.toLongOrNull() ?: viec.tongGia, viec.tyLeHuong)
                                            suaId = null
                                        }, modifier = Modifier.fillMaxWidth()) { Text("Lưu (tiền NV tự tính lại)") }
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
