package vn.rinso.qldvyt.ui.congNo

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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong
import vn.rinso.qldvyt.ui.common.mauTrangThaiDon
import vn.rinso.qldvyt.ui.donHang.tenTrangThaiDon

/** Man Cong no v3: tong no + group theo KH + goi nhanh + tuoi no bucket. */
@Composable
fun ManHinhCongNo(
    vm: CongNoViewModel = hiltViewModel(),
    onMoDon: (String) -> Unit = {},
) {
    val tong by vm.tongNo.collectAsState()
    val ds by vm.dsNo.collectAsState()
    val dsKh by vm.dsKhach.collectAsState()
    val soNgay by vm.soNgayCanhBao.collectAsState()
    var tk by remember { mutableStateOf("") }
    var chiQuaHan by remember { mutableStateOf(false) }
    var theoKhach by remember { mutableStateOf(false) }
    val gio = System.currentTimeMillis()
    val mapKh = dsKh.associateBy { it.id }
    val ctx = LocalContext.current
    val dsLoc = ds.filter {
        (!chiQuaHan || (gio - it.ngayTao) / 86400000L >= soNgay) &&
            (tk.isBlank() || it.maDh.contains(tk.trim(), true) ||
                (mapKh[it.khachHangId]?.hoTen?.contains(tk.trim(), true) == true))
    }
    val tongLoc = dsLoc.sumOf { it.conNo }
    val quaHan = dsLoc.count { (gio - it.ngayTao) / 86400000L >= soNgay }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Cong no (${ds.size} don)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("Tong con no", style = MaterialTheme.typography.labelLarge)
                Text(dinhDangTien(tong), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                Text("Loc hien tai: ${dinhDangTien(tongLoc)} - Qua han: $quaHan don (tren $soNgay ngay).", style = MaterialTheme.typography.bodySmall)
            }
        }
        OTimKiem(tk, { tk = it }, "Tim theo ma don / ten KH...")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { chiQuaHan = false }) { Text((if (!chiQuaHan) "✓ " else "") + "Tat ca") }
            OutlinedButton(onClick = { chiQuaHan = true }) { Text((if (chiQuaHan) "✓ " else "") + "Qua han") }
            OutlinedButton(onClick = { theoKhach = !theoKhach }) { Text((if (theoKhach) "✓ " else "") + "Theo KH") }
        }
        if (dsLoc.isEmpty()) {
            OTrong("Khong con cong no", "Tuyet voi - moi don da thu du.")
        } else if (theoKhach) {
            val theoKh = dsLoc.groupBy { it.khachHangId }.entries.sortedByDescending { e -> e.value.sumOf { it.conNo } }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(theoKh, key = { it.key }) { (khId, dons) ->
                    val kh = mapKh[khId]
                    val tongKh = dons.sumOf { it.conNo }
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                Text(kh?.hoTen ?: "KH le", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                Text(dinhDangTien(tongKh), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            }
                            Text("${dons.size} don no - SDT: ${kh?.sdt?.ifEmpty { "—" } ?: "—"}", style = MaterialTheme.typography.bodySmall)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (!kh?.sdt.isNullOrEmpty()) IconButton(onClick = {
                                    ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${kh!!.sdt}")))
                                }) { Icon(Icons.Filled.Phone, contentDescription = "Goi") }
                                OutlinedButton(onClick = { onMoDon(dons.maxBy { it.conNo }.id) }) { Text("Thu don lon nhat") }
                            }
                        }
                    }
                }
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dsLoc, key = { it.id }) { don ->
                    val tuoiNo = ((gio - don.ngayTao) / 86400000L).toInt()
                    val qh = tuoiNo >= soNgay
                    val kh = mapKh[don.khachHangId]
                    Card(
                        onClick = { onMoDon(don.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (qh) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(don.maDh, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                NhanTrangThai(don.trangThai, tenTrangThaiDon(don.trangThai), mauTrangThaiDon(don.trangThai))
                            }
                            Text("${kh?.hoTen ?: "KH le"} - ${kh?.sdt?.ifEmpty { "—" } ?: ""}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                            Text("${dinhDangNgay(don.ngayTao)} - Tuoi no: $tuoiNo ngay" + if (qh) " - QUA HAN" else "", style = MaterialTheme.typography.bodySmall)
                            Text("Con no: ${dinhDangTien(don.conNo)} (da thu ${dinhDangTien(don.daThu)}/${dinhDangTien(don.tongThanhToan)})", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.error)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = { onMoDon(don.id) }) { Text("Thu tien ngay") }
                                if (!kh?.sdt.isNullOrEmpty()) OutlinedButton(onClick = {
                                    ctx.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${kh!!.sdt}")))
                                }) { Text("Goi KH") }
                            }
                        }
                    }
                }
            }
        }
    }
}
