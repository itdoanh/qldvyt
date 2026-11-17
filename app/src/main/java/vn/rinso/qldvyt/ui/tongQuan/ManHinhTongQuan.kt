package vn.rinso.qldvyt.ui.tongQuan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien

/** Màn Tổng quan: số liệu thật từ Room (doanh thu hôm nay, đơn chờ, công nợ, vật tư sắp hết). */
@Composable
fun ManHinhTongQuan(
    onGioiThieu: () -> Unit = {},
    vm: TongQuanViewModel = hiltViewModel(),
) {
    val doanhThu by vm.doanhThuHomNay.collectAsState()
    val donCho by vm.donCho.collectAsState()
    val tongNo by vm.tongNo.collectAsState()
    val sapHet by vm.dsSapHet.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Tổng quan hôm nay", style = MaterialTheme.typography.headlineSmall)
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            TheSo("Doanh thu", dinhDangTien(doanhThu), Modifier.weight(1f))
            TheSo("Đơn chờ", donCho.toString(), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            TheSo("Công nợ", dinhDangTien(tongNo), Modifier.weight(1f))
            TheSo("Vật tư sắp hết", sapHet.size.toString(), Modifier.weight(1f))
        }
        if (sapHet.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Text("⚠ Sắp hết: ${sapHet.take(3).joinToString { "${it.ten} (${it.tonKho.toInt()})" }}", Modifier.padding(12.dp))
            }
        }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
            Column(Modifier.padding(16.dp)) {
                Text("Chào mừng đến QLDVYT!", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(4.dp))
                Text("Đơn hàng, nhiệm vụ NV hưởng 30%, vật tư, lương, doanh thu, công nợ — lưu local, backup file.")
                Spacer(Modifier.height(8.dp))
                Button(onClick = onGioiThieu) { Text("Giới thiệu Rinso") }
            }
        }
    }
}

@Composable
private fun TheSo(tieuDe: String, giaTri: String, modifier: Modifier = Modifier) {
    Card(modifier = modifier) {
        Column(Modifier.padding(16.dp)) {
            Text(tieuDe, style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(4.dp))
            Text(giaTri, style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        }
    }
}
