package vn.rinso.qldvyt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Màu gợi ngành y tế (teal) — Material 3 Expressive
private val TealSeed = Color(0xFF00796B)
private val TealDark = Color(0xFF00564D)
private val XanhNgoc = Color(0xFF4DB6AC)
private val CamYTe = Color(0xFFFFA000)
private val HongNhac = Color(0xFFE91E63)
private val TimThan = Color(0xFF3F51B5)
private val XanhLa = Color(0xFF2E7D32)
private val DoNo = Color(0xFFC62828)
private val CamNo = Color(0xFFEF6C00)

private val Sang = lightColorScheme(
    primary = TealSeed,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFA7E8DD),
    onPrimaryContainer = Color(0xFF00201C),
    secondary = XanhNgoc,
    onSecondary = Color.White,
    secondaryContainer = Color(0xFFD4F4EE),
    onSecondaryContainer = Color(0xFF00201C),
    tertiary = CamYTe,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFFFFE0B2),
    error = DoNo,
    surface = Color(0xFFF7FAF9),
    surfaceVariant = Color(0xFFE6F0ED),
    surfaceContainerHighest = Color(0xFFFFFFFF),
)

private val Toi = darkColorScheme(
    primary = Color(0xFF80CBC4),
    onPrimary = Color(0xFF00201C),
    primaryContainer = TealDark,
    secondary = XanhNgoc,
    tertiary = Color(0xFFFFCC80),
    error = Color(0xFFFFB4AB),
    surface = Color(0xFF101413),
    surfaceVariant = Color(0xFF1A2422),
    surfaceContainerHighest = Color(0xFF1E2B29),
)

/** Màu chức năng cho thẻ thống kê / biểu đồ (sáng + tối đều đọc được). */
object MauChucNang {
    val DoanhThu = Color(0xFF00796B)
    val DonCho = Color(0xFFEF6C00)
    val CongNo = Color(0xFFC62828)
    val Kho = Color(0xFF6A1B9A)
    val Luong = Color(0xFF1565C0)
    val NhiemVu = Color(0xFF2E7D32)
    val KhachHang = Color(0xFF00838F)
    val LoiNhuan = Color(0xFF2E7D32)
}

/** Theme QLDVYT: hỗ trợ sáng/tối, font hệ thống, tiếng Việt. */
@Composable
fun ChuDeQldvyt(
    toi: Boolean = isSystemInDarkTheme(),
    noiDung: @Composable () -> Unit,
) {
    MaterialTheme(
        colorScheme = if (toi) Toi else Sang,
        typography = Typography(),
        shapes = Shapes(
            small = RoundedCornerShape(10.dp),
            medium = RoundedCornerShape(16.dp),
            large = RoundedCornerShape(24.dp),
        ),
        content = noiDung,
    )
}
