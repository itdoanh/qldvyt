package vn.rinso.qldvyt.ui.dieuHuong

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import vn.rinso.qldvyt.ui.caiDat.ManHinhCaiDat
import vn.rinso.qldvyt.ui.congNo.ManHinhCongNo
import vn.rinso.qldvyt.ui.dichVu.ManHinhDichVu
import vn.rinso.qldvyt.ui.donHang.ManHinhChiTietDon
import vn.rinso.qldvyt.ui.donHang.ManHinhDonHang
import vn.rinso.qldvyt.ui.donHang.ManHinhTaoDon
import vn.rinso.qldvyt.ui.gioiThieu.ManHinhGioiThieu
import vn.rinso.qldvyt.ui.baoCao.ManHinhBaoCao
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QldvytNavGraph() {
    val nav = rememberNavController()
    val mucList = listOf(
        Tuyen.TONG_QUAN to "Tổng quan",
        Tuyen.DON_HANG to "Đơn hàng",
        Tuyen.BAO_CAO to "Báo cáo",
        Tuyen.KHO to "Kho",
        Tuyen.THEM to "Thêm",
    )
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("QLDVYT — Quản lý Dịch vụ Y tế") },
                actions = {
                    IconButton(onClick = { nav.navigate(Tuyen.SAO_LUU) }) {
                        Icon(Icons.Filled.Add, contentDescription = "Sao lưu")
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
                mucList.forEach { (tuyen, nhan) ->
                    val icon = when (tuyen) {
                        Tuyen.TONG_QUAN -> Icons.Filled.Home
                        Tuyen.DON_HANG -> Icons.Filled.ShoppingCart
                        Tuyen.BAO_CAO -> Icons.Filled.List
                        Tuyen.KHO -> Icons.Filled.Add
                        else -> Icons.Filled.Person
                    }
                    NavigationBarItem(
                        selected = tuyenHienTai == tuyen || (tuyen == Tuyen.DON_HANG && tuyenHienTai?.startsWith("chi_tiet_don") == true),
                        onClick = { nav.navigate(tuyen) { launchSingleTop = true } },
                        icon = { Icon(icon, contentDescription = nhan) },
                        label = { Text(nhan) },
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
                    onGioiThieu = { nav.navigate(Tuyen.GIOI_THIEU) },
                    onBaoCao = { nav.navigate(Tuyen.BAO_CAO) },
                    onDonHang = { nav.navigate(Tuyen.TAO_DON) },
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
                    onLuong = { nav.navigate(Tuyen.LUONG) },
                    onBaoCao = { nav.navigate(Tuyen.BAO_CAO) },
                    onDichVu = { nav.navigate(Tuyen.DICH_VU) },
                    onCongNo = { nav.navigate(Tuyen.CONG_NO) },
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

/** Màn hình chờ cho các phase sau — vẫn đúng chuẩn UI tiếng Việt. */
@Composable
private fun ManHinhCho(tieuDe: String, moTa: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
    ) {
        Text(tieuDe, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(moTa)
        Spacer(modifier = Modifier.height(8.dp))
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Dữ liệu lưu local trên máy, backup bằng file — không cần server.")
                Spacer(modifier = Modifier.height(4.dp))
                Text("Nhà phát triển: Rinso (Thuộc Rin) • Lập trình: Đoàn Văn Doanh (itdoanh)")
            }
        }
    }
}

/** Tab Thêm: gom Khách hàng, Nhân viên, Lương, Báo cáo, Dịch vụ, Công nợ, Sao lưu, Cài đặt. */
@Composable
private fun ManHinhThem(
    onKhachHang: () -> Unit,
    onNhanVien: () -> Unit,
    onLuong: () -> Unit,
    onBaoCao: () -> Unit,
    onDichVu: () -> Unit,
    onCongNo: () -> Unit,
    onCaiDat: () -> Unit,
    onSaoLuu: () -> Unit,
    onGioiThieu: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text("Chức năng khác", style = MaterialTheme.typography.headlineSmall)
        OutlinedButton(onClick = onKhachHang, modifier = Modifier.fillMaxWidth()) { Text("Khách hàng") }
        OutlinedButton(onClick = onNhanVien, modifier = Modifier.fillMaxWidth()) { Text("Nhân viên & chấm công") }
        OutlinedButton(onClick = onLuong, modifier = Modifier.fillMaxWidth()) { Text("Bảng lương") }
        OutlinedButton(onClick = onBaoCao, modifier = Modifier.fillMaxWidth()) { Text("Báo cáo & xuất CSV") }
        OutlinedButton(onClick = onDichVu, modifier = Modifier.fillMaxWidth()) { Text("Dịch vụ & Gói") }
        OutlinedButton(onClick = onCongNo, modifier = Modifier.fillMaxWidth()) { Text("Công nợ") }
        OutlinedButton(onClick = onSaoLuu, modifier = Modifier.fillMaxWidth()) { Text("Sao lưu & khôi phục (file .zip)") }
        OutlinedButton(onClick = onCaiDat, modifier = Modifier.fillMaxWidth()) { Text("Cài đặt hệ thống") }
        OutlinedButton(onClick = onGioiThieu, modifier = Modifier.fillMaxWidth()) { Text("Giới thiệu Rinso") }
    }
}
