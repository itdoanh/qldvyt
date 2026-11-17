package vn.rinso.qldvyt.ui.gioiThieu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/** Màn Giới thiệu: thông tin phần mềm (hiển thị trong app). */
@Composable
fun ManHinhGioiThieu() {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Top,
    ) {
        Text("QLDVYT — Quản lý Dịch vụ Y tế", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text("Nhà phát triển: Rinso (Thuộc Rin)")
        Text("Lập trình viên: Đoàn Văn Doanh (itdoanh)")
        Text("Repo: github.com/itdoanh/qldvyt")
        Spacer(Modifier.height(8.dp))
        Text("Hỗ trợ Android 10 trở lên. Dữ liệu lưu local trên máy (không cần server), sao lưu/khôi phục bằng file.")
        Spacer(Modifier.height(8.dp))
        Text("Chỉ dùng nội bộ công ty.", color = MaterialTheme.colorScheme.error)
    }
}
