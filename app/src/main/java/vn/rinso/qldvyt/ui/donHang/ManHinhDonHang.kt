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
import androidx.compose.material3.Card
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien

/** Tên trạng thái đơn hiển thị tiếng Việt. */
fun tenTrangThaiDon(tt: String): String = when (tt) {
    "CHO_XU_LY" -> "Chờ xử lý"
    "DANG_LAM" -> "Đang làm"
    "HOAN_THANH" -> "Hoàn thành"
    "HUY" -> "Đã hủy"
    else -> tt
}

/** Màn Đơn hàng: danh sách + lọc trạng thái + nút tạo đơn. */
@Composable
fun ManHinhDonHang(
    vm: DonHangViewModel = hiltViewModel(),
    onTaoDon: () -> Unit = {},
    onMoChiTiet: (String) -> Unit = {},
) {
    val ds by vm.dsDon.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var loc by remember { mutableStateOf<String?>(null) }
    val dsLoc = if (loc == null) ds else ds.filter { it.trangThai == loc }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Đơn hàng", style = MaterialTheme.typography.headlineSmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        OutlinedButton(onClick = onTaoDon, modifier = Modifier.fillMaxWidth()) { Text("+ Tạo đơn mới (3 bước)") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null to "Tất cả", "CHO_XU_LY" to "Chờ", "DANG_LAM" to "Đang làm", "HOAN_THANH" to "Xong", "HUY" to "Hủy").forEach { (ma, ten) ->
                AssistChip(onClick = { loc = ma }, label = { Text(ten) })
            }
        }
        if (dsLoc.isEmpty()) {
            Card { Text("Chưa có đơn hàng nào. Bấm “Tạo đơn mới”.", Modifier.padding(16.dp)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dsLoc, key = { it.id }) { don ->
                    Card(onClick = { onMoChiTiet(don.id) }) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text(don.maDh, style = MaterialTheme.typography.titleMedium)
                            Text("${dinhDangNgay(don.ngayTao)} • ${tenTrangThaiDon(don.trangThai)}")
                            Text(
                                "Thanh toán: ${dinhDangTien(don.tongThanhToan)} • Đã thu: ${dinhDangTien(don.daThu)} • Còn nợ: ${dinhDangTien(don.conNo)}",
                                color = if (don.conNo > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                            )
                        }
                    }
                }
            }
        }
    }
}
