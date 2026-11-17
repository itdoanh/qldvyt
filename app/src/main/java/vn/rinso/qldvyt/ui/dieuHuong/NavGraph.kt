package vn.rinso.qldvyt.ui.dieuHuong

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import vn.rinso.qldvyt.ui.gioiThieu.ManHinhGioiThieu
import vn.rinso.qldvyt.ui.tongQuan.ManHinhTongQuan

/** Tuyến điều hướng chính (tiếng Việt). */
object Tuyen {
    const val TONG_QUAN = "tong_quan"
    const val DON_HANG = "don_hang"
    const val NHIEM_VU = "nhiem_vu"
    const val KHO = "kho"
    const val THEM = "them"
    const val GIOI_THIEU = "gioi_thieu"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QldvytNavGraph() {
    val nav = rememberNavController()
    val mucList = listOf(
        Tuyen.TONG_QUAN to "Tổng quan",
        Tuyen.DON_HANG to "Đơn hàng",
        Tuyen.NHIEM_VU to "Nhiệm vụ",
        Tuyen.KHO to "Kho",
        Tuyen.THEM to "Thêm",
    )
    Scaffold(
        topBar = { TopAppBar(title = { Text("QLDVYT — Quản lý Dịch vụ Y tế") }) },
        bottomBar = {
            NavigationBar {
                val hienTai by nav.currentBackStackEntryAsState()
                val tuyenHienTai = hienTai?.destination?.route
                mucList.forEach { (tuyen, nhan) ->
                    val icon = when (tuyen) {
                        Tuyen.TONG_QUAN -> Icons.Filled.Home
                        Tuyen.DON_HANG -> Icons.Filled.ShoppingCart
                        Tuyen.NHIEM_VU -> Icons.Filled.List
                        Tuyen.KHO -> Icons.Filled.Add
                        else -> Icons.Filled.Person
                    }
                    NavigationBarItem(
                        selected = tuyenHienTai == tuyen,
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
            composable(Tuyen.TONG_QUAN) { ManHinhTongQuan(onGioiThieu = { nav.navigate(Tuyen.GIOI_THIEU) }) }
            composable(Tuyen.DON_HANG) { ManHinhCho(tieuDe = "Đơn hàng", moTa = "Phase P2: wizard tạo đơn 3 bước, thu tiền, công nợ.") }
            composable(Tuyen.NHIEM_VU) { ManHinhCho(tieuDe = "Nhiệm vụ", moTa = "Phase P3: giao việc tiêm truyền / thay băng / thủ tục, NV hưởng 30%.") }
            composable(Tuyen.KHO) { ManHinhCho(tieuDe = "Kho vật tư", moTa = "Phase P3: nhập / xuất / kiểm kê, cảnh báo tồn thấp.") }
            composable(Tuyen.THEM) { ManHinhGioiThieu() }
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
