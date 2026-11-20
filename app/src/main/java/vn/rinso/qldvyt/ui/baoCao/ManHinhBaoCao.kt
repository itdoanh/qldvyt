package vn.rinso.qldvyt.ui.baoCao

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.HangTienTrinh

/** Màn hình Báo cáo tổng hợp: KPI + biểu đồ chi tiết từng ngày + sổ nhóm + lương + xuất CSV. */
@Composable
fun ManHinhBaoCao(vm: BaoCaoViewModel = hiltViewModel()) {
    val thang by vm.thang.collectAsState()
    val nam by vm.nam.collectAsState()
    val doanhThu by vm.doanhThuThang.collectAsState()
    val doanhThuTruoc by vm.doanhThuThangTruoc.collectAsState()
    val dsLuong by vm.dsLuong.collectAsState()
    val tb by vm.thongBao.collectAsState()
    val theoNhom by vm.theoNhom.collectAsState()
    val bieuDo by vm.bieuDoNgay.collectAsState()
    val dsDonThang by vm.dsDonThang.collectAsState()
    val tongNo by vm.tongNo.collectAsState()
    val tk by vm.thongKe.collectAsState()
    val ngayChon by vm.ngayChon.collectAsState()

    val chonCsvLuong = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvLuong(uri)
    }
    val chonCsvDon = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvDon(uri)
    }
    val chonCsvSo = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvSoDoanhThu(uri)
    }
    val chonCsvNgay = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvNgay(uri)
    }

    val tongLuong = dsLuong.sumOf { it.thucLinh }
    val maxCot = (bieuDo.maxOfOrNull { it.tong } ?: 0L).coerceAtLeast(1L)
    val chenh = doanhThu - doanhThuTruoc
    val tyLeTtt = if (doanhThuTruoc > 0) doanhThu.toFloat() / doanhThuTruoc else if (doanhThu > 0) 1f else 0f

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Báo cáo tổng hợp", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Chạm vào từng cột để xem chi tiết đơn trong ngày — có so sánh với tháng trước.", style = MaterialTheme.typography.bodySmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(
                Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = {
                    if (thang == 1) { vm.thang.value = 12; vm.nam.value = nam - 1 } else vm.thang.value = thang - 1
                    vm.ngayChon.value = null
                }) { Text("◀") }
                Text("Tháng $thang/$nam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = {
                    if (thang == 12) { vm.thang.value = 1; vm.nam.value = nam + 1 } else vm.thang.value = thang + 1
                    vm.ngayChon.value = null
                }) { Text("▶") }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TheBaoCao("Doanh thu", dinhDangTien(doanhThu), Color(0xFF00796B), Modifier.weight(1f), "${tk.soDonHoanThanh} đơn xong • TB ${dinhDangTien(tk.trungBinhDon)}/đơn")
            TheBaoCao("Lương", dinhDangTien(tongLuong), Color(0xFF1565C0), Modifier.weight(1f), "${dsLuong.size} nhân viên")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TheBaoCao("Lãi gộp", dinhDangTien(doanhThu - tongLuong), Color(0xFF2E7D32), Modifier.weight(1f), "Doanh thu − lương")
            TheBaoCao("Công nợ", dinhDangTien(tongNo), Color(0xFFC62828), Modifier.weight(1f), "Toàn công ty")
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Chỉ số tháng $thang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Trước VAT: ${dinhDangTien(tk.truocVat)} • VAT đã thu: ${dinhDangTien(tk.tienVat)}")
                Text("Chờ xử lý: ${tk.soDonCho} • Đang làm: ${tk.soDonDangLam} • Hủy: ${tk.soDonHuy}")
                if (tk.ngayCaoNhat > 0) Text("Ngày cao nhất: ngày ${tk.ngayCaoNhat} — ${dinhDangTien(tk.doanhThuCaoNhat)}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("So với tháng trước (${dinhDangTien(doanhThuTruoc)})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                HangTienTrinh(
                    if (chenh >= 0) "Tăng ${dinhDangTien(chenh)}" else "Giảm ${dinhDangTien(-chenh)}",
                    tyLeTtt.coerceIn(0f, 2f) / 2f,
                    if (chenh >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                )
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Doanh thu từng ngày (tháng $thang)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AssistChip(onClick = { chonCsvNgay.launch("qldvyt-doanh-thu-ngay-$thang-$nam.csv") }, label = { Text("Xuất CSV") })
                }
                Text("31 cột = 31 ngày, cột cao = thu nhiều. Chạm để xem đơn của ngày đó.", style = MaterialTheme.typography.bodySmall)
                if (bieuDo.all { it.tong == 0L }) {
                    Text("Chưa có đơn hoàn thành trong tháng này.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        bieuDo.forEach { cot ->
                            val tyLe = (cot.tong.toFloat() / maxCot.toFloat()).coerceIn(0.05f, 1f)
                            val caoAnim by animateFloatAsState(targetValue = 24f + tyLe * 110f, label = "cao")
                            val dangChon = ngayChon == cot.ngay
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (cot.tong <= 0) "" else if (cot.tong >= 1_000_000L) "${cot.tong / 1_000_000L}tr" else "${cot.tong / 1_000L}k",
                                    style = MaterialTheme.typography.labelSmall,
                                )
                                Box(
                                    Modifier
                                        .width(if (dangChon) 34.dp else 26.dp)
                                        .height(if (cot.tong <= 0) 8.dp else caoAnim.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            when {
                                                dangChon -> MaterialTheme.colorScheme.tertiary
                                                cot.tong <= 0 -> MaterialTheme.colorScheme.surfaceVariant
                                                else -> MaterialTheme.colorScheme.primary
                                            }
                                        )
                                        .clickable { vm.ngayChon.value = if (dangChon) null else cot.ngay },
                                )
                                Text(cot.nhan, style = MaterialTheme.typography.labelSmall, fontWeight = if (dangChon) FontWeight.Bold else FontWeight.Normal)
                            }
                        }
                    }
                    if (ngayChon != null) {
                        val cot = bieuDo.firstOrNull { it.ngay == ngayChon }
                        if (cot != null) {
                            val donNgay = dsDonThang.filter { it.trangThai == "HOAN_THANH" }.filter {
                                val c = java.util.Calendar.getInstance().apply { timeInMillis = it.ngayTao }
                                c.get(java.util.Calendar.DAY_OF_MONTH) == cot.ngay
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    Text("Ngày ${cot.ngay}/$thang: ${donNgay.size} đơn — ${dinhDangTien(cot.tong)} (VAT ${dinhDangTien(cot.tienVat)})", fontWeight = FontWeight.Bold)
                                    if (donNgay.isEmpty()) Text("Ngày này chưa có đơn hoàn thành.", style = MaterialTheme.typography.bodySmall)
                                    donNgay.take(8).forEach { d ->
                                        Text("• ${d.maDh}: ${dinhDangTien(d.tongThanhToan)} (${dinhDangNgay(d.ngayTao)})", style = MaterialTheme.typography.bodySmall)
                                    }
                                    if (donNgay.size > 8) Text("... và ${donNgay.size - 8} đơn nữa", style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    } else {
                        Text("Chạm vào từng cột để xem đơn cấu thành.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Sổ doanh thu theo nhóm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AssistChip(onClick = { chonCsvSo.launch("qldvyt-so-doanh-thu-$thang-$nam.csv") }, label = { Text("Xuất CSV") })
                }
                if (theoNhom.isEmpty()) {
                    Text("Chưa có số liệu nhóm trong tháng — hãy hoàn thành đơn.", style = MaterialTheme.typography.bodySmall)
                } else {
                    val tongNhom = theoNhom.sumOf { it.tong }.coerceAtLeast(1L)
                    theoNhom.forEach { n ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(tenNhomSo(n.nhom), fontWeight = FontWeight.SemiBold)
                                    Text("${n.soDon} đơn", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(dinhDangTien(n.tong), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            HangTienTrinh("", n.tong.toFloat() / tongNhom, MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text("Tổng cộng: ${dinhDangTien(theoNhom.sumOf { it.tong })}", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lương & xuất file Excel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Tổng lương thực lĩnh: ${dinhDangTien(tongLuong)} (${dsLuong.size} nhân viên)")
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
