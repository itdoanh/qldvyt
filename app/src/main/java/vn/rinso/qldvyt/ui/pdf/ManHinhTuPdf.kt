package vn.rinso.qldvyt.ui.pdf

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import vn.rinso.qldvyt.ui.common.OTrong

/** Tủ PDF lương trong Tab Thêm: 2 cấp — loại (tổng / nhân viên) → tháng → file. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhTuPdf(
    onMoFile: (duongDan: String, tieuDe: String) -> Unit,
    onBack: () -> Unit,
    vm: TuPdfViewModel = hiltViewModel(),
) {
    var loai by remember { mutableStateOf(0) } // 0 = tổng, 1 = nhân viên
    var thangChon by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    val nhom = remember(loai, vm.tick) { vm.nhomTheoLoai(loai) }
    val files = remember(loai, thangChon, vm.tick) { vm.fileTheoThang(loai, thangChon) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Tủ PDF lương") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Về") } },
            )
        },
    ) { dem ->
        Column(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Lưu local trong máy • 2 cấp thư mục", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Card(
                    onClick = { loai = 0; thangChon = null },
                    colors = CardDefaults.cardColors(
                        containerColor = if (loai == 0) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                    modifier = Modifier.weight(1f),
                ) { Text("Bảng lương tổng", Modifier.padding(12.dp), fontWeight = FontWeight.Bold) }
                Card(
                    onClick = { loai = 1; thangChon = null },
                    colors = CardDefaults.cardColors(
                        containerColor = if (loai == 1) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainerHighest,
                    ),
                    modifier = Modifier.weight(1f),
                ) { Text("Phiếu lương NV", Modifier.padding(12.dp), fontWeight = FontWeight.Bold) }
            }
            if (thangChon != null) {
                Card(onClick = { thangChon = null }) {
                    Text("◀ Tháng ${thangChon!!.first}/${thangChon!!.second} — bấm để về danh sách tháng", Modifier.padding(12.dp))
                }
            }
            if (thangChon == null) {
                if (nhom.isEmpty()) {
                    OTrong(
                        if (loai == 0) "Chưa có bảng tổng nào" else "Chưa có phiếu NV nào",
                        "Vào Bảng lương → bấm Xuất PDF, file tự lưu vào đây.",
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(nhom) { n ->
                            Card(onClick = { thangChon = n.thang to n.nam }) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(Icons.Filled.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Column {
                                            Text("Tháng ${n.thang}/${n.nam}", fontWeight = FontWeight.Bold)
                                            Text(
                                                "${n.soFile} file • ${dungLuongDep(n.dungLuong)}",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                    Text("›", style = MaterialTheme.typography.headlineSmall)
                                }
                            }
                        }
                    }
                }
            } else {
                if (files.isEmpty()) {
                    OTrong("Thư mục trống", "File PDF của tháng này đã bị xóa.")
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(files, key = { it.absolutePath }) { f ->
                            Card(onClick = { onMoFile(f.absolutePath, f.name) }) {
                                Row(Modifier.fillMaxWidth().padding(12.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.weight(1f)) {
                                        Icon(Icons.Filled.Description, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(f.name, fontWeight = FontWeight.Bold)
                                            Text(
                                                "${dungLuongDep(f.length())} • ${ngayDep(f.lastModified())}",
                                                style = MaterialTheme.typography.bodySmall,
                                            )
                                        }
                                    }
                                    Text("Mở ›", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun dungLuongDep(b: Long): String = when {
    b >= 1_048_576L -> "%.1f MB".format(b / 1_048_576.0)
    b >= 1024L -> "%.0f KB".format(b / 1024.0)
    else -> "$b B"
}

private fun ngayDep(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy HH:mm", Locale("vi", "VN")).format(Date(millis))
