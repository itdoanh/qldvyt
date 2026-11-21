package vn.rinso.qldvyt.ui.nhanVien

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong

/** Màn Nhân viên: 1 luồng cuộn LazyColumn — form Thêm nằm chung nên không kẹt khi bàn phím mở. */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ManHinhNhanVien(vm: NhanVienViewModel = hiltViewModel()) {
    val dsCap by vm.dsCapBac.collectAsState()
    val dsNv by vm.dsNv.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var tab by remember { mutableStateOf(0) }
    var tk by remember { mutableStateOf("") }
    var hienForm by remember { mutableStateOf(false) }
    var oTen by remember { mutableStateOf("") }
    var oSdt by remember { mutableStateOf("") }
    var oDc by remember { mutableStateOf("") }
    var oLuong by remember { mutableStateOf("") }
    var capChon by remember { mutableStateOf<String?>(null) }
    var nvMoRong by remember { mutableStateOf<String?>(null) }
    var nvSua by remember { mutableStateOf<String?>(null) }
    var eTen by remember { mutableStateOf("") }
    var eSdt by remember { mutableStateOf("") }
    var eDc by remember { mutableStateOf("") }
    var eStk by remember { mutableStateOf("") }
    var eGhiChu by remember { mutableStateOf("") }
    var eCap by remember { mutableStateOf<String?>(null) }
    var eTrangThai by remember { mutableStateOf("DANGLAM") }
    var oTienTP by remember { mutableStateOf("") }
    var oLyDoTP by remember { mutableStateOf("") }
    val ctx = LocalContext.current

    val dsLoc = dsNv.filter { tk.isBlank() || it.hoTen.contains(tk.trim(), true) || it.maNv.contains(tk.trim(), true) || it.sdt.contains(tk.trim()) }
    val mapCap = dsCap.associateBy { it.id }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { hienForm = !hienForm }) { Icon(Icons.Filled.Add, contentDescription = "Thêm") }
    }) { dem ->
        LazyColumn(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Text("Nhân viên (${dsNv.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold) }
            item { Text("Chấm công hôm nay 1 chạm • bấm tên để ghi thưởng/phạt/tạm ứng • bấm 📞 để gọi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary) }
            if (tb.isNotEmpty()) item { Card { Text(tb, Modifier.padding(12.dp)) } }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AssistChip(onClick = { tab = 0 }, label = { Text((if (tab == 0) "✓ " else "") + "Nhân viên") })
                    AssistChip(onClick = { tab = 1 }, label = { Text((if (tab == 1) "✓ " else "") + "Cấp bậc (${dsCap.size})") })
                }
            }
            if (tab == 1) {
                if (hienForm) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Thêm cấp bậc mới", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    OutlinedButton(onClick = { hienForm = false }) { Text("Đóng") }
                                }
                                OutlinedTextField(oTen, { oTen = it }, label = { Text("Tên cấp bậc mới") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(oLuong, { oLuong = it.filter(Char::isDigit) }, label = { Text("Lương cơ bản (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                Button(onClick = {
                                    vm.themCapBac(oTen, oLuong.toLongOrNull() ?: 0L, 0L); oTen = ""; oLuong = ""; hienForm = false
                                }, modifier = Modifier.fillMaxWidth()) { Text("Thêm cấp bậc") }
                            }
                        }
                    }
                }
                items(dsCap, key = { it.id }) { cap ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(cap.ten, fontWeight = FontWeight.Bold)
                            Text("Lương chuẩn: ${dinhDangTien(cap.luongCoBan)} • Chuyên cần: ${dinhDangTien(cap.chuyenCanMuc)}", style = MaterialTheme.typography.bodySmall)
                            if (cap.moTa.isNotEmpty()) Text(cap.moTa, style = MaterialTheme.typography.bodySmall)
                            Text("${dsNv.count { it.capBacId == cap.id }} NV thuộc cấp này", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                    }
                }
            } else {
                item { OTimKiem(tk, { tk = it }, "Tìm NV theo tên / mã / SĐT") }
                if (hienForm) {
                    item {
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text("Thêm nhân viên", fontWeight = FontWeight.Bold)
                                    OutlinedButton(onClick = { hienForm = false }) { Text("Đóng") }
                                }
                                OutlinedTextField(oTen, { oTen = it }, label = { Text("Họ tên (*)") }, modifier = Modifier.fillMaxWidth())
                                OutlinedTextField(oSdt, { oSdt = it.filter(Char::isDigit) }, label = { Text("SĐT") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                OutlinedTextField(oDc, { oDc = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())
                                Text("Cấp bậc: ${dsCap.firstOrNull { it.id == capChon }?.ten ?: "chưa chọn"}")
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    dsCap.forEach { cap ->
                                        AssistChip(onClick = { capChon = cap.id }, label = { Text((if (capChon == cap.id) "✓ " else "") + cap.ten) })
                                    }
                                }
                                Button(onClick = {
                                    if (capChon != null) { vm.themNhanVien(oTen, oSdt, capChon!!, oDc); oTen = ""; oSdt = ""; oDc = ""; hienForm = false }
                                }, enabled = capChon != null, modifier = Modifier.fillMaxWidth()) { Text("Thêm nhân viên") }
                            }
                        }
                    }
                }
                if (dsLoc.isEmpty()) {
                    item { OTrong("Không có nhân viên", "Bấm + để thêm.", Icons.Filled.Group) }
                } else {
                    items(dsLoc, key = { it.id }) { nv ->
                        val cap = mapCap[nv.capBacId]
                        val moRong = nvMoRong == nv.id
                        Card(
                            onClick = { nvMoRong = if (moRong) null else nv.id },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text("${nv.hoTen} • ${nv.maNv}", fontWeight = FontWeight.Bold)
                                        Text("${cap?.ten ?: ""} • SĐT: ${nv.sdt.ifEmpty { "—" }}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (nv.sdt.isNotEmpty()) IconButton(onClick = {
                                        ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${nv.sdt}")))
                                    }) { Icon(Icons.Filled.Phone, contentDescription = "Gọi NV") }
                                }
                                if (nv.diaChi.isNotEmpty()) Text("Đ/c: ${nv.diaChi}", style = MaterialTheme.typography.bodySmall)
                                if (nv.stk.isNotEmpty()) Text("STK: ${nv.stk}", style = MaterialTheme.typography.bodySmall)
                                if (nv.ghiChu.isNotEmpty()) Text("📝 ${nv.ghiChu}", style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { vm.chamNhanh(nv.id, 1.0, false) }) { Text("Chấm 1 công") }
                                    OutlinedButton(onClick = { vm.chamNhanh(nv.id, 0.5, false) }) { Text("0.5 công") }
                                    OutlinedButton(onClick = { nvMoRong = if (moRong) null else nv.id }) { Text(if (moRong) "Đóng" else "Thưởng/Ứng") }
                                }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = {
                                        if (nvSua == nv.id) nvSua = null
                                        else {
                                            nvSua = nv.id; eTen = nv.hoTen; eSdt = nv.sdt; eDc = nv.diaChi
                                            eStk = nv.stk; eGhiChu = nv.ghiChu; eCap = nv.capBacId; eTrangThai = nv.trangThai
                                        }
                                    }, modifier = Modifier.weight(1f)) { Text(if (nvSua == nv.id) "Đóng sửa" else "✏️ Sửa thông tin") }
                                    OutlinedButton(onClick = { vm.xoaNhanVien(nv.id) }, modifier = Modifier.weight(1f)) { Text("Cho nghỉ việc") }
                                }
                                if (nvSua == nv.id) {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            Text("Sửa thông tin ${nv.maNv}", fontWeight = FontWeight.Bold)
                                            OutlinedTextField(eTen, { eTen = it }, label = { Text("Họ tên (*)") }, modifier = Modifier.fillMaxWidth())
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                OutlinedTextField(eSdt, { eSdt = it.filter(Char::isDigit) }, label = { Text("SĐT") }, modifier = Modifier.weight(1f), singleLine = true)
                                                OutlinedTextField(eStk, { eStk = it }, label = { Text("STK ngân hàng") }, modifier = Modifier.weight(1f), singleLine = true)
                                            }
                                            OutlinedTextField(eDc, { eDc = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(eGhiChu, { eGhiChu = it }, label = { Text("Ghi chú") }, modifier = Modifier.fillMaxWidth())
                                            Text("Cấp bậc:", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                                            dsCap.forEach { c ->
                                                AssistChip(onClick = { eCap = c.id }, label = { Text((if (eCap == c.id) "✓ " else "") + c.ten) })
                                            }
                                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                                AssistChip(onClick = { eTrangThai = "DANGLAM" }, label = { Text((if (eTrangThai == "DANGLAM") "✓ " else "") + "Đang làm") })
                                                AssistChip(onClick = { eTrangThai = "TAMNGHI" }, label = { Text((if (eTrangThai == "TAMNGHI") "✓ " else "") + "Tạm nghỉ") })
                                                AssistChip(onClick = { eTrangThai = "NGHIVIEC" }, label = { Text((if (eTrangThai == "NGHIVIEC") "✓ " else "") + "Nghỉ việc") })
                                            }
                                            Button(onClick = {
                                                if (eCap != null) {
                                                    vm.suaNhanVien(nv.id, eTen, eSdt, eDc, eCap!!, eStk, eGhiChu, eTrangThai)
                                                    nvSua = null
                                                }
                                            }, enabled = eCap != null, modifier = Modifier.fillMaxWidth()) { Text("Lưu thay đổi") }
                                        }
                                    }
                                }
                                if (moRong) {
                                    OutlinedTextField(oTienTP, { oTienTP = it.filter(Char::isDigit) }, label = { Text("Số tiền (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                    OutlinedTextField(oLyDoTP, { oLyDoTP = it }, label = { Text("Lý do") }, modifier = Modifier.fillMaxWidth())
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        OutlinedButton(onClick = {
                                            oTienTP.toLongOrNull()?.let { vm.thuongPhat(nv.id, "THUONG", it, oLyDoTP.ifBlank { "Thưởng" }) }
                                            oTienTP = ""; oLyDoTP = ""
                                        }, modifier = Modifier.weight(1f)) { Text("Thưởng") }
                                        OutlinedButton(onClick = {
                                            oTienTP.toLongOrNull()?.let { vm.thuongPhat(nv.id, "PHAT", -it, oLyDoTP.ifBlank { "Phạt" }) }
                                            oTienTP = ""; oLyDoTP = ""
                                        }, modifier = Modifier.weight(1f)) { Text("Phạt") }
                                        OutlinedButton(onClick = {
                                            oTienTP.toLongOrNull()?.let { vm.tamUng(nv.id, it, oLyDoTP.ifBlank { "Tạm ứng" }) }
                                            oTienTP = ""; oLyDoTP = ""
                                        }, modifier = Modifier.weight(1f)) { Text("Tạm ứng") }
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
