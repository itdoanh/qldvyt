package vn.rinso.qldvyt.ui.nhiemVu

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien

/** Tên loại việc tiếng Việt. */
fun tenLoaiViec(loai: String): String = when (loai) {
    "TIEM_TRUYEN" -> "Tiêm truyền"
    "THAY_BANG" -> "Thay băng"
    "THU_TUC" -> "Làm thủ tục"
    else -> "Việc khác"
}

fun tenTrangThaiViec(tt: String): String = when (tt) {
    "DAGIAO" -> "Đã giao"
    "DANGLAM" -> "Đang làm"
    "HOANTHANH" -> "Hoàn thành"
    "HUY" -> "Đã hủy"
    else -> tt
}

/** Màn Nhiệm vụ: giao việc + hoàn thành 1 chạm (NV hưởng 30%). */
@Composable
fun ManHinhNhiemVu(vm: NhiemVuViewModel = hiltViewModel()) {
    val dsViec by vm.dsViec.collectAsState()
    val dsNv by vm.dsNv.collectAsState()
    val tyLeMacDinh by vm.tyLeMacDinh.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var loc by remember { mutableStateOf<String?>(null) }
    var hienForm by remember { mutableStateOf(false) }
    var nvChon by remember { mutableStateOf<String?>(null) }
    var oTen by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }
    var loaiChon by remember { mutableStateOf("TIEM_TRUYEN") }

    val dsLoc = if (loc == null) dsViec else dsViec.filter { it.trangThai == loc }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Nhiệm vụ (NV hưởng ${(tyLeMacDinh * 100).toInt()}%)", style = MaterialTheme.typography.headlineSmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        OutlinedButton(onClick = { hienForm = !hienForm }, modifier = Modifier.fillMaxWidth()) {
            Text(if (hienForm) "Đóng form giao việc" else "+ Giao việc mới")
        }
        if (hienForm) {
            Card {
                Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (dsNv.isEmpty()) Text("Chưa có NV đang làm — hãy thêm ở tab Thêm → Nhân viên.")
                    dsNv.forEach { nv ->
                        AssistChip(onClick = { nvChon = nv.id }, label = { Text(if (nvChon == nv.id) "✓ ${nv.hoTen}" else nv.hoTen) })
                    }
                    OutlinedTextField(oTen, { oTen = it }, label = { Text("Tên công việc (*)") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("TIEM_TRUYEN", "THAY_BANG", "THU_TUC", "KHAC").forEach { loai ->
                            AssistChip(onClick = { loaiChon = loai }, label = { Text(if (loaiChon == loai) "✓ ${tenLoaiViec(loai)}" else tenLoaiViec(loai)) })
                        }
                    }
                    OutlinedTextField(oGia, { oGia = it.filter(Char::isDigit) }, label = { Text("Tổng giá (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                    Button(onClick = {
                        if (nvChon != null) {
                            vm.giaoViec(nvChon!!, oTen, loaiChon, oGia.toLongOrNull() ?: 0L, tyLeMacDinh)
                            oTen = ""; oGia = ""
                        }
                    }, enabled = nvChon != null, modifier = Modifier.fillMaxWidth()) { Text("Giao việc") }
                }
            }
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(null to "Tất cả", "DAGIAO" to "Đã giao", "DANGLAM" to "Đang làm", "HOANTHANH" to "Xong", "HUY" to "Hủy").forEach { (ma, ten) ->
                AssistChip(onClick = { loc = ma }, label = { Text(ten) })
            }
        }
        if (dsLoc.isEmpty()) {
            Card { Text("Chưa có nhiệm vụ nào. Bấm “Giao việc mới”.", Modifier.padding(16.dp)) }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(dsLoc, key = { it.id }) { viec ->
                    Card {
                        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("${viec.tenCongViec} • ${viec.maViec}", style = MaterialTheme.typography.titleMedium)
                            Text("${tenLoaiViec(viec.loai)} • ${tenTrangThaiViec(viec.trangThai)} • Giá: ${dinhDangTien(viec.tongGia)}")
                            if (viec.trangThai == "HOANTHANH") {
                                Text("NV hưởng: ${dinhDangTien(viec.tienNvHuong)} (${(viec.tyLeHuong * 100).toInt()}%)", color = MaterialTheme.colorScheme.primary)
                            }
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                if (viec.trangThai == "DAGIAO") OutlinedButton(onClick = { vm.nhanViec(viec.id) }) { Text("Nhận việc") }
                                if (viec.trangThai == "DAGIAO" || viec.trangThai == "DANGLAM") {
                                    Button(onClick = { vm.hoanThanh(viec.id) }) { Text("Hoàn thành") }
                                    OutlinedButton(onClick = { vm.huy(viec.id) }) { Text("Hủy") }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
