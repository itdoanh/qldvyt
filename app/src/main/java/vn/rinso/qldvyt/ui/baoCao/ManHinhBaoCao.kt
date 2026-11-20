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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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

/** Man Bao cao v3: so sanh thang truoc + bieu do bam duoc + so nhom + CSV. */
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
    var cotChon by remember { mutableStateOf<Int?>(null) }

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
    val chenh = doanhThu - doanhThuTruoc
    val tyLeTtt = if (doanhThuTruoc > 0) doanhThu.toFloat() / doanhThuTruoc else if (doanhThu > 0) 1f else 0f

    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Bao cao tong hop", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Bam vao cot bieu do de xem don cau thanh - co so sanh thang truoc.", style = MaterialTheme.typography.bodySmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(
                Modifier.padding(12.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                OutlinedButton(onClick = {
                    if (thang == 1) { vm.thang.value = 12; vm.nam.value = nam - 1 } else vm.thang.value = thang - 1
                }) { Text("◀") }
                Text("Thang $thang/$nam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                OutlinedButton(onClick = {
                    if (thang == 12) { vm.thang.value = 1; vm.nam.value = nam + 1 } else vm.thang.value = thang + 1
                }) { Text("▶") }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TheBaoCao("Doanh thu", dinhDangTien(doanhThu), Color(0xFF00796B), Modifier.weight(1f), "$soHoanThanh don xong")
            TheBaoCao("Luong", dinhDangTien(tongLuong), Color(0xFF1565C0), Modifier.weight(1f), "${dsLuong.size} NV")
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TheBaoCao("Lai gop", dinhDangTien(doanhThu - tongLuong), Color(0xFF2E7D32), Modifier.weight(1f), "DT - luong")
            TheBaoCao("Cong no", dinhDangTien(tongNo), Color(0xFFC62828), Modifier.weight(1f), "toan cong ty")
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("So voi thang truoc (${dinhDangTien(doanhThuTruoc)})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                HangTienTrinh(
                    if (chenh >= 0) "Tang ${dinhDangTien(chenh)}" else "Giam ${dinhDangTien(-chenh)}",
                    tyLeTtt.coerceIn(0f, 2f) / 2f,
                    if (chenh >= 0) Color(0xFF2E7D32) else Color(0xFFC62828),
                )
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Bieu do doanh thu theo ngay (thang $thang)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (bieuDo.isEmpty()) {
                    Text("Chua co don hoan thanh trong thang nay.", style = MaterialTheme.typography.bodySmall)
                } else {
                    Row(
                        Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.Bottom,
                    ) {
                        bieuDo.forEachIndexed { idx, cot ->
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
                                        .background(if (cotChon == idx) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.primary)
                                        .clickable { cotChon = if (cotChon == idx) null else idx },
                                )
                                Text(cot.nhan, style = MaterialTheme.typography.labelSmall)
                            }
                        }
                    }
                    if (cotChon != null && cotChon!! in bieuDo.indices) {
                        val cot = bieuDo[cotChon!!]
                        val parts = cot.nhan.split("-")
                        val d1 = parts.getOrNull(0)?.toIntOrNull() ?: 1
                        val d2 = parts.getOrNull(1)?.toIntOrNull() ?: d1
                        val donCot = dsDonThang.filter { it.trangThai == "HOAN_THANH" }.filter {
                            val c = java.util.Calendar.getInstance().apply { timeInMillis = it.ngayTao }
                            c.get(java.util.Calendar.DAY_OF_MONTH) in d1..d2
                        }
                        Text("Ngay ${cot.nhan}: ${donCot.size} don - ${dinhDangTien(cot.tong)}. Bam cot khac de xem.", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        donCot.take(6).forEach { d ->
                            Text("- ${d.maDh}: ${dinhDangTien(d.tongThanhToan)} (${dinhDangNgay(d.ngayTao)})", style = MaterialTheme.typography.bodySmall)
                        }
                    } else {
                        Text("Bam vao tung cot de xem don cau thanh.", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("So doanh thu theo nhom", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AssistChip(onClick = { chonCsvSo.launch("qldvyt-so-doanh-thu-$thang-$nam.csv") }, label = { Text("Xuat CSV") })
                }
                if (theoNhom.isEmpty()) {
                    Text("Chua co so lieu nhom trong thang - hay hoan thanh don.", style = MaterialTheme.typography.bodySmall)
                } else {
                    val tongNhom = theoNhom.sumOf { it.tong }.coerceAtLeast(1L)
                    theoNhom.forEach { n ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(tenNhomSo(n.nhom), fontWeight = FontWeight.SemiBold)
                                    Text("${n.soDon} don", style = MaterialTheme.typography.bodySmall)
                                }
                                Text(dinhDangTien(n.tong), color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                            HangTienTrinh("", n.tong.toFloat() / tongNhom, MaterialTheme.colorScheme.primary)
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                    Text("Tong cong: ${dinhDangTien(theoNhom.sumOf { it.tong })}", fontWeight = FontWeight.Bold)
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Luong & xuat file Excel", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Tong luong thuc linh: ${dinhDangTien(tongLuong)} (${dsLuong.size} NV)")
                Text("Lai gop uoc tinh: ${dinhDangTien(doanhThu - tongLuong)}", color = MaterialTheme.colorScheme.primary)
                Button(onClick = { chonCsvDon.launch("qldvyt-don-$thang-$nam.csv") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Xuat CSV don hang thang nay (${dsDonThang.size} don)")
                }
                Button(onClick = { chonCsvLuong.launch("qldvyt-luong-$thang-$nam.csv") }, modifier = Modifier.fillMaxWidth()) {
                    Text("Xuat CSV bang luong thang nay")
                }
                Text("File CSV mo bang Excel, giu tieng Viet (co BOM).", style = MaterialTheme.typography.bodySmall)
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
