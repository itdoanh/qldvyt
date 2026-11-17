package vn.rinso.qldvyt.ui.tongQuan

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien

/** Màn Tổng quan kiểu MISA: gradient header + thẻ số + cảnh báo + lối tắt. */
@Composable
fun ManHinhTongQuan(
    onGioiThieu: () -> Unit = {},
    onBaoCao: () -> Unit = {},
    onDonHang: () -> Unit = {},
    vm: TongQuanViewModel = hiltViewModel(),
) {
    val doanhThuHomNay by vm.doanhThuHomNay.collectAsState()
    val doanhThuThang by vm.doanhThuThang.collectAsState()
    val donCho by vm.donCho.collectAsState()
    val tongNo by vm.tongNo.collectAsState()
    val sapHet by vm.dsSapHet.collectAsState()
    val dsNv by vm.dsNv.collectAsState()

    val tyLeThang = (doanhThuHomNay.toFloat() / maxOf(1L, doanhThuThang).toFloat()).coerceIn(0f, 1f)
    val tyLeAnim by animateFloatAsState(targetValue = tyLeThang, label = "tile")

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        // Header gradient
        Box(
            Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp))
                .background(Brush.horizontalGradient(listOf(Color(0xFF00796B), Color(0xFF26A69A))))
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("QUẢN LÝ DỊCH VỤ Y TẾ", color = Color.White, style = MaterialTheme.typography.labelLarge)
                Text("Tổng quan hôm nay", color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text("Doanh thu tháng: ${dinhDangTien(doanhThuThang)}", color = Color.White)
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(progress = { tyLeAnim }, modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(99.dp)))
                Text("Hôm nay đóng góp ${(tyLeThang * 100).toInt()}% của tháng", color = Color.White, style = MaterialTheme.typography.bodySmall)
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TheTongQuan("Doanh thu hôm nay", dinhDangTien(doanhThuHomNay), Icons.Filled.ShoppingCart, Color(0xFF00796B), Modifier.weight(1f))
            TheTongQuan("Đơn cần xử lý", "$donCho đơn", Icons.Filled.Info, Color(0xFFEF6C00), Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TheTongQuan("Công nợ", dinhDangTien(tongNo), Icons.Filled.Warning, Color(0xFFC62828), Modifier.weight(1f))
            TheTongQuan("Nhân sự", "${dsNv.size} NV", Icons.Filled.Person, Color(0xFF1565C0), Modifier.weight(1f))
        }

        if (sapHet.isNotEmpty()) {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.Warning, contentDescription = null)
                    Text("⚠ Sắp hết kho: ${sapHet.take(3).joinToString { "${it.ten} (${it.tonKho.toInt()} ${it.donVi})" }}", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))) {
                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = Color(0xFF2E7D32))
                    Text("Kho ổn định — không có vật tư dưới tồn tối thiểu.")
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Lối tắt điều hành", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(onClick = onBaoCao, modifier = Modifier.weight(1f)) { Text("Báo cáo tổng hợp") }
                    Button(onClick = onDonHang, modifier = Modifier.weight(1f)) { Text("Tạo đơn nhanh") }
                }
                Text("Đơn hàng • nhiệm vụ NV hưởng 30% • vật tư • lương • công nợ — lưu local, backup file.", style = MaterialTheme.typography.bodySmall)
                Button(onClick = onGioiThieu) { Text("Giới thiệu Rinso") }
            }
        }
    }
}

@Composable
private fun TheTongQuan(tieuDe: String, giaTri: String, icon: ImageVector, mau: Color, modifier: Modifier = Modifier) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = mau.copy(alpha = 0.10f))) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Box(Modifier.size(36.dp).clip(CircleShape).background(mau.copy(alpha = 0.18f)), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = mau)
            }
            Text(tieuDe, style = MaterialTheme.typography.labelLarge)
            Text(giaTri, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = mau)
        }
    }
}
