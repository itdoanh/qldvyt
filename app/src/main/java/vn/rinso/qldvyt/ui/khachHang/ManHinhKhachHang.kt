package vn.rinso.qldvyt.ui.khachHang

import android.content.Intent
import android.net.Uri
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

/** Màn Khách hàng v3: công nợ + tổng chi tiêu + gọi nhanh + tạo đơn. */
@Composable
fun ManHinhKhachHang(
    vm: KhachHangViewModel = hiltViewModel(),
    onChon: (String) -> Unit = {},
) {
    val ds by vm.danhSach.collectAsState()
    val dsDon by vm.dsDon.collectAsState()
    val tk by vm.tuKhoa.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var hienThem by remember { mutableStateOf(false) }
    var chiNo by remember { mutableStateOf(false) }
    var oTen by remember { mutableStateOf("") }
    var oSdt by remember { mutableStateOf("") }
    var oDiaChi by remember { mutableStateOf("") }
    var suaId by remember { mutableStateOf<String?>(null) }
    var sTen by remember { mutableStateOf("") }
    var sSdt by remember { mutableStateOf("") }
    var sDiaChi by remember { mutableStateOf("") }
    var sGhiChu by remember { mutableStateOf("") }
    val ctx = LocalContext.current

    val noTheoKh = dsDon.filter { it.conNo > 0 && it.trangThai != "HUY" }.groupBy { it.khachHangId }
    val donTheoKh = dsDon.groupBy { it.khachHangId }
    val dsHien = if (chiNo) ds.filter { (noTheoKh[it.id]?.sumOf { d -> d.conNo } ?: 0L) > 0 } else ds

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { hienThem = !hienThem }) { Icon(Icons.Filled.Add, contentDescription = "Thêm KH") }
    }) { dem ->
        Column(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Khách hàng (${ds.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("${noTheoKh.size} KH đang nợ - bấm tên để tạo đơn, bấm điện thoại để gọi.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
            OTimKiem(tk, { vm.tuKhoa.value = it }, "Tìm theo tên hoặc SĐT...")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { chiNo = false }, label = { Text((if (!chiNo) "✓ " else "") + "Tất cả") })
                AssistChip(onClick = { chiNo = true }, label = { Text((if (chiNo) "✓ " else "") + "Đang nợ (${noTheoKh.size})") })
            }
            if (hienThem) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Thêm khách hàng mới", fontWeight = FontWeight.Bold)
                        OutlinedTextField(oTen, { oTen = it }, label = { Text("Họ tên (*)") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(oSdt, { oSdt = it.filter(Char::isDigit) }, label = { Text("SĐT") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(oDiaChi, { oDiaChi = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())
                        Button(onClick = {
                            vm.them(oTen, oSdt, oDiaChi)
                            oTen = ""; oSdt = ""; oDiaChi = ""; hienThem = false
                        }, modifier = Modifier.fillMaxWidth()) { Text("Lưu khách hàng") }
                    }
                }
            }
            if (dsHien.isEmpty()) {
                OTrong("Chưa có khách hàng", "Bấm + để thêm khách mới.", Icons.Filled.Person)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsHien, key = { it.id }) { kh ->
                        val no = noTheoKh[kh.id]?.sumOf { d -> d.conNo } ?: 0L
                        val dons = donTheoKh[kh.id] ?: emptyList()
                        val chi = dons.filter { it.trangThai == "HOAN_THANH" }.sumOf { it.tongThanhToan }
                        Card(
                            onClick = { onChon(kh.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = if (no > 0) MaterialTheme.colorScheme.errorContainer
                                else MaterialTheme.colorScheme.surfaceContainerHighest
                            ),
                        ) {
                            Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(kh.hoTen, fontWeight = FontWeight.Bold)
                                        Text("${kh.maKh} - SĐT: ${kh.sdt.ifEmpty { "—" }}", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (kh.sdt.isNotEmpty()) IconButton(onClick = {
                                        ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${kh.sdt}")))
                                    }) { Icon(Icons.Filled.Phone, contentDescription = "Gọi") }
                                }
                                if (kh.diaChi.isNotEmpty()) Text(kh.diaChi, style = MaterialTheme.typography.bodySmall)
                                Text("${dons.size} đơn - Đã chi: ${dinhDangTien(chi)}" + if (no > 0) " - Nợ: ${dinhDangTien(no)}" else " - Không nợ", style = MaterialTheme.typography.bodySmall, color = if (no > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
                                if (kh.ghiChu.isNotEmpty()) Text(kh.ghiChu, style = MaterialTheme.typography.bodySmall)
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { onChon(kh.id) }) { Text("Tạo đơn") }
                                    OutlinedButton(onClick = {
                                        if (suaId == kh.id) suaId = null
                                        else {
                                            suaId = kh.id
                                            sTen = kh.hoTen; sSdt = kh.sdt; sDiaChi = kh.diaChi; sGhiChu = kh.ghiChu
                                        }
                                    }) { Text(if (suaId == kh.id) "Đóng" else "Sửa") }
                                    OutlinedButton(onClick = { vm.xoa(kh) }) { Text("Xóa") }
                                }
                                if (suaId == kh.id) {
                                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                            OutlinedTextField(sTen, { sTen = it }, label = { Text("Họ tên (*)") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(sSdt, { sSdt = it.filter(Char::isDigit) }, label = { Text("SĐT") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                                            OutlinedTextField(sDiaChi, { sDiaChi = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())
                                            OutlinedTextField(sGhiChu, { sGhiChu = it }, label = { Text("Ghi chú") }, modifier = Modifier.fillMaxWidth())
                                            Button(onClick = {
                                                vm.sua(kh, sTen, sSdt, sDiaChi, sGhiChu)
                                                suaId = null
                                            }, modifier = Modifier.fillMaxWidth()) { Text("Lưu thay đổi") }
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
}
