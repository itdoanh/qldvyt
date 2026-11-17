package vn.rinso.qldvyt

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import vn.rinso.qldvyt.ui.dieuHuong.QldvytNavGraph
import vn.rinso.qldvyt.ui.theme.ChuDeQldvyt

/** Màn hình chính duy nhất chứa toàn bộ Navigation Compose. */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
