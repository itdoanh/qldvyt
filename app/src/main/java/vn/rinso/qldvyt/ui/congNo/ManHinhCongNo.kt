package vn.rinso.qldvyt.ui.congNo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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

/** Màn Công nợ kiểu MISA: tổng nợ + quá hạn + thu nhanh. */
@Composable
fun ManHinhCongNo(
    vm: CongNoViewModel = hiltViewModel(),
    onMoDon: (String) -> Unit = {},
) {
    val tong by vm.tongNo.collectAsState()
    val ds by vm.dsNo.collectAsState()
    val soNgay by vm.soNgayCanhBao.collectAsState()
    var tk by remember { mutableStateOf("") }
    var chiQuaHan by remember { mutableStateOf(false) }
    val gio = System.currentTimeMillis()
    val dsLoc = ds.filter {
        (!chiQuaHan || (gio - it.ngayTao) / 86400000L >= soNgay) &&
            (tk.isBlank() || it.maDh.contains(tk.trim(), true))
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Công nợ (${ds.size} đơn)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("Tổng còn nợ", style = MaterialTheme.typography.labelLarge)
                Text(dinhDangTien(tong), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = Color(0xFFC62828))
                Text("Quá hạn: đơn tạo quá $soNgay ngày vẫn còn nợ.", style = MaterialTheme.typography.bodySmall)
            }
        }
        OTimKiem(tk, { tk = it }, "Tìm theo mã đơn nợ...")
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { chiQuaHan = false }) { Text((if (!chiQuaHan) "✓ " else "") + "Tất cả") }
            OutlinedButton(onClick = { chiQuaHan = true }) { Text((if (chiQuaHan) "✓ " else "") + "Quá hạn") }
        }
        if (dsLoc.isEmpty()) {
            OTrong("Không còn công nợ", "Tuyệt vời — mọi đơn đã thu đủ.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dsLoc, key = { it.id }) { don ->
                    val tuoiNo = ((gio - don.ngayTao) / 86400000L).toInt()
                    val quaHan = tuoiNo >= soNgay
                    Card(
                        onClick = { onMoDon(don.id) },
                        colors = CardDefaults.cardColors(
                            containerColor = if (quaHan) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.surfaceContainerHighest
                        ),
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(don.maDh, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                NhanTrangThai(don.trangThai, tenTrangThaiDon(don.trangThai), mauTrangThaiDon(don.trangThai))
                            }
                            Text("${dinhDangNgay(don.ngayTao)} • Tuổi nợ: $tuoiNo ngày" + if (quaHan) " • QUÁ HẠN" else "", style = MaterialTheme.typography.bodySmall)
                            Text(
                                "Còn nợ: ${dinhDangTien(don.conNo)} (đã thu ${dinhDangTien(don.daThu)}/${dinhDangTien(don.tongThanhToan)})",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error,
                            )
                            OutlinedButton(onClick = { onMoDon(don.id) }) { Text("Thu tiền ngay") }
                        }
                    }
                }
            }
        }
    }
}
