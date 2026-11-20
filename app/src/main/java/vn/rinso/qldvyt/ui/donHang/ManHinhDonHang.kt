package vn.rinso.qldvyt.ui.donHang

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
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import android.content.Intent
import android.net.Uri
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong
import vn.rinso.qldvyt.ui.common.mauTrangThaiDon

/** Ten trang thai don hien thi tieng Viet. */
fun tenTrangThaiDon(tt: String): String = when (tt) {
    "CHO_XU_LY" -> "Cho xu ly"
    "DANG_LAM" -> "Dang lam"
    "HOAN_THANH" -> "Hoan thanh"
    "HUY" -> "Da huy"
    else -> tt
}

/** Man Don hang v3: hien ten KH + SDT goi nhanh + sap xep + loc. */
@Composable
fun ManHinhDonHang(
    vm: DonHangViewModel = hiltViewModel(),
    onTaoDon: () -> Unit = {},
    onMoChiTiet: (String) -> Unit = {},
) {
    val ds by vm.dsDon.collectAsState()
    val dsKh by vm.dsKhach.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var loc by remember { mutableStateOf<String?>(null) }
    var tk by remember { mutableStateOf("") }
    var sapXep by remember { mutableStateOf(0) } // 0 moi nhat, 1 no nhieu, 2 thanh toan lon
    val mapKh = dsKh.associateBy { it.id }
    val ctx = LocalContext.current

    var dsLoc = ds.filter {
        (loc == null || it.trangThai == loc) &&
            (tk.isBlank() || it.maDh.contains(tk.trim(), true) ||
                (mapKh[it.khachHangId]?.hoTen?.contains(tk.trim(), true) == true) ||
                (mapKh[it.khachHangId]?.sdt?.contains(tk.trim()) == true))
    }
    dsLoc = when (sapXep) {
        1 -> dsLoc.sortedByDescending { it.conNo }
        2 -> dsLoc.sortedByDescending { it.tongThanhToan }
        else -> dsLoc.sortedByDescending { it.ngayTao }
    }
    val tongLoc = dsLoc.sumOf { it.tongThanhToan }
    val noLoc = dsLoc.sumOf { it.conNo }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = onTaoDon) { Icon(Icons.Filled.Add, contentDescription = "Tao don") }
    }) { dem ->
        Column(
            Modifier.fillMaxSize().padding(dem).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text("Don hang (${ds.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Tong loc: ${dinhDangTien(tongLoc)} - Con no: ${dinhDangTien(noLoc)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
            OTimKiem(tk, { tk = it }, "Tim theo ma don / ten KH / SDT...")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(null to "Tat ca", "CHO_XU_LY" to "Cho", "DANG_LAM" to "Dang lam", "HOAN_THANH" to "Xong", "HUY" to "Huy").forEach { (ma, ten) ->
                    AssistChip(onClick = { loc = ma }, label = { Text((if (loc == ma) "✓ " else "") + ten) })
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("Moi nhat", "No nhieu", "Tien lon").forEachIndexed { i, ten ->
                    AssistChip(onClick = { sapXep = i }, label = { Text((if (sapXep == i) "✓ " else "") + ten) })
                }
            }
            if (dsLoc.isEmpty()) {
                OTrong("Khong co don nao", "Bam + de tao don moi cho khach.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsLoc, key = { it.id }) { don ->
                        val kh = mapKh[don.khachHangId]
                        Card(
                            onClick = { onMoChiTiet(don.id) },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text(don.maDh, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    NhanTrangThai(don.trangThai, tenTrangThaiDon(don.trangThai), mauTrangThaiDon(don.trangThai))
                                }
                                Text("${kh?.hoTen ?: "KH le"} - ${kh?.sdt?.ifEmpty { "chua co SDT" } ?: ""}", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
                                if (!kh?.diaChi.isNullOrEmpty()) Text(kh!!.diaChi, style = MaterialTheme.typography.bodySmall)
                                Text("${dinhDangNgay(don.ngayTao)} - Thanh toan: ${dinhDangTien(don.tongThanhToan)}", style = MaterialTheme.typography.bodySmall)
                                Text(
                                    "Da thu: ${dinhDangTien(don.daThu)} - Con no: ${dinhDangTien(don.conNo)}",
                                    color = if (don.conNo > 0) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.SemiBold,
                                )
                                if (!kh?.sdt.isNullOrEmpty()) {
                                    Row {
                                        IconButton(onClick = {
                                            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${kh!!.sdt}"))
                                            ctx.startActivity(intent)
                                        }) { Icon(Icons.Filled.Phone, contentDescription = "Goi KH") }
                                        Text("Bam de mo chi tiet / thu tien", style = MaterialTheme.typography.bodySmall, modifier = Modifier.align(Alignment.CenterVertically))
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
