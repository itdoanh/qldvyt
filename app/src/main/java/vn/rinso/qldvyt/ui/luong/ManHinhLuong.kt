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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien

/** Màn Lương: chọn tháng → tính cả tháng → chốt/mở khóa từng NV. */
@Composable
fun ManHinhLuong(vm: LuongViewModel = hiltViewModel()) {
    val thang by vm.thang.collectAsState()
    val nam by vm.nam.collectAsState()
    val ds by vm.dsBangLuong.collectAsState()
    val tb by vm.thongBao.collectAsState()
    val tong = ds.sumOf { it.thucLinh }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Bảng lương tháng $thang/$nam", style = MaterialTheme.typography.headlineSmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                if (thang == 1) { vm.thang.value = 12; vm.nam.value = nam - 1 } else vm.thang.value = thang - 1
            }, modifier = Modifier.weight(1f)) { Text("◀ Tháng trước") }
            Button(onClick = { vm.tinhCaThang() }, modifier = Modifier.weight(1f)) { Text("Tính cả tháng") }
            OutlinedButton(onClick = {
                if (thang == 12) { vm.thang.value = 1; vm.nam.value = nam + 1 } else vm.thang.value = thang + 1
            }, modifier = Modifier.weight(1f)) { Text("Tháng sau ▶") }
        }
        Card { Text("Tổng thực lĩnh: ${dinhDangTien(tong)} (${ds.size} NV)", Modifier.padding(16.dp), style = MaterialTheme.typography.titleMedium) }
        if (ds.isEmpty()) {
            Card { Text("Chưa có bảng lương. Bấm “Tính cả tháng” sau khi đã chấm công + hoàn thành nhiệm vụ.", Modifier.padding(16.dp)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ds, key = { it.id }) { bl ->
                    Card {
                        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("NV: ${bl.nhanVienId.take(8)}… • ${if (bl.trangThai == "CHOT") "ĐÃ CHỐT" else "Nháp"}", style = MaterialTheme.typography.titleMedium)
                            Text("Công: ${bl.tongCong} • Theo công: ${dinhDangTien(bl.luongCoBan)}")
                            Text("Nhiệm vụ 30%: ${dinhDangTien(bl.tienNhiemVu)} • Thưởng: ${dinhDangTien(bl.thuong)} • Chuyên cần: ${dinhDangTien(bl.chuyenCan)}")
                            Text("Tạm ứng: ${dinhDangTien(bl.tamUng)} • Phạt: ${dinhDangTien(bl.khauTruKhac)}")
                            Text("Thực lĩnh: ${dinhDangTien(bl.thucLinh)}", style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.primary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (bl.trangThai != "CHOT") Button(onClick = { vm.chot(bl.nhanVienId) }) { Text("Chốt") }
                                else OutlinedButton(onClick = { vm.moKhoa(bl.nhanVienId) }) { Text("Mở khóa (admin)") }
                            }
                        }
                    }
                }
            }
        }
    }
}
