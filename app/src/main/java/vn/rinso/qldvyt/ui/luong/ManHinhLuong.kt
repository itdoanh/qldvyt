package vn.rinso.qldvyt.ui.luong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.DuongDanCongThuc
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.OTrong

/** Màn Lương v3: diễn giải công thức từng dòng + chốt/mở khóa. */
@Composable
fun ManHinhLuong(vm: LuongViewModel = hiltViewModel()) {
    val thang by vm.thang.collectAsState()
    val nam by vm.nam.collectAsState()
    val ds by vm.dsBangLuong.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var moDienGiai by remember { mutableStateOf<String?>(null) }
    val tong = ds.sumOf { it.thucLinh }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Bảng lương $thang/$nam (${ds.size} NV)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Thực lĩnh = theo công + NV 30% + thưởng + chuyên cần − tạm ứng − phạt. Bấm tên để xem diễn giải.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = {
                    if (thang == 1) { vm.thang.value = 12; vm.nam.value = nam - 1 } else vm.thang.value = thang - 1
                }) { Text("◀") }
                Column {
                    Text("Tháng $thang/$nam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Tổng: ${dinhDangTien(tong)}", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = {
                    if (thang == 12) { vm.thang.value = 1; vm.nam.value = nam + 1 } else vm.thang.value = thang + 1
                }) { Text("▶") }
            }
        }
        Button(onClick = { vm.tinhCaThang() }, modifier = Modifier.fillMaxWidth()) { Text("Tính lương cả tháng") }
        if (ds.isEmpty()) {
            OTrong("Chưa có bảng lương", "Chấm công + hoàn thành nhiệm vụ rồi bấm Tính cả tháng.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ds, key = { it.id }) { bl ->
                    val ten = vm.tenNv(bl.nhanVienId)
                    val mo = moDienGiai == bl.id
                    Card(
                        onClick = { moDienGiai = if (mo) null else bl.id },
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text(ten, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                NhanTrangThai(
                                    bl.trangThai, if (bl.trangThai == "CHOT") "ĐÃ CHỐT" else "Nháp",
                                    if (bl.trangThai == "CHOT") Color(0xFF2E7D32) else Color(0xFFEF6C00),
                                )
                            }
                            Text("Công: ${bl.tongCong} • Theo công: ${dinhDangTien(bl.luongCoBan)}", style = MaterialTheme.typography.bodySmall)
                            Text("NV 30%: ${dinhDangTien(bl.tienNhiemVu)} • Thưởng: ${dinhDangTien(bl.thuong)} • C.cần: ${dinhDangTien(bl.chuyenCan)}", style = MaterialTheme.typography.bodySmall)
                            Text("Tạm ứng: ${dinhDangTien(bl.tamUng)} • Phạt: ${dinhDangTien(bl.khauTruKhac)}", style = MaterialTheme.typography.bodySmall)
                            Text("Thực lĩnh: ${dinhDangTien(bl.thucLinh)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                            if (mo) {
                                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                        Text("Diễn giải công thức:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                        DuongDanCongThuc("Lương chuẩn / công chuẩn × công thực", dinhDangTien(bl.luongCoBan))
                                        DuongDanCongThuc("Tiền nhiệm vụ 30% đã hoàn thành", "+ ${dinhDangTien(bl.tienNhiemVu)}")
                                        DuongDanCongThuc("Thưởng + chuyên cần", "+ ${dinhDangTien(bl.thuong + bl.chuyenCan)}")
                                        DuongDanCongThuc("Tạm ứng + phạt", "− ${dinhDangTien(bl.tamUng + bl.khauTruKhac)}")
                                        DuongDanCongThuc("Thực lĩnh", dinhDangTien(bl.thucLinh))
                                    }
                                }
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (bl.trangThai != "CHOT") Button(onClick = { vm.chot(bl.nhanVienId) }) { Text("Chốt") }
                                else OutlinedButton(onClick = { vm.moKhoa(bl.nhanVienId) }) { Text("Mở khóa (admin)") }
                                OutlinedButton(onClick = { moDienGiai = if (mo) null else bl.id }) { Text(if (mo) "Ẩn" else "Xem công thức") }
                            }
                        }
                    }
                }
            }
        }
    }
}
