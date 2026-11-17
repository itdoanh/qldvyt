package vn.rinso.qldvyt.ui.congNo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.donHang.tenTrangThaiDon

/** Màn Công nợ: tổng nợ + danh sách đơn còn nợ (bấm để thu tiếp). */
@Composable
fun ManHinhCongNo(
    vm: CongNoViewModel = hiltViewModel(),
    onMoDon: (String) -> Unit = {},
) {
    val tong by vm.tongNo.collectAsState()
    val ds by vm.dsNo.collectAsState()
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Công nợ khách hàng", style = MaterialTheme.typography.headlineSmall)
        Card { Text("Tổng còn nợ: ${dinhDangTien(tong)}", Modifier.padding(16.dp), style = MaterialTheme.typography.titleLarge, color = MaterialTheme.colorScheme.error) }
        if (ds.isEmpty()) {
            Card { Text("Không còn công nợ. Tuyệt vời!", Modifier.padding(16.dp)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ds, key = { it.id }) { don ->
                    Card(onClick = { onMoDon(don.id) }) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(don.maDh, style = MaterialTheme.typography.titleMedium)
                            Text("${dinhDangNgay(don.ngayTao)} • ${tenTrangThaiDon(don.trangThai)}")
                            Text("Còn nợ: ${dinhDangTien(don.conNo)} (đã thu ${dinhDangTien(don.daThu)}/${dinhDangTien(don.tongThanhToan)})")
                        }
                    }
                }
            }
        }
    }
}
