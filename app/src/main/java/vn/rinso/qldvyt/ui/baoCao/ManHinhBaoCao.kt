package vn.rinso.qldvyt.ui.baoCao

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import vn.rinso.qldvyt.core.dinhDangTien

/** Màn Báo cáo: doanh thu tháng + lương tháng + xuất CSV (mở bằng Excel). */
@Composable
fun ManHinhBaoCao(vm: BaoCaoViewModel = hiltViewModel()) {
    val thang by vm.thang.collectAsState()
    val nam by vm.nam.collectAsState()
    val doanhThu by vm.doanhThuThang.collectAsState()
    val dsLuong by vm.dsLuong.collectAsState()
    val tb by vm.thongBao.collectAsState()

    val chonCsvLuong = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvLuong(uri)
    }
    val chonCsvDon = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        if (uri != null) vm.xuatCsvDon(uri)
    }

    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Báo cáo tháng $thang/$nam", style = MaterialTheme.typography.headlineSmall)
        if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = {
                if (thang == 1) { vm.thang.value = 12; vm.nam.value = nam - 1 } else vm.thang.value = thang - 1
            }, modifier = Modifier.weight(1f)) { Text("◀") }
            OutlinedButton(onClick = {
                if (thang == 12) { vm.thang.value = 1; vm.nam.value = nam + 1 } else vm.thang.value = thang + 1
            }, modifier = Modifier.weight(1f)) { Text("▶") }
        }
        Card {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Doanh thu (đơn hoàn thành): ${dinhDangTien(doanhThu)}", style = MaterialTheme.typography.titleMedium)
                Text("Tổng lương thực lĩnh: ${dinhDangTien(dsLuong.sumOf { it.thucLinh })} (${dsLuong.size} NV)")
                Text("Lãi gộp ước tính: ${dinhDangTien(doanhThu - dsLuong.sumOf { it.thucLinh })}", color = MaterialTheme.colorScheme.primary)
            }
        }
        Button(onClick = { chonCsvDon.launch("qldvyt-don-$thang-$nam.csv") }, modifier = Modifier.fillMaxWidth()) {
            Text("Xuất CSV đơn hàng tháng này")
        }
        Button(onClick = { chonCsvLuong.launch("qldvyt-luong-$thang-$nam.csv") }, modifier = Modifier.fillMaxWidth()) {
            Text("Xuất CSV bảng lương tháng này")
        }
        Text("File CSV mở được bằng Excel, giữ nguyên tiếng Việt (có BOM).", style = MaterialTheme.typography.bodySmall)
    }
}
