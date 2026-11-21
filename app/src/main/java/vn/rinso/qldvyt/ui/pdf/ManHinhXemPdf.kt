package vn.rinso.qldvyt.ui.pdf

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Print
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import vn.rinso.qldvyt.data.pdf.PdfChiaSe

/**
 * Trình xem PDF ngay trong app (không cần app ngoài, không lib nặng).
 * Dùng android.graphics.pdf.PdfRenderer vẽ từng trang thành Bitmap → Compose Image.
 * Thanh trên có 4 nút: Lưu (SAF) • Chia sẻ (Zalo/Gmail…) • In (máy in đt) • Đóng.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ManHinhXemPdf(duongDan: String, tieuDe: String = "Xem PDF", onDong: () -> Unit) {
    val ctx = LocalContext.current
    val file = remember(duongDan) { File(duongDan) }
    var anhTrang by remember { mutableStateOf<List<Bitmap>>(emptyList()) }
    var dangTai by remember { mutableStateOf(true) }
    var loi by remember { mutableStateOf("") }

    val chonChoLuu = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("application/pdf"),
    ) { uri ->
        if (uri != null) {
            try {
                PdfChiaSe.chepRaNgoai(ctx, file, uri)
            } catch (e: Exception) {
                loi = e.message ?: "Lưu thất bại"
            }
        }
    }

    // Render PDF → Bitmap trên IO, đóng renderer khi rời màn
    var rendererRef by remember { mutableStateOf<PdfRenderer?>(null) }
    var fdRef by remember { mutableStateOf<ParcelFileDescriptor?>(null) }
    LaunchedEffect(duongDan) {
        dangTai = true; loi = ""
        withContext(Dispatchers.IO) {
            runCatching {
                if (!file.exists() || file.length() == 0L) error("File PDF không tồn tại hoặc rỗng")
                fdRef?.close(); rendererRef?.close()
                val fd = ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)
                fdRef = fd
                val renderer = PdfRenderer(fd)
                rendererRef = renderer
                val ds = mutableListOf<Bitmap>()
                for (i in 0 until renderer.pageCount) {
                    renderer.openPage(i).use { trang ->
                        // Render gấp ~2x để chữ nét trên điện thoại
                        val tiLe = 2
                        val bm = Bitmap.createBitmap(
                            trang.width * tiLe, trang.height * tiLe, Bitmap.Config.ARGB_8888,
                        )
                        bm.eraseColor(android.graphics.Color.WHITE)
                        trang.render(bm, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        ds.add(bm)
                    }
                }
                anhTrang = ds
            }.onFailure { loi = it.message ?: "Không mở được PDF" }
        }
        dangTai = false
    }
    DisposableEffect(Unit) {
        onDispose {
            try { rendererRef?.close() } catch (_: Exception) { }
            try { fdRef?.close() } catch (_: Exception) { }
            try { anhTrang.forEach { if (!it.isRecycled) it.recycle() } } catch (_: Exception) { }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(tieuDe, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = onDong) {
                        Icon(Icons.Filled.Close, contentDescription = "Đóng")
                    }
                },
                actions = {
                    IconButton(onClick = { chonChoLuu.launch(file.name) }) {
                        Icon(Icons.Filled.Save, contentDescription = "Lưu")
                    }
                    IconButton(onClick = {
                        runCatching { PdfChiaSe.chiaSe(ctx, file) }
                            .onFailure { loi = it.message ?: "Chia sẻ thất bại" }
                    }) {
                        Icon(Icons.Filled.Share, contentDescription = "Chia sẻ")
                    }
                    IconButton(onClick = {
                        runCatching { PdfChiaSe.inPdf(ctx, file, file.nameWithoutExtension) }
                            .onFailure { loi = it.message ?: "In thất bại" }
                    }) {
                        Icon(Icons.Filled.Print, contentDescription = "In")
                    }
                },
            )
        },
    ) { dem ->
        Column(
            Modifier.fillMaxSize().padding(dem).padding(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(
                    "Lưu • Chia sẻ • In • Đóng ở thanh trên",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
            }
            if (loi.isNotBlank()) Card { Text(loi, Modifier.padding(12.dp), color = MaterialTheme.colorScheme.error) }
            if (dangTai) {
                Column(Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                    Text("Đang mở PDF…", style = MaterialTheme.typography.bodySmall)
                }
            } else if (anhTrang.isEmpty() && loi.isBlank()) {
                Card { Text("Không có trang nào để hiển thị.", Modifier.padding(12.dp)) }
            } else {
                Text("${file.name} • ${anhTrang.size} trang", style = MaterialTheme.typography.bodySmall)
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(anhTrang) { i, bm ->
                        Card {
                            Column(Modifier.padding(6.dp)) {
                                Text("Trang ${i + 1}/${anhTrang.size}", style = MaterialTheme.typography.labelSmall)
                                Image(
                                    bitmap = bm.asImageBitmap(),
                                    contentDescription = "Trang ${i + 1}",
                                    modifier = Modifier.fillMaxWidth(),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
