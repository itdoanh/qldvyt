package vn.rinso.qldvyt.ui.saoLuu

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.launch
import vn.rinso.qldvyt.data.backup.SaoLuuManager

@HiltViewModel
class SaoLuuViewModel @Inject constructor(private val saoLuu: SaoLuuManager) : ViewModel() {
    var thongBao by mutableStateOf("")
        private set

    fun backup(uri: Uri) = viewModelScope.launch {
        runCatching {
            val kq = saoLuu.taoBackup(uri)
            thongBao = "Đã sao lưu xong. SHA-256: ${kq.sha256.take(16)}… (${kq.dungLuong / 1024} KB)"
        }.onFailure { thongBao = "Sao lưu thất bại: ${it.message}" }
    }

    fun xemTruoc(uri: Uri) = viewModelScope.launch {
        runCatching {
            val tt = saoLuu.docThongTin(uri)
            thongBao = if (tt.hopLe) "File hợp lệ. Ngày backup: ${tt.ngay}. SHA: ${tt.sha256.take(16)}…"
            else "File KHÔNG hợp lệ (thiếu qldvyt.db)."
        }.onFailure { thongBao = "Đọc file thất bại: ${it.message}" }
    }

    fun khoiPhuc(uri: Uri) = viewModelScope.launch {
        runCatching {
            saoLuu.khoiPhuc(uri)
            thongBao = "Đã khôi phục xong. Hãy khởi động lại app để dùng dữ liệu mới."
        }.onFailure { thongBao = "Khôi phục thất bại: ${it.message}" }
    }
}

/** Màn Sao lưu / Khôi phục bằng file .zip (Storage Access Framework, không cần quyền nguy hiểm). */
@Composable
fun ManHinhSaoLuu(vm: SaoLuuViewModel = hiltViewModel()) {
    var hienXacNhan by remember { mutableStateOf<Uri?>(null) }
    val chonBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
        if (uri != null) vm.backup(uri)
    }
    val chonRestore = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
        if (uri != null) hienXacNhan = uri
    }
    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Sao lưu & khôi phục", style = MaterialTheme.typography.headlineSmall)
        Text("Dữ liệu lưu local trên máy. Hãy sao lưu ra file .zip và giữ cẩn thận.")
        Button(onClick = { chonBackup.launch("qldvyt-backup.zip") }, modifier = Modifier.fillMaxWidth()) {
            Text("1. Sao lưu ra file (.zip)")
        }
        OutlinedButton(onClick = { chonRestore.launch(arrayOf("application/zip")) }, modifier = Modifier.fillMaxWidth()) {
            Text("2. Khôi phục từ file (.zip)")
        }
        if (vm.thongBao.isNotEmpty()) {
            Card { Text(vm.thongBao, modifier = Modifier.padding(16.dp)) }
        }
        hienXacNhan?.let { uri ->
            Card {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Xác nhận khôi phục?", style = MaterialTheme.typography.titleMedium)
                    Text("App sẽ tự backup dữ liệu hiện tại ra bộ nhớ cache trước, rồi mới chép đè.")
                    Button(onClick = { vm.xemTruoc(uri) }, modifier = Modifier.fillMaxWidth()) { Text("Kiểm tra file trước") }
                    Button(onClick = { vm.khoiPhuc(uri); hienXacNhan = null }, modifier = Modifier.fillMaxWidth()) {
                        Text("Đồng ý khôi phục (ghi đè)")
                    }
                    OutlinedButton(onClick = { hienXacNhan = null }, modifier = Modifier.fillMaxWidth()) { Text("Hủy") }
                }
            }
        }
    }
}
