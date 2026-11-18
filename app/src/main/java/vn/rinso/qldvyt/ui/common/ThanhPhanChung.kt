package vn.rinso.qldvyt.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
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

/** Thẻ số tổng hợp kiểu MISA: icon tròn + tiêu đề + giá trị + dòng phụ. */
@Composable
fun TheSoLieu(
    tieuDe: String,
    giaTri: String,
    icon: ImageVector,
    mau: Color,
    phu: String = "",
    modifier: Modifier = Modifier,
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
            if (phu.isNotEmpty()) Text(phu, style = MaterialTheme.typography.bodySmall)
        }
    }
}
