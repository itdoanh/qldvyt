package vn.rinso.qldvyt.ui.donHang

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.data.repo.MatHangMoi
import vn.rinso.qldvyt.data.repo.VatTuDungMoi

/** Wizard tạo đơn 3 bước: 1) chọn khách → 2) chọn dịch vụ/vật tư → 3) xác nhận. */
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

    val tongHang = mhChon.sumOf { (it.soLuong * it.donGia).toLong() }
    val giam = oGiam.toLongOrNull() ?: 0L
    val thanhToan = maxOf(0L, tongHang - giam)

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tạo đơn mới — bước $buoc/3", style = MaterialTheme.typography.headlineSmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }

        if (buoc == 1) {
            Text("Bước 1: chọn khách hàng")
            if (dsKhach.isEmpty()) Text("Chưa có khách — hãy qua tab Khách hàng để thêm trước.")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(dsKhach, key = { it.id }) { kh ->
                    val chon = khachChon == kh.id
                    Card(onClick = { vm.chonKhach(kh.id) }) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(kh.hoTen + if (chon) " ✓" else "", style = MaterialTheme.typography.titleMedium)
                            Text("SĐT: ${kh.sdt.ifEmpty { "—" }}")
                        }
                    }
                }
            }
            Button(onClick = { buoc = 2 }, enabled = khachChon != null, modifier = Modifier.fillMaxWidth()) {
                Text("Tiếp tục → chọn dịch vụ")
            }
        } else if (buoc == 2) {
            Text("Bước 2: chọn dịch vụ/gói + vật tư (giá snapshot lúc bán)")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(dsDv, key = { it.id }) { dv ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) { Text(dv.ten); Text(dinhDangTien(dv.giaNiemYet), style = MaterialTheme.typography.bodySmall) }
                        OutlinedButton(onClick = {
                            vm.themMatHang(MatHangMoi(dichVuId = dv.id, tenHienThi = dv.ten, soLuong = 1.0, donGia = dv.giaNiemYet))
                        }) { Text("+ Thêm") }
                    }
                }
                items(dsGoi, key = { it.id }) { goi ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) { Text(goi.ten + " (gói)"); Text(dinhDangTien(goi.giaGoi), style = MaterialTheme.typography.bodySmall) }
                        OutlinedButton(onClick = {
                            vm.themMatHang(MatHangMoi(goiId = goi.id, tenHienThi = goi.ten, soLuong = 1.0, donGia = goi.giaGoi))
                        }) { Text("+ Thêm") }
                    }
                }
            }
            Text("Đã chọn ${mhChon.size} mặt hàng • Tổng: ${dinhDangTien(tongHang)}")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { buoc = 1 }, modifier = Modifier.weight(1f)) { Text("← Quay lại") }
                Button(onClick = { buoc = 3 }, enabled = mhChon.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Tiếp tục →") }
            }
        } else {
            Text("Bước 3: xác nhận + giảm giá")
            mhChon.forEachIndexed { i, m ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${m.tenHienThi} × ${m.soLuong.toInt()} = ${dinhDangTien((m.soLuong * m.donGia).toLong())}", modifier = Modifier.weight(1f))
                    OutlinedButton(onClick = { vm.xoaMatHang(i) }) { Text("Xóa") }
                }
            }
            OutlinedTextField(
                value = oGiam, onValueChange = { oGiam = it.filter(Char::isDigit) },
                label = { Text("Giảm giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true,
            )
            var vtChonId by remember { mutableStateOf<String?>(null) }
            Text("Vật tư dùng cho đơn (trừ tồn kho):")
            dsVt.take(20).forEach { vt ->
                AssistChip(
                    onClick = {
                        vtChonId = vt.id
                        vm.themVatTu(VatTuDungMoi(vt.id, 1.0))
                    },
                    label = { Text("+ ${vt.ten} (${vt.tonKho.toInt()})") },
                )
            }
            if (vtChon.isNotEmpty()) Text("Đã gắn ${vtChon.size} dòng vật tư.")
            Text("Tổng thanh toán: ${dinhDangTien(thanhToan)}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { buoc = 2 }, modifier = Modifier.weight(1f)) { Text("← Quay lại") }
                Button(onClick = {
                    vm.giamGia.value = giam
                    vm.taoDon { onXong() }
                }, modifier = Modifier.weight(1f)) { Text("Tạo đơn") }
            }
        }
    }
}
