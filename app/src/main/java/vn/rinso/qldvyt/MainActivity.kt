package vn.rinso.qldvyt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.ui.dieuHuong.QldvytNavGraph
import vn.rinso.qldvyt.ui.theme.ChuDeQldvyt

/** Màn hình chính duy nhất chứa toàn bộ Navigation Compose. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @javax.inject.Inject lateinit var db: vn.rinso.qldvyt.data.local.QldvytDatabase
    @javax.inject.Inject lateinit var caiDat: vn.rinso.qldvyt.data.prefs.CaiDat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Seed dữ liệu mẫu phong phú 1 lần (幂等, máy cũ cũng được bổ sung)
        lifecycleScope.launch {
            runCatching { vn.rinso.qldvyt.data.local.seedNeuChuaCo(db, caiDat) }
        }
        setContent {
            ChuDeQldvyt {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    QldvytNavGraph()
                }
            }
        }
    }
}
