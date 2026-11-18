package vn.rinso.qldvyt.ui.donHang

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.data.repo.MatHangMoi
import vn.rinso.qldvyt.data.repo.VatTuDungMoi
import vn.rinso.qldvyt.ui.common.OTimKiem

/** Wizard tạo đơn 3 bước kiểu MISA: chọn khách → chọn DV/vật tư → xác nhận. GD1. */
@Composable
fun ManHinhTaoDon(
    vm: DonHangViewModel = hiltViewModel(),
    onXong: () -> Unit = {},
) {
    var buoc by remember { mutableStateOf(1) }
    val dsKhach by vm.dsKhach.collectAsState()
    val dsDv by vm.dsDichVu.collectAsState()
    val dsGoi by vm.dsGoi.collectAsState()
    val dsVt by vm.dsVatTu.collectAsState()
    val khachChon by vm.khachDaChon.collectAsState()
    val mhChon by vm.matHangDaChon.collectAsState()
    val vtChon by vm.vatTuDaChon.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var oGiam by remember { mutableStateOf("") }
    var tkKh by remember { mutableStateOf("") }
    var tkDv by remember { mutableStateOf("") }

    val tongHang = mhChon.sumOf { (it.soLuong * it.donGia).toLong() }
    val giam = oGiam.toLongOrNull() ?: 0L
    val thanhToan = maxOf(0L, tongHang - giam)
    val khLoc = dsKhach.filter { tkKh.isBlank() || it.hoTen.contains(tkKh.trim(), true) || it.sdt.contains(tkKh.trim()) }
    val dvLoc = dsDv.filter { tkDv.isBlank() || it.ten.contains(tkDv.trim(), true) }
    val goiLoc = dsGoi.filter { tkDv.isBlank() || it.ten.contains(tkDv.trim(), true) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tạo đơn mới", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(progress = { buoc / 3f }, modifier = Modifier.fillMaxWidth())
        Text(
            when (buoc) { 1 -> "Bước 1/3: chọn khách hàng"; 2 -> "Bước 2/3: chọn dịch vụ + vật tư"; else -> "Bước 3/3: xác nhận + giảm giá" },
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
        )
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }

        if (buoc == 1) {
            OTimKiem(tkKh, { tkKh = it }, "Tìm khách theo tên / SĐT...")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(khLoc.take(30), key = { it.id }) { kh ->
                    val chon = khachChon == kh.id
                    Card(
                        onClick = { vm.chonKhach(kh.id) },
                        colors = if (chon) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text((if (chon) "✓ " else "") + kh.hoTen, fontWeight = FontWeight.Bold)
                            Text("${kh.maKh} • SĐT: ${kh.sdt.ifEmpty { "—" }}", style = MaterialTheme.typography.bodySmall)
                            if (kh.diaChi.isNotEmpty()) Text(kh.diaChi, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            Button(onClick = { buoc = 2 }, enabled = khachChon != null, modifier = Modifier.fillMaxWidth()) {
                Text("Tiếp tục → chọn dịch vụ")
            }
        } else if (buoc == 2) {
            OTimKiem(tkDv, { tkDv = it }, "Tìm dịch vụ / gói...")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(dvLoc, key = { it.id }) { dv ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(dv.ten, fontWeight = FontWeight.SemiBold)
                            Text(dinhDangTien(dv.giaNiemYet) + " / " + dv.donVi, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(onClick = { vm.themMatHang(MatHangMoi(dichVuId = dv.id, tenHienThi = dv.ten, soLuong = 1.0, donGia = dv.giaNiemYet)) }) { Text("+ Thêm") }
                    }
                }
                items(goiLoc, key = { it.id }) { goi ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(goi.ten + " (GÓI)", fontWeight = FontWeight.SemiBold)
                            Text(dinhDangTien(goi.giaGoi), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(onClick = { vm.themMatHang(MatHangMoi(goiId = goi.id, tenHienThi = goi.ten, soLuong = 1.0, donGia = goi.giaGoi)) }) { Text("+ Thêm") }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text("Đã chọn ${mhChon.size} mặt hàng • Tổng: ${dinhDangTien(tongHang)}", Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { buoc = 1 }, modifier = Modifier.weight(1f)) { Text("← Quay lại") }
                Button(onClick = { buoc = 3 }, enabled = mhChon.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Tiếp tục →") }
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mặt hàng đã chọn:", fontWeight = FontWeight.Bold)
                mhChon.forEachIndexed { i, m ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${m.tenHienThi} × ${m.soLuong.toInt()} = ${dinhDangTien((m.soLuong * m.donGia).toLong())}", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { vm.xoaMatHang(i) }) { Text("Xóa") }
                    }
                }
                OutlinedTextField(oGiam, { oGiam = it.filter(Char::isDigit) }, label = { Text("Giảm giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Vật tư dùng cho đơn (trừ tồn kho):", fontWeight = FontWeight.Bold)
                dsVt.take(15).forEach { vt ->
                    AssistChip(onClick = { vm.themVatTu(VatTuDungMoi(vt.id, 1.0)) }, label = { Text("+ ${vt.ten} (${vt.tonKho.toInt()})") })
                }
                if (vtChon.isNotEmpty()) Text("Đã gắn ${vtChon.size} dòng vật tư.", color = MaterialTheme.colorScheme.primary)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Tổng hàng: ${dinhDangTien(tongHang)}")
                        Text("Giảm: ${dinhDangTien(giam)}")
                        Text("Thanh toán: ${dinhDangTien(thanhToan)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { buoc = 2 }, modifier = Modifier.weight(1f)) { Text("← Quay lại") }
                Button(onClick = { vm.giamGia.value = giam; vm.taoDon { onXong() } }, modifier = Modifier.weight(1f)) { Text("Tạo đơn") }
            }
        }
    }
}

