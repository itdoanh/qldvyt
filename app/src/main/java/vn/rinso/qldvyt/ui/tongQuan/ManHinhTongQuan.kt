package vn.rinso.qldvyt.ui.tongQuan

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Task
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.TheSoLieu
import vn.rinso.qldvyt.ui.common.mauTrangThaiDon
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.donHang.tenTrangThaiDon

/** Tổng quan kiểu MISA: header gradient + lưới số liệu + cảnh báo + lối tắt + đơn mới nhất. */
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

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF00796B), Color(0xFF26A69A), Color(0xFF00838F))))
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("QUẢN LÝ DỊCH VỤ Y TẾ • RINSO", color = Color.White, style = MaterialTheme.typography.labelLarge)
                Text("Tổng quan điều hành", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Hôm nay: ${dinhDangTien(doanhThuHomNay)} • Tháng: ${dinhDangTien(doanhThuThang)}", color = Color.White)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onTaoDon) { Text("+ Tạo đơn") }
                    OutlinedButton(onClick = onBaoCao) { Text("Báo cáo") }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TheSoLieu("Doanh thu tháng", dinhDangTien(doanhThuThang), Icons.Filled.Payments, Color(0xFF00796B), "${donXong.size} đơn xong", Modifier.weight(1f))
            TheSoLieu("Đơn cần xử lý", "$donCho đơn", Icons.Filled.ReceiptLong, Color(0xFFEF6C00), "Chờ + đang làm", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TheSoLieu("Công nợ", dinhDangTien(tongNo), Icons.Filled.Warning, Color(0xFFC62828), "Bấm để thu nợ", Modifier.weight(1f))
            TheSoLieu("Nhân sự", "${dsNv.size} NV", Icons.Filled.Group, Color(0xFF1565C0), "Đang làm việc", Modifier.weight(1f))
        }

        if (sapHet.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("⚠ Sắp hết kho (${sapHet.size})", fontWeight = FontWeight.Bold)
                    Text(sapHet.take(3).joinToString { "${it.ten} (còn ${it.tonKho.toInt()} ${it.donVi})" })
                    OutlinedButton(onClick = onKho) { Text("Mở kho nhập hàng") }
                }
            }
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
                    Text("Đơn hoàn thành gần nhất", fontWeight = FontWeight.Bold)
                    Text("${donXong.size} đơn", style = MaterialTheme.typography.bodySmall)
                }
                if (donXong.isEmpty()) {
                    Text("Chưa có đơn hoàn thành trong tháng — hãy tạo đơn rồi bấm Hoàn thành.", style = MaterialTheme.typography.bodySmall)
                } else {
                    donXong.take(5).forEach { don ->
                        Card(onClick = { onMoDon(don.id) }) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text(don.maDh, fontWeight = FontWeight.SemiBold)
                                    Text(dinhDangNgay(don.ngayTao) + " • " + dinhDangTien(don.tongThanhToan), style = MaterialTheme.typography.bodySmall)
                                }
                                NhanTrangThai(don.trangThai, tenTrangThaiDon(don.trangThai), mauTrangThaiDon(don.trangThai))
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onBaoCao, modifier = Modifier.fillMaxWidth()) {
                    Text("Xem báo cáo tổng hợp (biểu đồ + sổ nhóm)")
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nhiệm vụ hưởng 30% • Vật tư trừ kho tự động • Lương theo công", style = MaterialTheme.typography.bodySmall)
                Text("Dữ liệu lưu trên máy, backup file .zip — không cần server.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

