package vn.rinso.qldvyt.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Màu gợi ngành y tế (teal) — Material 3 Expressive
private val TealSeed = Color(0xFF00796B)

private val Sang = lightColorScheme(
    primary = TealSeed,
    secondary = Color(0xFF4DB6AC),
    tertiary = Color(0xFFFFA000),
)

private val Toi = darkColorScheme(
    primary = Color(0xFF80CBC4),
    secondary = Color(0xFF4DB6AC),
    tertiary = Color(0xFFFFCC80),
)

/** Theme QLDVYT: hỗ trợ sáng/tối, font hệ thống, tiếng Việt. */
@Composable
fun ChuDeQldvyt(toi: Boolean = false, noiDung: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (toi) Toi else Sang,
        content = noiDung,
    )
}
