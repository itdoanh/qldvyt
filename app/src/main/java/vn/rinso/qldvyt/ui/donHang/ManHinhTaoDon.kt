package vn.rinso.qldvyt.ui.donHang

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.data.repo.MatHangMoi
import vn.rinso.qldvyt.data.repo.VatTuDungMoi
import vn.rinso.qldvyt.ui.common.OTimKiem

/** Wizard tạo đơn 3 bước kiểu MISA: chọn khách → chọn DV/vật tư → xác nhận. GD1. */
@Composable
fun ManHinhTaoDon(
    vm: DonHangViewModel = hiltViewModel(),
    onXong: () -> Unit = {},
) {
    var buoc by remember { mutableStateOf(1) }
    val dsKhach by vm.dsKhach.collectAsState()
    val dsDv by vm.dsDichVu.collectAsState()
    val dsGoi by vm.dsGoi.collectAsState()
    val dsVt by vm.dsVatTu.collectAsState()
    val khachChon by vm.khachDaChon.collectAsState()
    val mhChon by vm.matHangDaChon.collectAsState()
    val vtChon by vm.vatTuDaChon.collectAsState()
    val tb by vm.thongBao.collectAsState()
    val vatBatHt by vm.vatBat.collectAsState()
    val vatApDon by vm.vatApDungDon.collectAsState()
    val vatTheoDv by vm.vatTheoDv.collectAsState()
    val tyLeVatMm by vm.vatMacDinh.collectAsState()
    var oGiam by remember { mutableStateOf("") }
    var tkKh by remember { mutableStateOf("") }
    var tkDv by remember { mutableStateOf("") }
    var tkVt by remember { mutableStateOf("") }
    var laVangLai by remember { mutableStateOf(false) }
    var tenVangLai by remember { mutableStateOf("") }
    var sdtVangLai by remember { mutableStateOf("") }

    val tongHang = mhChon.sumOf { (it.soLuong * it.donGia).toLong() }
    val giam = oGiam.toLongOrNull() ?: 0L
    val truocVat = maxOf(0L, tongHang - giam)
    val vatBatMm = vatBatHt && vatApDon
    val tienVatMm = if (!vatBatMm) 0L else if (vatTheoDv) {
        mhChon.sumOf { m ->
            if (!m.chiuThue) 0L else {
                val thanh = (m.soLuong * m.donGia).toLong()
                val tyTrong = if (tongHang > 0) thanh.toDouble() / tongHang else 0.0
                val giamDong = (giam * tyTrong).toLong()
                ((maxOf(0L, thanh - giamDong)) * tyLeVatMm).toLong()
            }
        }
    } else (truocVat * tyLeVatMm).toLong()
    val thanhToan = truocVat + tienVatMm
    val khLoc = dsKhach.filter { tkKh.isBlank() || it.hoTen.contains(tkKh.trim(), true) || it.sdt.contains(tkKh.trim()) }
    fun khongDau(s: String): String = try {
        java.text.Normalizer.normalize(s, java.text.Normalizer.Form.NFD)
            .replace("\\p{M}".toRegex(), "").lowercase().replace('đ', 'd').replace('Đ', 'd')
    } catch (_: Exception) { s.lowercase() }
    fun khopTuongDoi(ten: String, ma: String, tuKhoa: String): Boolean {
        val tk = tuKhoa.trim(); if (tk.isBlank()) return true
        val tenKD = khongDau(ten); val maKD = khongDau(ma); val tkKD = khongDau(tk)
        // Từng từ trong từ khóa đều phải xuất hiện (search tương đối cho kho lớn)
        return tkKD.split("\\s+".toRegex()).all { tu -> tenKD.contains(tu) || maKD.contains(tu) } ||
            ten.contains(tk.trim(), true) || ma.contains(tk.trim(), true)
    }
    val dvLoc = dsDv.filter { khopTuongDoi(it.ten, it.maDv, tkDv) }
    val goiLoc = dsGoi.filter { khopTuongDoi(it.ten, it.maGoi, tkDv) }
    val mapVt = dsVt.associateBy { it.id }
    val vtLoc = dsVt.filter { khopTuongDoi(it.ten, it.maVt, tkVt) }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Tạo đơn mới", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        LinearProgressIndicator(progress = { buoc / 3f }, modifier = Modifier.fillMaxWidth())
        Text(
            when (buoc) { 1 -> "Bước 1/3: chọn khách hàng"; 2 -> "Bước 2/3: chọn dịch vụ + vật tư"; else -> "Bước 3/3: xác nhận + giảm giá" },
            style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold,
        )
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }

        if (buoc == 1) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(onClick = { laVangLai = false }, label = { Text((if (!laVangLai) "✓ " else "") + "Khách có hồ sơ") })
                AssistChip(onClick = { laVangLai = true }, label = { Text((if (laVangLai) "✓ " else "") + "Khách vãng lai") })
            }
            if (laVangLai) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Khách vãng lai — không cần tạo hồ sơ trước, chỉ ghi tên:", fontWeight = FontWeight.Bold)
                        OutlinedTextField(tenVangLai, { tenVangLai = it }, label = { Text("Tên khách (*)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(sdtVangLai, { sdtVangLai = it.filter(Char::isDigit) }, label = { Text("SĐT (nếu có)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    }
                }
                Spacer(Modifier.weight(1f))
            } else {
            OTimKiem(tkKh, { tkKh = it }, "Tìm khách theo tên / SĐT...")
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(khLoc.take(30), key = { it.id }) { kh ->
                    val chon = khachChon == kh.id
                    Card(
                        onClick = { vm.chonKhach(kh.id) },
                        colors = if (chon) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                        else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    ) {
                        Column(Modifier.padding(12.dp).fillMaxWidth()) {
                            Text((if (chon) "✓ " else "") + kh.hoTen, fontWeight = FontWeight.Bold)
                            Text("${kh.maKh} • SĐT: ${kh.sdt.ifEmpty { "—" }}", style = MaterialTheme.typography.bodySmall)
                            if (kh.diaChi.isNotEmpty()) Text(kh.diaChi, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
            }
            }
            val duocTiep = if (laVangLai) tenVangLai.isNotBlank() else khachChon != null
            Button(onClick = { buoc = 2 }, enabled = duocTiep, modifier = Modifier.fillMaxWidth()) {
                Text("Tiếp tục → chọn dịch vụ")
            }
        } else if (buoc == 2) {
            OTimKiem(tkDv, { tkDv = it }, "Tìm dịch vụ / gói...")
            // B2: danh sách đã thêm — tên gói/DV + số lượng + đơn giá + điều chỉnh/Xóa
            if (mhChon.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Gói / dịch vụ đã thêm (${mhChon.size}):", fontWeight = FontWeight.Bold)
                        mhChon.forEachIndexed { i, m ->
                            val laGoi = m.goiId != null
                            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Text((if (laGoi) "📦 " else "💉 ") + m.tenHienThi, fontWeight = FontWeight.SemiBold)
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    OutlinedButton(onClick = { vm.doiSoLuongMatHang(i, -1.0) }) { Text("−") }
                                    AssistChip(onClick = {}, label = { Text("× ${m.soLuong.toInt()}") })
                                    OutlinedButton(onClick = { vm.doiSoLuongMatHang(i, 1.0) }) { Text("+") }
                                    OutlinedButton(onClick = { vm.xoaMatHang(i) }) { Text("Xóa") }
                                }
                                Text("${dinhDangTien(m.donGia)} × ${m.soLuong.toInt()} = ${dinhDangTien((m.soLuong * m.donGia).toLong())}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                items(dvLoc, key = { it.id }) { dv ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(dv.ten, fontWeight = FontWeight.SemiBold)
                            Text(dinhDangTien(dv.giaNiemYet) + " / " + dv.donVi, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(onClick = { vm.themMatHang(MatHangMoi(dichVuId = dv.id, tenHienThi = dv.ten, soLuong = 1.0, donGia = dv.giaNiemYet, chiuThue = dv.chiuThue)) }) { Text("+ Thêm") }
                    }
                }
                items(goiLoc, key = { it.id }) { goi ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(goi.ten + " (GÓI)", fontWeight = FontWeight.SemiBold)
                            Text(dinhDangTien(goi.giaGoi), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(onClick = { vm.themMatHang(MatHangMoi(goiId = goi.id, tenHienThi = goi.ten, soLuong = 1.0, donGia = goi.giaGoi)) }) { Text("+ Thêm") }
                    }
                }
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text("Đã chọn ${mhChon.size} mặt hàng • Tổng: ${dinhDangTien(tongHang)}", Modifier.padding(12.dp), fontWeight = FontWeight.Bold)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { buoc = 1 }, modifier = Modifier.weight(1f)) { Text("← Quay lại") }
                Button(onClick = { buoc = 3 }, enabled = mhChon.isNotEmpty(), modifier = Modifier.weight(1f)) { Text("Tiếp tục →") }
            }
        } else {
            Column(Modifier.weight(1f).verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Mặt hàng đã chọn:", fontWeight = FontWeight.Bold)
                mhChon.forEachIndexed { i, m ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${m.tenHienThi} × ${m.soLuong.toInt()} = ${dinhDangTien((m.soLuong * m.donGia).toLong())}", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = { vm.xoaMatHang(i) }) { Text("Xóa") }
                    }
                }
                OutlinedTextField(oGiam, { oGiam = it.filter(Char::isDigit) }, label = { Text("Giảm giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Text("Vật tư dùng cho đơn (trừ tồn kho):", fontWeight = FontWeight.Bold)
                OTimKiem(tkVt, { tkVt = it }, "Tìm vật tư theo tên / mã (gõ không dấu vẫn ra)...")
                // B3: vật tư đã gắn — tên + SL + đơn giá vốn + thành tiền + tăng/giảm/Xóa
                if (vtChon.isNotEmpty()) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                        Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Vật tư đã thêm (${vtChon.size}):", fontWeight = FontWeight.Bold)
                            vtChon.forEachIndexed { i, x ->
                                val vt = mapVt[x.vatTuId]
                                val tenVt = vt?.ten ?: "Vật tư"
                                val von = vt?.giaNhapTB ?: 0L
                                Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                    Text("🧰 $tenVt", fontWeight = FontWeight.SemiBold)
                                    Text("SL ${x.soLuong.toInt()} ${vt?.donVi ?: ""} × ${dinhDangTien(von)} = ${dinhDangTien((x.soLuong * von).toLong())}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        OutlinedButton(onClick = { vm.doiSoLuongVatTu(i, -1.0) }) { Text("−") }
                                        OutlinedButton(onClick = { vm.doiSoLuongVatTu(i, 1.0) }) { Text("+") }
                                        OutlinedButton(onClick = { vm.xoaVatTu(i) }) { Text("Xóa") }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("Chưa gắn vật tư nào (không bắt buộc).", style = MaterialTheme.typography.bodySmall)
                }
                Text("Chọn thêm từ kho (${vtLoc.size} khớp):", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                vtLoc.take(30).forEach { vt ->
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Column(Modifier.weight(1f)) {
                            Text(vt.ten, fontWeight = FontWeight.SemiBold)
                            Text("${vt.maVt} • Tồn ${vt.tonKho.toInt()} ${vt.donVi} • Vốn ${dinhDangTien(vt.giaNhapTB)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                        }
                        OutlinedButton(onClick = { vm.themVatTu(VatTuDungMoi(vt.id, 1.0)) }) { Text("+ Thêm") }
                    }
                }
                if (vtLoc.size > 30) Text("... còn ${vtLoc.size - 30} vật tư nữa — thu hẹp từ khóa để tìm.", style = MaterialTheme.typography.bodySmall)
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Tổng hàng: ${dinhDangTien(tongHang)}")
                        Text("Giảm: ${dinhDangTien(giam)}")
                        Text("Trước VAT: ${dinhDangTien(truocVat)}")
                        if (vatBatMm) Text("VAT ${(tyLeVatMm * 100).toInt()}%: ${dinhDangTien(tienVatMm)}", fontWeight = FontWeight.SemiBold)
                        else Text("VAT: Không áp dụng (tắt ở Cài đặt)", style = MaterialTheme.typography.bodySmall)
                        Text("Thanh toán: ${dinhDangTien(thanhToan)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { buoc = 2 }, modifier = Modifier.weight(1f)) { Text("← Quay lại") }
                Button(
                    onClick = {
                        vm.giamGia.value = giam
                        if (laVangLai) vm.taoDonVangLai(tenVangLai, sdtVangLai) { onXong() }
                        else vm.taoDon { onXong() }
                    },
                    enabled = mhChon.isNotEmpty() && (!laVangLai || tenVangLai.isNotBlank()),
                    modifier = Modifier.weight(1f),
                ) { Text(if (laVangLai) "Tạo đơn vãng lai" else "Tạo đơn") }
            }
        }
    }
}

