package vn.rinso.qldvyt.ui.baoCao

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.HangTienTrinh

/** Màn hình Báo cáo tổng hợp: KPI + biểu đồ đẹp từng ngày + xu hướng + cơ cấu + lương + CSV. */
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
    val xuHuong by vm.xuHuong6Thang.collectAsState()

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
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text("Chỉ số tháng $thang", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ChipNho("Trước VAT: ${dinhDangTien(tk.truocVat)}")
                    ChipNho("VAT: ${dinhDangTien(tk.tienVat)}")
                }
                Text("Chờ xử lý: ${tk.soDonCho} • Đang làm: ${tk.soDonDangLam} • Hủy: ${tk.soDonHuy}")
                if (tk.ngayCaoNhat > 0) Text("Ngày cao nhất: ngày ${tk.ngayCaoNhat} — ${dinhDangTien(tk.doanhThuCaoNhat)}", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)
                DonutTrangThai(cho = tk.soDonCho, dangLam = tk.soDonDangLam, xong = tk.soDonHoanThanh, huy = tk.soDonHuy)
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
                    Text("Xu hướng 6 tháng", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    Text("Đường doanh thu", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                }
                if (xuHuong.all { it.tong == 0L }) {
                    Text("Chưa có số liệu 6 tháng gần nhất.", style = MaterialTheme.typography.bodySmall)
                } else {
                    BieuDoDuong(xuHuong.map { it.tong }, xuHuong.map { "T${it.nhan}" })
                    Text(
                        "Cao nhất: ${dinhDangTien(xuHuong.maxOf { it.tong })} • Tháng này: ${dinhDangTien(xuHuong.lastOrNull()?.tong ?: 0L)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.SemiBold,
                    )
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Doanh thu từng ngày (tháng $thang)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AssistChip(onClick = { chonCsvNgay.launch("qldvyt-doanh-thu-ngay-$thang-$nam.csv") }, label = { Text("Xuất CSV") })
                }
                Text("Cột gradient càng cao thu càng nhiều. Chạm để xem đơn của ngày đó.", style = MaterialTheme.typography.bodySmall)
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
                            val caoAnim by animateFloatAsState(targetValue = 26f + tyLe * 118f, label = "cao")
                            val dangChon = ngayChon == cot.ngay
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    if (cot.tong <= 0) "" else if (cot.tong >= 1_000_000L) "${cot.tong / 1_000_000L}tr" else "${cot.tong / 1_000L}k",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (dangChon) FontWeight.Bold else FontWeight.Normal,
                                    color = if (dangChon) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                                )
                                Box(
                                    Modifier
                                        .width(if (dangChon) 36.dp else 28.dp)
                                        .height(if (cot.tong <= 0) 10.dp else caoAnim.dp)
                                        .clip(RoundedCornerShape(topStart = 10.dp, topEnd = 10.dp, bottomStart = 4.dp, bottomEnd = 4.dp))
                                        .background(
                                            when {
                                                dangChon -> Brush.verticalGradient(listOf(Color(0xFF7B1FA2), Color(0xFFCE93D8)))
                                                cot.tong <= 0 -> Brush.verticalGradient(listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant))
                                                else -> Brush.verticalGradient(listOf(Color(0xFF00695C), Color(0xFF4DB6AC)))
                                            }
                                        )
                                        .clickable { vm.ngayChon.value = if (dangChon) null else cot.ngay },
                                )
                                Box(
                                    Modifier.width(if (dangChon) 36.dp else 28.dp).height(3.dp)
                                        .clip(RoundedCornerShape(99.dp))
                                        .background(if (dangChon) MaterialTheme.colorScheme.tertiary else Color.Transparent)
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
                    Text("Cơ cấu doanh thu theo nhóm", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                    AssistChip(onClick = { chonCsvSo.launch("qldvyt-so-doanh-thu-$thang-$nam.csv") }, label = { Text("Xuất CSV") })
                }
                if (theoNhom.isEmpty()) {
                    Text("Chưa có số liệu nhóm trong tháng — hãy hoàn thành đơn.", style = MaterialTheme.typography.bodySmall)
                } else {
                    val tongNhom = theoNhom.sumOf { it.tong }.coerceAtLeast(1L)
                    BieuDoTron(theoNhom.map { it.tong }, theoNhom.map { tenNhomSo(it.nhom) })
                    theoNhom.forEach { n ->
                        Column {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(tenNhomSo(n.nhom), fontWeight = FontWeight.SemiBold)
                                    Text("${n.soDon} đơn • ${(n.tong * 100 / tongNhom)}%", style = MaterialTheme.typography.bodySmall)
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
                BieuDoCotDoi(doanhThu, tongLuong)
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
private fun ChipNho(text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
        Text(text, Modifier.padding(horizontal = 10.dp, vertical = 6.dp), style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
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

/** Biểu đồ đường xu hướng: nền gradient + điểm tròn + lưới mờ. */
@Composable
private fun BieuDoDuong(giaTri: List<Long>, nhan: List<String>) {
    val max = (giaTri.maxOrNull() ?: 0L).coerceAtLeast(1L).toFloat()
    val min = (giaTri.minOrNull() ?: 0L).toFloat()
    val bien = (max - min).coerceAtLeast(1f)
    val mau = Color(0xFF00796B)
    Column {
        Canvas(Modifier.fillMaxWidth().height(150.dp).clip(RoundedCornerShape(16.dp)).background(Color(0xFF00796B).copy(alpha = 0.06f))) {
            val w = size.width; val h = size.height - 24f
            // lưới ngang
            repeat(4) { i ->
                val y = h * (i + 1) / 5
                drawLine(Color.Gray.copy(alpha = 0.25f), Offset(0f, y), Offset(w, y), strokeWidth = 1f)
            }
            if (giaTri.size < 2) return@Canvas
            val stepX = w / (giaTri.size - 1)
            val pts = giaTri.mapIndexed { i, v -> Offset(i * stepX, h - ((v - min) / bien) * (h - 16f) - 8f) }
            // vùng tô dưới đường
            val path = Path().apply {
                moveTo(pts.first().x, h)
                pts.forEach { lineTo(it.x, it.y) }
                lineTo(pts.last().x, h); close()
            }
            drawPath(path, Brush.verticalGradient(listOf(mau.copy(alpha = 0.35f), mau.copy(alpha = 0.02f))))
            // đường chính
            val linePath = Path().apply { moveTo(pts.first().x, pts.first().y); pts.drop(1).forEach { lineTo(it.x, it.y) } }
            drawPath(linePath, mau, style = Stroke(width = 6f))
            pts.forEach { drawCircle(Color.White, 10f, it); drawCircle(mau, 7f, it) }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            nhan.forEach { Text(it, style = MaterialTheme.typography.labelSmall) }
        }
    }
}

/** Biểu đồ tròn cơ cấu: nhiều màu + chú thích. */
@Composable
private fun BieuDoTron(giaTri: List<Long>, nhan: List<String>) {
    val tong = giaTri.sum().coerceAtLeast(1L).toFloat()
    val mauSac = listOf(Color(0xFF00897B), Color(0xFF5C6BC0), Color(0xFFFFA000), Color(0xFFEF5350), Color(0xFF8E24AA), Color(0xFF6D4C41))
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(Modifier.width(120.dp).height(120.dp)) {
            var goc = -90f
            giaTri.forEachIndexed { i, v ->
                val gocQuet = (v / tong) * 360f
                drawArc(mauSac[i % mauSac.size], goc, gocQuet, true, Offset.Zero, Size(size.width, size.height))
                goc += gocQuet
            }
            drawCircle(Color.White, size.minDimension / 5, center = center)
        }
        Column(verticalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.weight(1f)) {
            giaTri.forEachIndexed { i, v ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.width(12.dp).height(12.dp).clip(RoundedCornerShape(4.dp)).background(mauSac[i % mauSac.size]))
                    Text("${nhan.getOrNull(i) ?: ""} — ${(v * 100 / tong.toLong())}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

/** Donut trạng thái đơn: chờ / đang làm / xong / hủy. */
@Composable
private fun DonutTrangThai(cho: Int, dangLam: Int, xong: Int, huy: Int) {
    val tong = (cho + dangLam + xong + huy).coerceAtLeast(1)
    val duLieu = listOf(cho to Color(0xFF1565C0), dangLam to Color(0xFFEF6C00), xong to Color(0xFF2E7D32), huy to Color(0xFF9E9E9E))
    val nhan = listOf("Chờ $cho", "Đang làm $dangLam", "Xong $xong", "Hủy $huy")
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        Canvas(Modifier.width(96.dp).height(96.dp)) {
            var goc = -90f
            duLieu.forEach { (sl, _) ->
                val quet = sl.toFloat() / tong * 360f
                if (quet > 0) drawArc(color = Color.Transparent, startAngle = 0f, sweepAngle = 0f, useCenter = true)
                goc += quet
            }
            var g2 = -90f
            duLieu.forEach { (sl, mau) ->
                val quet = sl.toFloat() / tong * 360f
                if (quet > 0) drawArc(mau, g2, quet, true, Offset.Zero, Size(size.width, size.height))
                g2 += quet
            }
            drawCircle(Color.White, size.minDimension / 3.2f, center = center)
        }
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            nhan.forEachIndexed { i, s ->
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Box(Modifier.width(10.dp).height(10.dp).clip(RoundedCornerShape(99.dp)).background(duLieu[i].second))
                    Text(s, style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

/** Cột đôi doanh thu vs lương. */
@Composable
private fun BieuDoCotDoi(doanhThu: Long, luong: Long) {
    val max = maxOf(doanhThu, luong, 1L).toFloat()
    Row(Modifier.fillMaxWidth().height(110.dp), horizontalArrangement = Arrangement.spacedBy(16.dp), verticalAlignment = Alignment.Bottom) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(dinhDangTien(doanhThu), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Box(
                Modifier.fillMaxWidth().height((20 + doanhThu / max * 70).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF00796B), Color(0xFF80CBC4))))
            )
            Text("Doanh thu", style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            Text(dinhDangTien(luong), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Box(
                Modifier.fillMaxWidth().height((20 + luong / max * 70).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF1565C0), Color(0xFF90CAF9))))
            )
            Text("Lương", style = MaterialTheme.typography.bodySmall)
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
            val lai = doanhThu - luong
            Text(dinhDangTien(lai), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Box(
                Modifier.fillMaxWidth().height((20 + (if (lai > 0) lai / max * 70 else 4.0).toFloat()).dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Brush.verticalGradient(listOf(Color(0xFF2E7D32), Color(0xFFA5D6A7))))
            )
            Text("Lãi gộp", style = MaterialTheme.typography.bodySmall)
        }
    }
}
