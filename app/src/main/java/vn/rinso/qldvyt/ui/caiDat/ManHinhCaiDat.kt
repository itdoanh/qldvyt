package vn.rinso.qldvyt.ui.caiDat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch

/** Màn hình Cài đặt: công thức lương, VAT/thuế toàn hệ thống + từng phần, công ty, giao diện. */
@Composable
fun ManHinhCaiDat(vm: CaiDatViewModel = hiltViewModel()) {
    val congChuan by vm.congChuanThang.collectAsState()
    val tyLe by vm.tyLeNhiemVu.collectAsState()
    val ngayNo by vm.soNgayCanhBaoNo.collectAsState()
    val tenCty by vm.tenCongTy.collectAsState()
    val diaChi by vm.diaChiCongTy.collectAsState()
    val sdt by vm.sdtCongTy.collectAsState()
    val mst by vm.maSoThue.collectAsState()
    val cheDo by vm.cheDoGiaoDien.collectAsState()
    val vatBat by vm.vatBat.collectAsState()
    val vatMacDinh by vm.vatMacDinh.collectAsState()
    val vatApDungDon by vm.vatApDungDon.collectAsState()
    val vatTheoDv by vm.vatTheoDv.collectAsState()
    val thueBat by vm.thueTncnBat.collectAsState()
    val thueTncn by vm.thueTncn.collectAsState()
    val hoaHong by vm.hoaHong.collectAsState()
    val phuCap by vm.phuCap.collectAsState()
    val phatMuon by vm.phatMuon.collectAsState()
    val mucTieu by vm.mucTieu.collectAsState()

    var oCong by remember(congChuan) { mutableStateOf(congChuan.toString()) }
    var oTyLe by remember(tyLe) { mutableStateOf((tyLe * 100).toString()) }
    var oNgay by remember(ngayNo) { mutableStateOf(ngayNo.toString()) }
    var oTen by remember(tenCty) { mutableStateOf(tenCty) }
    var oDiaChi by remember(diaChi) { mutableStateOf(diaChi) }
    var oSdt by remember(sdt) { mutableStateOf(sdt) }
    var oMst by remember(mst) { mutableStateOf(mst) }
    var oVat by remember(vatMacDinh) { mutableStateOf((vatMacDinh * 100).toString()) }
    var oThue by remember(thueTncn) { mutableStateOf((thueTncn * 100).toString()) }
    var oHoaHong by remember(hoaHong) { mutableStateOf((hoaHong * 100).toString()) }
    var oPhuCap by remember(phuCap) { mutableStateOf(phuCap.toString()) }
    var oPhat by remember(phatMuon) { mutableStateOf(phatMuon.toString()) }
    var oMucTieu by remember(mucTieu) { mutableStateOf(mucTieu.toString()) }
    val snack = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    fun bao(msg: String) = scope.launch { snack.showSnackbar(msg) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SnackbarHost(hostState = snack)
        Text("Cài đặt hệ thống", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Đổi là áp dụng ngay toàn app: đơn mới tính VAT theo công tắc, lương mới tính thuế/phụ cấp theo công thức.", style = MaterialTheme.typography.bodySmall)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Chế độ giao diện", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("SANG" to "☀️ Sáng", "TOI" to "🌙 Tối", "HE_THONG" to "📱 Theo máy").forEach { (ma, nhan) ->
                        FilterChip(
                            selected = cheDo == ma,
                            onClick = { vm.luuCheDoGiaoDien(ma); bao("Đã chuyển giao diện: $nhan") },
                            label = { Text(nhan) },
                        )
                    }
                }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Thuế VAT toàn hệ thống", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Bật/tắt VAT cả hệ thống và từng phần. Đơn mới sẽ snapshot thuế suất vào hóa đơn PDF.", style = MaterialTheme.typography.bodySmall)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Bật VAT toàn hệ thống", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                    Switch(checked = vatBat, onCheckedChange = { vm.luuVatBat(it); bao(if (it) "Đã BẬT VAT toàn hệ thống" else "Đã TẮT VAT toàn hệ thống") })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Áp VAT cho đơn hàng", modifier = Modifier.weight(1f))
                    Switch(checked = vatApDungDon, onCheckedChange = { vm.luuVatApDungDon(it); bao("Đã lưu") })
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Tính VAT theo từng dịch vụ")
                        Text("Bật = chỉ dòng chịu thuế mới tính; Tắt = tính gộp cả đơn", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = vatTheoDv, onCheckedChange = { vm.luuVatTheoDv(it); bao("Đã lưu") })
                }
                OutlinedTextField(value = oVat, onValueChange = { oVat = it }, label = { Text("Thuế suất VAT mặc định % (VD: 8 hoặc 10)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = {
                    oVat.toDoubleOrNull()?.let { vm.luuVatMacDinh(it / 100.0); bao("Đã lưu VAT ${it}%") }
                }, modifier = Modifier.fillMaxWidth()) { Text("Lưu thuế suất VAT") }
                Text("Mẹo: vào Dịch vụ để bật/tắt chịu thuế từng món (VD: khám bệnh không VAT, vật lý trị liệu có VAT).", style = MaterialTheme.typography.bodySmall)
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Công thức tính lương", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text("Thực lĩnh = theo công + NV 30% + hoa hồng + thưởng + chuyên cần + phụ cấp − tạm ứng − phạt − thuế TNCN.", style = MaterialTheme.typography.bodySmall)
                OutlinedTextField(value = oCong, onValueChange = { oCong = it }, label = { Text("Công chuẩn 1 tháng (VD: 26)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = oTyLe, onValueChange = { oTyLe = it }, label = { Text("Tỷ lệ NV hưởng % (VD: 30)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = oHoaHong, onValueChange = { oHoaHong = it }, label = { Text("Hoa hồng thêm % trên NV (VD: 0 hoặc 5)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = oPhuCap, onValueChange = { oPhuCap = it.filter(Char::isDigit) }, label = { Text("Phụ cấp mặc định (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = oPhat, onValueChange = { oPhat = it.filter(Char::isDigit) }, label = { Text("Phạt mỗi ngày đi muộn (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Khấu trừ thuế TNCN", fontWeight = FontWeight.SemiBold)
                        Text("Bật để trừ thuế trên tổng thu nhập", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(checked = thueBat, onCheckedChange = { vm.luuThueBat(it) })
                }
                OutlinedTextField(value = oThue, onValueChange = { oThue = it }, label = { Text("Thuế TNCN % (VD: 10)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = oNgay, onValueChange = { oNgay = it }, label = { Text("Số ngày cảnh báo công nợ (VD: 7)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(value = oMucTieu, onValueChange = { oMucTieu = it.filter(Char::isDigit) }, label = { Text("Mục tiêu doanh thu tháng (VNĐ)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                Button(onClick = {
                    oCong.toDoubleOrNull()?.let { vm.luuCongChuan(it) }
                    oTyLe.toDoubleOrNull()?.let { vm.luuTyLe(it / 100.0) }
                    oHoaHong.toDoubleOrNull()?.let { vm.luuHoaHong(it / 100.0) }
                    oPhuCap.toLongOrNull()?.let { vm.luuPhuCap(it) }
                    oPhat.toLongOrNull()?.let { vm.luuPhatMuon(it) }
                    oThue.toDoubleOrNull()?.let { vm.luuThueTncn(it / 100.0) }
                    oNgay.toIntOrNull()?.let { vm.luuNgayCanhBao(it) }
                    oMucTieu.toLongOrNull()?.let { vm.luuMucTieu(it) }
                    bao("Đã lưu công thức lương")
                }, modifier = Modifier.fillMaxWidth()) { Text("Lưu công thức lương") }
            }
        }

        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Thông tin công ty (in hóa đơn PDF)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                OutlinedTextField(value = oTen, onValueChange = { oTen = it }, label = { Text("Tên công ty") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = oDiaChi, onValueChange = { oDiaChi = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = oSdt, onValueChange = { oSdt = it }, label = { Text("Số điện thoại") }, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = oMst, onValueChange = { oMst = it }, label = { Text("Mã số thuế (để trống nếu không có)") }, modifier = Modifier.fillMaxWidth())
                Button(onClick = {
                    vm.luuTenCongTy(oTen); vm.luuDiaChi(oDiaChi); vm.luuSdt(oSdt); vm.luuMst(oMst)
                    bao("Đã lưu thông tin công ty")
                }, modifier = Modifier.fillMaxWidth()) { Text("Lưu thông tin công ty") }
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Nhà phát triển: Rinso by Rin", fontWeight = FontWeight.SemiBold)
                Text("App offline 100% — dữ liệu lưu trên máy, sao lưu bằng file.", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}
