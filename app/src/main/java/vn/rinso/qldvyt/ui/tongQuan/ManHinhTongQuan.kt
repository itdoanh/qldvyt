package vn.rinso.qldvyt.ui.tongQuan

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangNgay
import vn.rinso.qldvyt.core.dinhDangTien
import vn.rinso.qldvyt.ui.common.HangTienTrinh
import vn.rinso.qldvyt.ui.common.NhanTrangThai
import vn.rinso.qldvyt.ui.common.TheCanhBao
import vn.rinso.qldvyt.ui.common.TheHero
import vn.rinso.qldvyt.ui.common.TheSoLieu
import vn.rinso.qldvyt.ui.common.mauTrangThaiDon
import vn.rinso.qldvyt.ui.donHang.tenTrangThaiDon
import vn.rinso.qldvyt.ui.theme.MauChucNang

/** Tong quan v3 - trung tam dieu hanh: hero + sparkline + hang doi viec. */
@Composable
fun ManHinhTongQuan(
    onTaoDon: () -> Unit = {},
    onDonHang: () -> Unit = {},
    onNhiemVu: () -> Unit = {},
    onKho: () -> Unit = {},
    onBaoCao: () -> Unit = {},
    onCongNo: () -> Unit = {},
    onNhanVien: () -> Unit = {},
    onMoDon: (String) -> Unit = {},
    onGioiThieu: () -> Unit = {},
    vm: TongQuanViewModel = hiltViewModel(),
) {
    val doanhThuHomNay by vm.doanhThuHomNay.collectAsState()
    val doanhThuThang by vm.doanhThuThang.collectAsState()
    val donCho by vm.donCho.collectAsState()
    val tongNo by vm.tongNo.collectAsState()
    val sapHet by vm.dsSapHet.collectAsState()
    val dsNv by vm.dsNv.collectAsState()
    val donXong by vm.donXongThang.collectAsState()
    val spark by vm.spark7Ngay.collectAsState()
    val triGiaKho by vm.triGiaKho.collectAsState()
    val dsKh by vm.dsKhach.collectAsState()
    val dsViec by vm.dsViec.collectAsState()
    val dsDonMoi by vm.dsDonMoi.collectAsState()
    val mapKh = dsKh.associateBy { it.id }
    val mapNv = dsNv.associateBy { it.id }
    val viecCho = dsViec.count { it.trangThai == "DAGIAO" || it.trangThai == "DANGLAM" }
    val noDon = dsDonMoi.count { it.conNo > 0 && it.trangThai != "HUY" }
    val mucTieu = 50_000_000L
    val tyLeThang = if (mucTieu > 0) doanhThuThang.toFloat() / mucTieu else 0f

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        TheHero(
            dongNho = "QUAN LY DICH VU Y TE - RINSO",
            tieuDe = "Tong quan dieu hanh",
            dongSoLieu = "Hom nay: ${dinhDangTien(doanhThuHomNay)} - Thang: ${dinhDangTien(doanhThuThang)}",
            nutChinh = "+ Tao don",
            onNutChinh = onTaoDon,
            nutPhu = "Bao cao",
            onNutPhu = onBaoCao,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TheSoLieu("Doanh thu thang", dinhDangTien(doanhThuThang), Icons.Filled.Payments, MauChucNang.DoanhThu, "${donXong.size} don xong", Modifier.weight(1f), spark)
            TheSoLieu("Don can xu ly", "$donCho don", Icons.Filled.ReceiptLong, MauChucNang.DonCho, "Cho + dang lam", Modifier.weight(1f))
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            TheSoLieu("Cong no", dinhDangTien(tongNo), Icons.Filled.Warning, MauChucNang.CongNo, "$noDon don con no", Modifier.weight(1f))
            TheSoLieu("Nhan su", "${dsNv.size} NV", Icons.Filled.Group, MauChucNang.Luong, "$viecCho viec dang chay", Modifier.weight(1f))
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Tien do muc tieu thang (${dinhDangTien(mucTieu)})", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                HangTienTrinh("Doanh thu ${dinhDangTien(doanhThuThang)}", tyLeThang, MauChucNang.DoanhThu)
                Text("Kho tri gia ${dinhDangTien(triGiaKho)} - Khach hang ${dsKh.size} - NV ${dsNv.size}", style = MaterialTheme.typography.bodySmall)
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Can lam ngay", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                if (donCho == 0 && noDon == 0 && sapHet.isEmpty() && viecCho == 0) {
                    Text("Tuyet voi - khong con viec ton. Tao don moi ngay!", style = MaterialTheme.typography.bodySmall)
                } else {
                    if (donCho > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$donCho don cho/xu ly", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onDonHang) { Text("Mo don") }
                    }
                    if (noDon > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$noDon don con no (${dinhDangTien(tongNo)})", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onCongNo) { Text("Thu no") }
                    }
                    if (viecCho > 0) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("$viecCho viec chua xong", modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onNhiemVu) { Text("Giao viec") }
                    }
                    if (sapHet.isNotEmpty()) Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("Kho sap het (${sapHet.size}): " + sapHet.take(2).joinToString { it.ten }, modifier = Modifier.weight(1f))
                        OutlinedButton(onClick = onKho) { Text("Nhap kho") }
                    }
                }
            }
        }
        if (sapHet.isNotEmpty()) {
            TheCanhBao("Sap het kho (${sapHet.size})", sapHet.take(3).joinToString { "${it.ten} (con ${it.tonKho.toInt()} ${it.donVi})" }, "Mo kho nhap hang", onKho)
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("Loi tat nghiep vu", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onDonHang, modifier = Modifier.weight(1f)) { Text("Don hang") }
                    OutlinedButton(onClick = onNhiemVu, modifier = Modifier.weight(1f)) { Text("Giao viec 30%") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onNhanVien, modifier = Modifier.weight(1f)) { Text("Cham cong") }
                    OutlinedButton(onClick = onCongNo, modifier = Modifier.weight(1f)) { Text("Thu no") }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(onClick = onKho, modifier = Modifier.weight(1f)) { Text("Kho") }
                    OutlinedButton(onClick = onGioiThieu, modifier = Modifier.weight(1f)) { Text("Gioi thieu") }
                }
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Don moi nhat", fontWeight = FontWeight.Bold)
                    Text("${dsDonMoi.size} don", style = MaterialTheme.typography.bodySmall)
                }
                if (dsDonMoi.isEmpty()) {
                    Text("Chua co don - hay tao don roi bam Hoan thanh.", style = MaterialTheme.typography.bodySmall)
                } else {
                    dsDonMoi.take(5).forEach { don ->
                        val tenKh = mapKh[don.khachHangId]?.hoTen ?: "KH le"
                        Card(onClick = { onMoDon(don.id) }) {
                            Row(Modifier.padding(12.dp).fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column(Modifier.weight(1f)) {
                                    Text("${don.maDh} - $tenKh", fontWeight = FontWeight.SemiBold)
                                    Text(dinhDangNgay(don.ngayTao) + " - " + dinhDangTien(don.tongThanhToan), style = MaterialTheme.typography.bodySmall)
                                    if (don.conNo > 0) Text("Con no: ${dinhDangTien(don.conNo)}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
                                }
                                NhanTrangThai(don.trangThai, tenTrangThaiDon(don.trangThai), mauTrangThaiDon(don.trangThai))
                            }
                        }
                    }
                }
                OutlinedButton(onClick = onBaoCao, modifier = Modifier.fillMaxWidth()) { Text("Xem bao cao tong hop") }
            }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Ca lam dot nay", fontWeight = FontWeight.Bold)
                    Text("${dsViec.count { it.trangThai == "DANGLAM" }} dang lam", style = MaterialTheme.typography.bodySmall)
                }
                val dot = dsViec.take(4)
                if (dot.isEmpty()) Text("Chua giao viec nao - qua tab Viec de giao.", style = MaterialTheme.typography.bodySmall)
                else dot.forEach { v ->
                    val tenNv = mapNv[v.nhanVienId]?.hoTen ?: "?"
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("${v.tenCongViec} - $tenNv", modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodySmall)
                        OutlinedButton(onClick = onNhiemVu) { Text("Mo") }
                    }
                }
            }
        }
    }
}
