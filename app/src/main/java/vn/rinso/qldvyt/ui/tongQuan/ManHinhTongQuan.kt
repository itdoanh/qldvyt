package vn.rinso.qldvyt.ui.tongQuan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.HangTienTrinh
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.TheCanhBao
import vn.rinso.qldvyt.ui.common.TheHero
import vn.rinso.qldvyt.ui.common.TheSoLieu
import vn.rinso.qldvyt.ui.common.mauTrangThaiDon
import vn.rinso.qldvyt.ui.donHang.tenTrangThaiDon
import vn.rinso.qldvyt.ui.theme.MauChucNang

/** Tổng quan v3 - trung tâm điều hành: hero + sparkline + hàng đợi việc. */
@Composable
fun ManHinhTongQuan(
    onTaoDon: () -> Unit = {},
    onDonHang: () -> Unit = {},
    onNhiemVu: () -> Unit = {},
    onKho: () -> Unit = {},
    onBaoCao: () -> Unit = {},
    onCongNo: () -> Unit = {},
    onNhanVien: () -> Unit = {},
    onMoDon: (String) -> Unit = {},
    onGioiThieu: () -> Unit = {},
    vm: TongQuanViewModel = hiltViewModel(),
) {
    val doanhThuHomNay by vm.doanhThuHomNay.collectAsState()
    val doanhThuThang by vm.doanhThuThang.collectAsState()
    val donCho by vm.donCho.collectAsState()
    val tongNo by vm.tongNo.collectAsState()
    val sapHet by vm.dsSapHet.collectAsState()
    val dsNv by vm.dsNv.collectAsState()
    val donXong by vm.donXongThang.collectAsState()
    val spark by vm.spark7Ngay.collectAsState()
    val triGiaKho by vm.triGiaKho.collectAsState()
    val dsKh by vm.dsKhach.collectAsState()
    val dsViec by vm.dsViec.collectAsState()
    val dsDonMoi by vm.dsDonMoi.collectAsState()
    val mapKh = dsKh.associateBy { it.id }
    val mapNv = dsNv.associateBy { it.id }
    val viecCho = dsViec.count { it.trangThai == "DAGIAO" || it.trangThai == "DANGLAM" }
    val noDon = dsDonMoi.count { it.conNo > 0 && it.trangThai != "HUY" }
    val mucTieu = 50_000_000L
    val tyLeThang = if (mucTieu > 0) doanhThuThang.toFloat() / mucTieu else 0f

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TheHero(
            dongNho = "QUẢN LÝ DỊCH VỤ Y TẾ - RINSO",
            tieuDe = "Tổng quan điều hành",
            dongSoLieu = "Hôm nay: ${dinhDangTien(doanhThuHomNay)} - Tháng: ${dinhDangTien(doanhThuThang)}",
            nutChinh = "+ Tạo đơn",
            onNutChinh = onTaoDon,
            nutPhu = "Báo cáo",
            onNutPhu = onBaoCao,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TheSoLieu("Doanh thu tháng", dinhDangTien(doanhThuThang), Icons.Filled.Payments, MauChucNang.DoanhThu, "${donXong.size} đơn xong", Modifier.weight(1f), spark)
            TheSoLieu("Đơn cần xử lý", "$donCho đơn", Icons.Filled.ReceiptLong, MauChucNang.DonCho, "Chờ + đang làm", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TheSoLieu("Công nợ", dinhDangTien(tongNo), Icons.Filled.Warning, MauChucNang.CongNo, "$noDon đơn còn nợ", Modifier.weight(1f))
            TheSoLieu("Nhân sự", "${dsNv.size} NV", Icons.Filled.Group, MauChucNang.Luong, "$viecCho việc đang chạy", Modifier.weight(1f))
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tiến độ mục tiêu tháng (${dinhDangTien(mucTieu)})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                HangTienTrinh("Doanh thu ${dinhDangTien(doanhThuThang)}", tyLeThang, MauChucNang.DoanhThu)
                Text("Kho trị giá ${dinhDangTien(triGiaKho)} - Khách hàng ${dsKh.size} - NV ${dsNv.size}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Cần làm ngay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (donCho == 0 && noDon == 0 && sapHet.isEmpty() && viecCho == 0) {
                    Text("Tuyệt vời - không còn việc tồn. Tạo đơn mới ngay!", style = MaterialTheme.typography.bodySmall)
                } else {
                    if (donCho > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$donCho đơn chờ/xử lý", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onDonHang) { Text("Mở đơn") }
                    }
                    if (noDon > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$noDon đơn còn nợ (${dinhDangTien(tongNo)})", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onCongNo) { Text("Thu nợ") }
                    }
                    if (viecCho > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$viecCho việc chưa xong", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onNhiemVu) { Text("Giao việc") }
                    }
                    if (sapHet.isNotEmpty()) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kho sắp hết (${sapHet.size}): " + sapHet.take(2).joinToString { it.ten }, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onKho) { Text("Nhập kho") }
                    }
                }
            }
        }
        if (sapHet.isNotEmpty()) {
            TheCanhBao("Sắp hết kho (${sapHet.size})", sapHet.take(3).joinToString { "${it.ten} (còn ${it.tonKho.toInt()} ${it.donVi})" }, "Mở kho nhập hàng", onKho)
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lối tắt nghiệp vụ", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDonHang, modifier = Modifier.weight(1f)) { Text("Đơn hàng") }
                    OutlinedButton(onClick = onNhiemVu, modifier = Modifier.weight(1f)) { Text("Giao việc 30%") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNhanVien, modifier = Modifier.weight(1f)) { Text("Chấm công") }
                    OutlinedButton(onClick = onCongNo, modifier = Modifier.weight(1f)) { Text("Thu nợ") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onKho, modifier = Modifier.weight(1f)) { Text("Kho") }
                    OutlinedButton(onClick = onGioiThieu, modifier = Modifier.weight(1f)) { Text("Giới thiệu") }
                }
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Đơn mới nhất", fontWeight = FontWeight.Bold)
                    Text("${dsDonMoi.size} đơn", style = MaterialTheme.typography.bodySmall)
                }
                if (dsDonMoi.isEmpty()) {
                    Text("Chưa có đơn - hãy tạo đơn rồi bấm Hoàn thành.", style = MaterialTheme.typography.bodySmall)
                } else {
                    dsDonMoi.take(5).forEach { don ->
                        val tenKh = mapKh[don.khachHangId]?.hoTen ?: "KH lẻ"
                        Card(onClick = { onMoDon(don.id) }) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text("${don.maDh} - $tenKh", fontWeight = FontWeight.SemiBold)
                                    Text(dinhDangNgay(don.ngayTao) + " - " + dinhDangTien(don.tongThanhToan), style = MaterialTheme.typography.bodySmall)
                                    if (don.conNo > 0) Text("Còn nợ: ${dinhDangTien(don.conNo)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                                NhanTrangThai(don.trangThai, tenTrangThaiDon(don.trangThai), mauTrangThaiDon(don.trangThai))
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onBaoCao, modifier = Modifier.fillMaxWidth()) { Text("Xem báo cáo tổng hợp") }
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ca làm đợt này", fontWeight = FontWeight.Bold)
                    Text("${dsViec.count { it.trangThai == "DANGLAM" }} đang làm", style = MaterialTheme.typography.bodySmall)
                }
                val dot = dsViec.take(4)
                if (dot.isEmpty()) Text("Chưa giao việc nào - qua tab Việc để giao.", style = MaterialTheme.typography.bodySmall)
                else dot.forEach { v ->
                    val tenNv = mapNv[v.nhanVienId]?.hoTen ?: "?"
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${v.tenCongViec} - $tenNv", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = onNhiemVu) { Text("Mở") }
                    }
                }
            }
        }
    }
}
