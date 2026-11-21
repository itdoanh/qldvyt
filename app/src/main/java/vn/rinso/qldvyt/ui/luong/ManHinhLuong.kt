package vn.rinso.qldvyt.ui.luong

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.data.local.BangLuong
import vn.rinso.qldvyt.ui.common.DuongDanCongThuc
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.OTrong

/** Màn Lương: diễn giải công thức + chốt/mở khóa + sửa tạm ứng/thưởng-phạt từng NV + xuất PDF. */
@Composable
fun ManHinhLuong(vm: LuongViewModel = hiltViewModel()) {
    val thang by vm.thang.collectAsState()
    val nam by vm.nam.collectAsState()
    val ds by vm.dsBangLuong.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var moDienGiai by remember { mutableStateOf<String?>(null) }
    var moSua by remember { mutableStateOf<String?>(null) }
    val tong = ds.sumOf { it.thucLinh }

    val chonPhieu = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        val nvId = vm.pendingXuatNv
        if (uri != null && nvId != null) vm.xuatPhieuLuong(nvId, uri)
        vm.pendingXuatNv = null
    }
    val chonBang = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        if (uri != null) vm.xuatBangTongHop(uri)
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text("Bảng lương $thang/$nam (${ds.size} NV)", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Thực lĩnh = theo công + NV + hoa hồng + thưởng + chuyên cần + phụ cấp − tạm ứng − phạt − thuế TNCN. Bấm tên để xem công thức, bấm Sửa để thêm tạm ứng/thưởng/phạt.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                OutlinedButton(onClick = {
                    if (thang == 1) { vm.thang.value = 12; vm.nam.value = nam - 1 } else vm.thang.value = thang - 1
                }) { Text("◀") }
                Column {
                    Text("Tháng $thang/$nam", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Text("Tổng: ${dinhDangTien(tong)}", style = MaterialTheme.typography.bodySmall)
                }
                OutlinedButton(onClick = {
                    if (thang == 12) { vm.thang.value = 1; vm.nam.value = nam + 1 } else vm.thang.value = thang + 1
                }) { Text("▶") }
            }
        }
        Button(onClick = { vm.tinhCaThang() }, modifier = Modifier.fillMaxWidth()) { Text("Tính lương cả tháng") }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = { chonBang.launch("bang-luong-${thang}-${nam}.pdf") }, modifier = Modifier.weight(1f), enabled = ds.isNotEmpty()) { Text("Xuất bảng tổng hợp PDF") }
        }
        if (ds.isEmpty()) {
            OTrong("Chưa có bảng lương", "Chấm công + hoàn thành nhiệm vụ rồi bấm Tính cả tháng.")
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ds, key = { it.id }) { bl ->
                    DongBangLuong(
                        bl = bl,
                        ten = vm.tenNv(bl.nhanVienId),
                        mo = moDienGiai == bl.id,
                        dangSua = moSua == bl.id,
                        onMo = { moDienGiai = if (moDienGiai == bl.id) null else bl.id },
                        onSua = { moSua = if (moSua == bl.id) null else bl.id },
                        vm = vm,
                        thang = thang,
                        nam = nam,
                        onXuatPhieu = {
                            vm.pendingXuatNv = bl.nhanVienId
                            val tenFile = "phieu-luong-thang-$thang-$nam-${bl.nhanVienId.take(8)}.pdf"
                            chonPhieu.launch(tenFile)
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun DongBangLuong(
    bl: BangLuong,
    ten: String,
    mo: Boolean,
    dangSua: Boolean,
    onMo: () -> Unit,
    onSua: () -> Unit,
    vm: LuongViewModel,
    thang: Int,
    nam: Int,
    onXuatPhieu: () -> Unit,
) {
    val dsTamUng by remember(thang, nam, bl.nhanVienId) { vm.tamUngKy(bl.nhanVienId) }.collectAsState(initial = emptyList())
    val dsTp by remember(thang, nam, bl.nhanVienId) { vm.thuongPhatKy(bl.nhanVienId) }.collectAsState(initial = emptyList())
    var oTien by remember(bl.id) { mutableStateOf("") }
    var oLyDo by remember(bl.id) { mutableStateOf("") }

    Card(
        onClick = onMo,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
    ) {
        Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(ten, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                NhanTrangThai(
                    bl.trangThai, if (bl.trangThai == "CHOT") "ĐÃ CHỐT" else "Nháp",
                    if (bl.trangThai == "CHOT") Color(0xFF2E7D32) else Color(0xFFEF6C00),
                )
            }
            Text("Công: ${bl.tongCong} • Theo công: ${dinhDangTien(bl.luongCoBan)}", style = MaterialTheme.typography.bodySmall)
            Text("NV: ${dinhDangTien(bl.tienNhiemVu)} • Hoa hồng: ${dinhDangTien(bl.hoaHong)} • Thưởng: ${dinhDangTien(bl.thuong)} • C.cần: ${dinhDangTien(bl.chuyenCan)}", style = MaterialTheme.typography.bodySmall)
            Text("Phụ cấp: ${dinhDangTien(bl.phuCap)} • TNCN: ${dinhDangTien(bl.thueTncn)}", style = MaterialTheme.typography.bodySmall)
            Text("Tạm ứng: ${dinhDangTien(bl.tamUng)} • Phạt: ${dinhDangTien(bl.khauTruKhac)}", style = MaterialTheme.typography.bodySmall)
            Text("Thực lĩnh: ${dinhDangTien(bl.thucLinh)}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            if (mo) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                        Text("Diễn giải công thức:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        DuongDanCongThuc("Lương theo công", dinhDangTien(bl.luongCoBan))
                        DuongDanCongThuc("Tiền nhiệm vụ đã hoàn thành", "+ ${dinhDangTien(bl.tienNhiemVu)}")
                        DuongDanCongThuc("Hoa hồng + thưởng + chuyên cần + phụ cấp", "+ ${dinhDangTien(bl.hoaHong + bl.thuong + bl.chuyenCan + bl.phuCap)}")
                        DuongDanCongThuc("Thuế TNCN", "− ${dinhDangTien(bl.thueTncn)}")
                        DuongDanCongThuc("Tạm ứng + phạt", "− ${dinhDangTien(bl.tamUng + bl.khauTruKhac)}")
                        DuongDanCongThuc("Thực lĩnh", dinhDangTien(bl.thucLinh))
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (bl.trangThai != "CHOT") Button(onClick = { vm.chot(bl.nhanVienId) }) { Text("Chốt") }
                else OutlinedButton(onClick = { vm.moKhoa(bl.nhanVienId) }) { Text("Mở khóa") }
                OutlinedButton(onClick = onMo) { Text(if (mo) "Ẩn" else "Công thức") }
                OutlinedButton(onClick = onSua) { Text(if (dangSua) "Đóng" else "Sửa ứng/thưởng") }
            }
            if (dangSua) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Column(Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Tạm ứng / thưởng / phạt tháng $thang/$nam (lưu là lương tự tính lại):", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        if (dsTamUng.isEmpty() && dsTp.isEmpty()) {
                            Text("Chưa có dòng nào trong tháng.", style = MaterialTheme.typography.bodySmall)
                        }
                        dsTamUng.forEach { tu ->
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Ứng ${dinhDangTien(tu.soTien)} — ${tu.lyDo.ifBlank { "Tạm ứng" }} (${dinhDangNgay(tu.ngay)})", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = { vm.xoaTamUngThang(tu.id, bl.nhanVienId) }) { Text("Xóa") }
                            }
                        }
                        dsTp.forEach { tp ->
                            val nhan = if (tp.soTien >= 0) "Thưởng" else "Phạt"
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("$nhan ${dinhDangTien(kotlin.math.abs(tp.soTien))} — ${tp.lyDo.ifBlank { tp.loai }}", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = { vm.xoaThuongPhatThang(tp.id, bl.nhanVienId) }) { Text("Xóa") }
                            }
                        }
                        OutlinedTextField(oTien, { oTien = it.filter(Char::isDigit) }, label = { Text("Số tiền (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(oLyDo, { oLyDo = it }, label = { Text("Lý do") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(onClick = {
                                oTien.toLongOrNull()?.let { vm.themTamUngThang(bl.nhanVienId, it, oLyDo); oTien = ""; oLyDo = "" }
                            }, modifier = Modifier.weight(1f)) { Text("Ứng") }
                            OutlinedButton(onClick = {
                                oTien.toLongOrNull()?.let { vm.themThuongThang(bl.nhanVienId, it, oLyDo); oTien = ""; oLyDo = "" }
                            }, modifier = Modifier.weight(1f)) { Text("Thưởng") }
                            OutlinedButton(onClick = {
                                oTien.toLongOrNull()?.let { vm.themPhatThang(bl.nhanVienId, it, oLyDo); oTien = ""; oLyDo = "" }
                            }, modifier = Modifier.weight(1f)) { Text("Phạt") }
                        }
                    }
                }
            }
            OutlinedButton(onClick = onXuatPhieu, modifier = Modifier.fillMaxWidth()) { Text("Xuất phiếu lương PDF") }
        }
    }
}
