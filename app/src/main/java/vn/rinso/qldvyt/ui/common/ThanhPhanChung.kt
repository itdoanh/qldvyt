package vn.rinso.qldvyt.ui.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp

/** Thẻ trạng thái nhỏ kiểu MISA: màu theo mã. */
@Composable
fun NhanTrangThai(ma: String, nhan: String, mau: Color) {
    Surface(
        shape = RoundedCornerShape(99.dp),
        color = mau.copy(alpha = 0.14f),
    ) {
        Text(
            nhan,
            Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = mau,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

fun mauTrangThaiDon(tt: String): Color = when (tt) {
    "HOAN_THANH" -> Color(0xFF2E7D32)
    "DANG_LAM" -> Color(0xFFEF6C00)
    "HUY" -> Color(0xFF9E9E9E)
    else -> Color(0xFF1565C0)
}

fun mauTrangThaiViec(tt: String): Color = when (tt) {
    "HOANTHANH" -> Color(0xFF2E7D32)
    "DANGLAM" -> Color(0xFFEF6C00)
    "HUY" -> Color(0xFF9E9E9E)
    else -> Color(0xFF1565C0)
}

/** Ô tìm kiếm dùng chung mọi màn hình. */
@Composable
fun OTimKiem(giaTri: String, doiGiaTri: (String) -> Unit, goiY: String = "Tìm kiếm...") {
    OutlinedTextField(
        value = giaTri,
        onValueChange = doiGiaTri,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text(goiY) },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
    )
}

/** Trạng thái trống dùng chung: icon + mô tả + nút. */
@Composable
fun OTrong(tieuDe: String, moTa: String = "", icon: ImageVector? = null) {
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(20.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("📦", style = MaterialTheme.typography.headlineMedium)
            Text(tieuDe, fontWeight = FontWeight.Bold)
            if (moTa.isNotEmpty()) Text(moTa, style = MaterialTheme.typography.bodySmall)
        }
    }
}

/** Hero banner điều hành: gradient + tiêu đề + số liệu nổi bật + 2 nút. */
@Composable
fun TheHero(
    dongNho: String,
    tieuDe: String,
    dongSoLieu: String,
    nutChinh: String,
    onNutChinh: () -> Unit,
    nutPhu: String = "",
    onNutPhu: () -> Unit = {},
) {
    Card(shape = RoundedCornerShape(24.dp)) {
        Box(
            Modifier.fillMaxWidth()
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF00796B), Color(0xFF26A69A), Color(0xFF00838F))
                    )
                )
                .padding(20.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(dongNho, color = Color.White, style = MaterialTheme.typography.labelLarge)
                Text(tieuDe, color = Color.White, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                Text(dongSoLieu, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    androidx.compose.material3.Button(onClick = onNutChinh) { Text(nutChinh) }
                    if (nutPhu.isNotEmpty()) androidx.compose.material3.OutlinedButton(onClick = onNutPhu) { Text(nutPhu) }
                }
            }
        }
    }
}

/** Hàng tiến độ: nhãn + % + thanh. */
@Composable
fun HangTienTrinh(nhan: String, tyLe: Float, mau: Color = MaterialTheme.colorScheme.primary) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(nhan, style = MaterialTheme.typography.bodySmall)
            Text("${(tyLe.coerceIn(0f, 1f) * 100).toInt()}%", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = mau)
        }
        androidx.compose.material3.LinearProgressIndicator(
            progress = { tyLe.coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(99.dp)),
            color = mau,
        )
    }
}

/** Dòng diễn giải công thức tiền (bấm mở chi tiết ở màn Lương). */
@Composable
fun DuongDanCongThuc(nhan: String, giaTri: String, mau: Color = MaterialTheme.colorScheme.primary) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(nhan, style = MaterialTheme.typography.bodySmall, modifier = Modifier.weight(1f))
        Text(giaTri, fontWeight = FontWeight.SemiBold, color = mau, style = MaterialTheme.typography.bodyMedium)
    }
}

/** Thẻ cảnh báo dùng chung (kho hết / nợ quá hạn / việc trễ). */
@Composable
fun TheCanhBao(tieuDe: String, moTa: String, nut: String = "", onNut: () -> Unit = {}) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(tieuDe, fontWeight = FontWeight.Bold)
            Text(moTa, style = MaterialTheme.typography.bodySmall)
            if (nut.isNotEmpty()) androidx.compose.material3.OutlinedButton(onClick = onNut) { Text(nut) }
        }
    }
}

/** Sparkline mini (không thêm lib): vẽ đường gấp khúc từ danh sách số. */
@Composable
fun Sparkline(ds: List<Long>, mau: Color = MaterialTheme.colorScheme.primary, modifier: Modifier = Modifier.fillMaxWidth().height(36.dp)) {
    if (ds.isEmpty()) return
    val max = (ds.maxOrNull() ?: 0L).coerceAtLeast(1L).toFloat()
    val min = (ds.minOrNull() ?: 0L).toFloat()
    val bien = (max - min).coerceAtLeast(1f)
    androidx.compose.foundation.Canvas(modifier = modifier) {
        val stepX = if (ds.size <= 1) 0f else size.width / (ds.size - 1)
        var px = 0f
        var py = size.height - ((ds[0] - min) / bien) * size.height
        for (i in 1 until ds.size) {
            val x = i * stepX
            val y = size.height - ((ds[i] - min) / bien) * size.height
            drawLine(mau, androidx.compose.ui.geometry.Offset(px, py), androidx.compose.ui.geometry.Offset(x, y), strokeWidth = 5f)
            px = x; py = y
        }
        ds.forEachIndexed { i, v ->
            val x = i * stepX
            val y = size.height - ((v - min) / bien) * size.height
            drawCircle(mau, radius = 6f, center = androidx.compose.ui.geometry.Offset(x, y))
        }
    }
}

/** Thẻ số tổng hợp kiểu MISA: icon tròn + tiêu đề + giá trị + dòng phụ + sparkline mini. */
@Composable
fun TheSoLieu(
    tieuDe: String,
    giaTri: String,
    icon: ImageVector,
    mau: Color,
    phu: String = "",
    modifier: Modifier = Modifier,
    spark: List<Long> = emptyList(),
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
        shape = RoundedCornerShape(20.dp),
    ) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    Modifier.size(34.dp).clip(CircleShape).background(mau.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center,
                ) { Icon(icon, contentDescription = null, tint = mau) }
                Text(tieuDe, style = MaterialTheme.typography.labelLarge, maxLines = 1, overflow = TextOverflow.Ellipsis)
            }
            Text(giaTri, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = mau, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (spark.isNotEmpty()) Sparkline(spark, mau)
            if (phu.isNotEmpty()) Text(phu, style = MaterialTheme.typography.bodySmall)
        }
    }
}
