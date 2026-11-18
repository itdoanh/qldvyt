package vn.rinso.qldvyt.ui.dieuHuong

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.MedicalServices
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Task
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import vn.rinso.qldvyt.ui.baoCao.ManHinhBaoCao
import vn.rinso.qldvyt.ui.caiDat.ManHinhCaiDat
import vn.rinso.qldvyt.ui.congNo.ManHinhCongNo
import vn.rinso.qldvyt.ui.dichVu.ManHinhDichVu
import vn.rinso.qldvyt.ui.donHang.ManHinhChiTietDon
import vn.rinso.qldvyt.ui.donHang.ManHinhDonHang
import vn.rinso.qldvyt.ui.donHang.ManHinhTaoDon
import vn.rinso.qldvyt.ui.gioiThieu.ManHinhGioiThieu
import vn.rinso.qldvyt.ui.khachHang.ManHinhKhachHang
import vn.rinso.qldvyt.ui.kho.ManHinhKho
import vn.rinso.qldvyt.ui.luong.ManHinhLuong
import vn.rinso.qldvyt.ui.nhanVien.ManHinhNhanVien
import vn.rinso.qldvyt.ui.nhiemVu.ManHinhNhiemVu
import vn.rinso.qldvyt.ui.saoLuu.ManHinhSaoLuu
import vn.rinso.qldvyt.ui.tongQuan.ManHinhTongQuan

/** Tuyến điều hướng chính (tiếng Việt). */
object Tuyen {
    const val TONG_QUAN = "tong_quan"
    const val DON_HANG = "don_hang"
    const val TAO_DON = "tao_don"
    const val CHI_TIET_DON = "chi_tiet_don/{donId}"
    const val KHACH_HANG = "khach_hang"
    const val DICH_VU = "dich_vu"
    const val CONG_NO = "cong_no"
    const val NHAN_VIEN = "nhan_vien"
    const val LUONG = "luong"
    const val BAO_CAO = "bao_cao"
    const val NHIEM_VU = "nhiem_vu"
    const val KHO = "kho"
    const val THEM = "them"
    const val CAI_DAT = "cai_dat"
    const val SAO_LUU = "sao_luu"
    const val GIOI_THIEU = "gioi_thieu"

    fun chiTietDon(donId: String) = "chi_tiet_don/$donId"
}

private data class TabBar(val tuyen: String, val nhan: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QldvytNavGraph() {
    val nav = rememberNavController()
    val tabs = listOf(
        TabBar(Tuyen.TONG_QUAN, "Tổng quan", Icons.Filled.Home),
        TabBar(Tuyen.DON_HANG, "Đơn", Icons.Filled.ReceiptLong),
        TabBar(Tuyen.NHIEM_VU, "Việc", Icons.Filled.Task),
        TabBar(Tuyen.KHO, "Kho", Icons.Filled.Inventory2),
        TabBar(Tuyen.THEM, "Thêm", Icons.Filled.Group),
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("QLDVYT", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Text("Quản lý Dịch vụ Y tế • Rinso", style = MaterialTheme.typography.labelSmall)
                    }
                },
                actions = {
                    IconButton(onClick = { nav.navigate(Tuyen.BAO_CAO) }) {
                        Icon(Icons.Filled.Assessment, contentDescription = "Báo cáo")
                    }
                    IconButton(onClick = { nav.navigate(Tuyen.CAI_DAT) }) {
                        Icon(Icons.Filled.Settings, contentDescription = "Cài đặt")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                val hienTai by nav.currentBackStackEntryAsState()
                val tuyenHienTai = hienTai?.destination?.route
                tabs.forEach { t ->
                    NavigationBarItem(
                        selected = tuyenHienTai == t.tuyen ||
                            (t.tuyen == Tuyen.DON_HANG && tuyenHienTai?.startsWith("chi_tiet_don") == true),
                        onClick = {
                            nav.navigate(t.tuyen) {
                                launchSingleTop = true
                                popUpTo(Tuyen.TONG_QUAN) { saveState = true }
                                restoreState = true
                            }
                        },
                        icon = { Icon(t.icon, contentDescription = t.nhan) },
                        label = { Text(t.nhan) },
                    )
                }
            }
        }
    ) { dem ->
        NavHost(
            navController = nav,
            startDestination = Tuyen.TONG_QUAN,
            modifier = Modifier.padding(dem),
        ) {
            composable(Tuyen.TONG_QUAN) {
                ManHinhTongQuan(
                    onTaoDon = { nav.navigate(Tuyen.TAO_DON) },
                    onDonHang = { nav.navigate(Tuyen.DON_HANG) },
                    onNhiemVu = { nav.navigate(Tuyen.NHIEM_VU) },
                    onKho = { nav.navigate(Tuyen.KHO) },
                    onBaoCao = { nav.navigate(Tuyen.BAO_CAO) },
                    onCongNo = { nav.navigate(Tuyen.CONG_NO) },
                    onNhanVien = { nav.navigate(Tuyen.NHAN_VIEN) },
                    onMoDon = { id -> nav.navigate(Tuyen.chiTietDon(id)) },
                    onGioiThieu = { nav.navigate(Tuyen.GIOI_THIEU) },
                )
            }
            composable(Tuyen.DON_HANG) {
                ManHinhDonHang(
                    onTaoDon = { nav.navigate(Tuyen.TAO_DON) },
                    onMoChiTiet = { id -> nav.navigate(Tuyen.chiTietDon(id)) },
                )
            }
            composable(Tuyen.TAO_DON) {
                ManHinhTaoDon(onXong = { nav.popBackStack(Tuyen.DON_HANG, false) })
            }
            composable(
                route = Tuyen.CHI_TIET_DON,
                arguments = listOf(navArgument("donId") { type = NavType.StringType }),
            ) { back ->
                ManHinhChiTietDon(donId = back.arguments?.getString("donId") ?: "")
            }
            composable(Tuyen.KHACH_HANG) {
                ManHinhKhachHang(onChon = { nav.navigate(Tuyen.TAO_DON) })
            }
            composable(Tuyen.DICH_VU) { ManHinhDichVu() }
            composable(Tuyen.CONG_NO) {
                ManHinhCongNo(onMoDon = { id -> nav.navigate(Tuyen.chiTietDon(id)) })
            }
            composable(Tuyen.NHIEM_VU) { ManHinhNhiemVu() }
            composable(Tuyen.KHO) { ManHinhKho() }
            composable(Tuyen.NHAN_VIEN) { ManHinhNhanVien() }
            composable(Tuyen.LUONG) { ManHinhLuong() }
            composable(Tuyen.BAO_CAO) { ManHinhBaoCao() }
            composable(Tuyen.THEM) {
                ManHinhThem(
                    onKhachHang = { nav.navigate(Tuyen.KHACH_HANG) },
                    onNhanVien = { nav.navigate(Tuyen.NHAN_VIEN) },
                    onNhiemVu = { nav.navigate(Tuyen.NHIEM_VU) },
                    onLuong = { nav.navigate(Tuyen.LUONG) },
                    onBaoCao = { nav.navigate(Tuyen.BAO_CAO) },
                    onDichVu = { nav.navigate(Tuyen.DICH_VU) },
                    onCongNo = { nav.navigate(Tuyen.CONG_NO) },
                    onKho = { nav.navigate(Tuyen.KHO) },
                    onCaiDat = { nav.navigate(Tuyen.CAI_DAT) },
                    onSaoLuu = { nav.navigate(Tuyen.SAO_LUU) },
                    onGioiThieu = { nav.navigate(Tuyen.GIOI_THIEU) },
                )
            }
            composable(Tuyen.CAI_DAT) { ManHinhCaiDat() }
            composable(Tuyen.SAO_LUU) { ManHinhSaoLuu() }
            composable(Tuyen.GIOI_THIEU) { ManHinhGioiThieu() }
        }
    }
}

/** Tab Thêm kiểu MISA: lưới chức năng có icon + mô tả. */
@Composable
private fun ManHinhThem(
    onKhachHang: () -> Unit,
    onNhanVien: () -> Unit,
    onNhiemVu: () -> Unit,
    onLuong: () -> Unit,
    onBaoCao: () -> Unit,
    onDichVu: () -> Unit,
    onCongNo: () -> Unit,
    onKho: () -> Unit,
    onCaiDat: () -> Unit,
    onSaoLuu: () -> Unit,
    onGioiThieu: () -> Unit,
) {
    data class Muc(val tieuDe: String, val moTa: String, val icon: ImageVector, val lam: () -> Unit)
    val ds = listOf(
        Muc("Khách hàng", "20 KH mẫu, tìm theo SĐT", Icons.Filled.Person, onKhachHang),
        Muc("Nhân viên", "12 NV + chấm công", Icons.Filled.Group, onNhanVien),
        Muc("Nhiệm vụ", "Giao việc hưởng 30%", Icons.Filled.Task, onNhiemVu),
        Muc("Bảng lương", "Tính + chốt tháng", Icons.Filled.Payments, onLuong),
        Muc("Báo cáo", "Doanh thu + sổ nhóm", Icons.Filled.Assessment, onBaoCao),
        Muc("Dịch vụ & Gói", "15 DV + 5 gói", Icons.Filled.MedicalServices, onDichVu),
        Muc("Công nợ", "Tuổi nợ, nhắc thu", Icons.Filled.ReceiptLong, onCongNo),
        Muc("Kho", "Nhập xuất + cảnh báo", Icons.Filled.Inventory2, onKho),
        Muc("Sao lưu", "File .zip + CSV", Icons.Filled.Backup, onSaoLuu),
        Muc("Cài đặt", "Sáng/Tối + công chuẩn", Icons.Filled.Settings, onCaiDat),
        Muc("Giới thiệu", "Rinso • itdoanh", Icons.Filled.Info, onGioiThieu),
    )
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Tất cả chức năng", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Kiểu MISA: mỗi nghiệp vụ 1 ô — bấm để mở.", style = MaterialTheme.typography.bodySmall)
        ds.chunked(2).forEach { hang ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                hang.forEach { m ->
                    Card(
                        onClick = m.lam,
                        modifier = Modifier.weight(1f),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                    ) {
                        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Icon(m.icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                            Text(m.tieuDe, fontWeight = FontWeight.Bold)
                            Text(m.moTa, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                if (hang.size == 1) Row(Modifier.weight(1f)) { }
            }
        }
    }
}
