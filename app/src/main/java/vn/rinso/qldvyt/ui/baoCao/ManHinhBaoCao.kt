package vn.rinso.qldvyt.ui.baoCao

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien

/** Màn Báo cáo tổng hợp kiểu MISA/sổ tay: doanh thu + nhóm sổ + biểu đồ + lương + xuất CSV. */
@Composable
fun ManHinhBaoCao(vm: BaoCaoViewModel = hiltViewModel()) {
    val thang by vm.thang.collectAsState()
    val nam by vm.nam.collectAsState()
    val doanhThu by vm.doanhThuThang.collectAsState()
    val dsLuong by vm.dsLuong.collectAsState()
    val tb by vm.thongBao.collectAsState()
    val theoNhom by vm.theoNhom.collectAsState()
    val bieuDo by vm.bieuDoNgay.collectAsState()
    val dsDonThang by vm.dsDonThang.collectAsState()
    val tongNo by vm.tongNo.collectAsState()

    val chonCsvLuong = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvLuong(uri)
    }
    val chonCsvDon = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvDon(uri)
    }
    val chonCsvSo = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvSoDoanhThu(uri)
    }

    val tongLuong = dsLuong.sumOf { it.thucLinh }
    val soHoanThanh = dsDonThang.count { it.trangThai == "HOAN_THANH" }
    val maxCot = (bieuDo.maxOfOrNull { it.tong } ?: 0L).coerceAtLeast(1L)

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Báo cáo tổng hợp", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Mọi số liệu gộp về đây: doanh thu, nhóm sổ, biểu đồ, lương, công nợ.", style = MaterialTheme.typography.bodySmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }

        // Chọn tháng
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(
                Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = {
                    if (thang == 1) { vm.thang.value = 12; vm.nam.value = nam - 1 } else vm.thang.value = thang - 1
                }) { Text("◀") }
                Text("Tháng $thang/$nam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = {
                    if (thang == 12) { vm.thang.value = 1; vm.nam.value = nam + 1 } else vm.thang.value = thang + 1
                }) { Text("▶") }
            }
        }

        // 4 thẻ số tổng hợp
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TheBaoCao("Doanh thu", dinhDangTien(doanhThu), Color(0xFF00796B), Modifier.weight(1f), "$soHoanThanh đơn xong")
            TheBaoCao("Lương", dinhDangTien(tongLuong), Color(0xFF1565C0), Modifier.weight(1f), "${dsLuong.size} NV")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TheBaoCao("Lãi gộp", dinhDangTien(doanhThu - tongLuong), Color(0xFF2E7D32), Modifier.weight(1f), "DT − lương")
            TheBaoCao("Công nợ", dinhDangTien(tongNo), Color(0xFFC62828), Modifier.weight(1f), "toàn công ty")
        }

        // Biểu đồ cột theo cụm ngày
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Biểu đồ doanh thu theo ngày (tháng $thang)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (bieuDo.isEmpty()) {
                    Text("Chưa có đơn hoàn thành trong tháng này.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        bieuDo.forEach { cot ->
                            val tyLe = (cot.tong.toFloat() / maxCot.toFloat()).coerceIn(0.05f, 1f)
                            val caoAnim by animateFloatAsState(targetValue = 20f + tyLe * 120f, label = "cao")
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (cot.tong >= 1_000_000L) "${cot.tong / 1_000_000L}tr" else "${cot.tong / 1_000L}k",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Box(
                                    Modifier
                                        .width(36.dp)
                                        .height(caoAnim.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.primary),
                                )
                                Text(cot.nhan, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                }
                Text("Mỗi cột gom 3 ngày — giống cách cộng sổ tay cuối tháng.", style = MaterialTheme.typography.bodySmall)
            }
        }

        // Sổ doanh thu theo nhóm (giống ảnh sổ tay: tiêm truyền / đi xe-trông / thủ tục...)
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sổ doanh thu theo nhóm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AssistChip(onClick = { chonCsvSo.launch("qldvyt-so-doanh-thu-$thang-$nam.csv") }, label = { Text("Xuất CSV") })
                }
                if (theoNhom.isEmpty()) {
                    Text("Chưa có số liệu nhóm trong tháng — hãy hoàn thành đơn.", style = MaterialTheme.typography.bodySmall)
                } else {
                    theoNhom.forEach { n ->
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column(Modifier.weight(1f)) {
                                Text(tenNhomSo(n.nhom), fontWeight = FontWeight.SemiBold)
                                Text("${n.soDon} đơn", style = MaterialTheme.typography.bodySmall)
                            }
                            Text(dinhDangTien(n.tong), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text(
                        "Tổng cộng: ${dinhDangTien(theoNhom.sumOf { it.tong })}",
                        fontWeight = FontWeight.Bold,
                    )
                }
            }
        }

        // Lương + CSV
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lương & xuất file Excel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Tổng lương thực lĩnh: ${dinhDangTien(tongLuong)} (${dsLuong.size} NV)")
                Text("Lãi gộp ước tính: ${dinhDangTien(doanhThu - tongLuong)}", color = MaterialTheme.colorScheme.primary)
                Button(onClick = { chonCsvDon.launch("qldvyt-don-$thang-$nam.csv") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Xuất CSV đơn hàng tháng này (${dsDonThang.size} đơn)")
                }
                Button(onClick = { chonCsvLuong.launch("qldvyt-luong-$thang-$nam.csv") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Xuất CSV bảng lương tháng này")
                }
                Text("File CSV mở bằng Excel, giữ tiếng Việt (có BOM).", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun TheBaoCao(tieuDe: String, giaTri: String, mau: Color, modifier: Modifier = Modifier, phu: String = "") {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = mau.copy(alpha = 0.12f)),
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(tieuDe, style = MaterialTheme.typography.labelLarge)
            Text(giaTri, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = mau)
            if (phu.isNotEmpty()) Text(phu, style = MaterialTheme.typography.bodySmall)
        }
    }
}
