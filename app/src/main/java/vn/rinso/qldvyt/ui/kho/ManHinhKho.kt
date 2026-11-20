package vn.rinso.qldvyt.ui.kho

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import vn.rinso.qldvyt.ui.common.OTimKiem
import vn.rinso.qldvyt.ui.common.OTrong

/** Man Kho v3: canh bao + tri gia + lich su nhap + them vat tu. */
@Composable
fun ManHinhKho(vm: KhoViewModel = hiltViewModel()) {
    val ds by vm.dsVatTu.collectAsState()
    val sapHet by vm.dsSapHet.collectAsState()
    val dsNhap by vm.dsNhap.collectAsState()
    val tb by vm.thongBao.collectAsState()
    var tk by remember { mutableStateOf("") }
    var tab by remember { mutableStateOf(0) }
    var hienNhap by remember { mutableStateOf(false) }
    var oTen by remember { mutableStateOf("") }
    var oSl by remember { mutableStateOf("") }
    var oGia by remember { mutableStateOf("") }
    var oNcc by remember { mutableStateOf("") }
    var vtChon by remember { mutableStateOf<String?>(null) }

    val dsLoc = ds.filter { tk.isBlank() || it.ten.contains(tk.trim(), true) || it.maVt.contains(tk.trim(), true) }
    val triGia = ds.sumOf { (it.tonKho * it.giaNhapTB).toLong() }
    val mapVt = ds.associateBy { it.id }

    Scaffold(floatingActionButton = {
        FloatingActionButton(onClick = { hienNhap = !hienNhap }) { Icon(Icons.Filled.Add, contentDescription = "Nhap kho") }
    }) { dem ->
        Column(Modifier.fillMaxSize().padding(dem).padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("Kho vat tu (${ds.size})", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text("Tri gia kho: ${dinhDangTien(triGia)} - Sap het: ${sapHet.size}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
            if (tb.isNotEmpty()) Card { Text(tb, Modifier.padding(12.dp)) }
            if (sapHet.isNotEmpty()) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Sap het: ${sapHet.size} mat hang", fontWeight = FontWeight.Bold)
                        Text(sapHet.take(4).joinToString { "${it.ten} (con ${it.tonKho.toInt()})" }, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = { tab = 0 }) { Text((if (tab == 0) "✓ " else "") + "Ton kho") }
                OutlinedButton(onClick = { tab = 1 }) { Text((if (tab == 1) "✓ " else "") + "Lich su nhap (${dsNhap.size})") }
            }
            if (tab == 1) {
                if (dsNhap.isEmpty()) {
                    OTrong("Chua co phieu nhap", "Bam + de nhap kho.", Icons.Filled.Inventory2)
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(dsNhap.take(30), key = { it.id }) { nk ->
                            val tenVt = mapVt[nk.vatTuId]?.ten ?: "Vat tu"
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest)) {
                                Column(Modifier.padding(12.dp).fillMaxWidth()) {
                                    Text("$tenVt +${nk.soLuong.toInt()} x ${dinhDangTien(nk.donGiaNhap)}", fontWeight = FontWeight.Bold)
                                    Text("${dinhDangNgay(nk.ngay)} - NCC: ${nk.nhaCungCap.ifEmpty { "khong ro" }}", style = MaterialTheme.typography.bodySmall)
                                    Text("Thanh tien: ${dinhDangTien((nk.soLuong * nk.donGiaNhap).toLong())}", color = MaterialTheme.colorScheme.primary)
                                }
                            }
                        }
                    }
                }
                return@Column
            }
            OTimKiem(tk, { tk = it }, "Tim vat tu theo ten / ma VT")
            if (hienNhap) {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                    Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Nhap kho (gia von binh quan tu cap nhat)", fontWeight = FontWeight.Bold)
                        Text("Vat tu: ${ds.firstOrNull { it.id == vtChon }?.ten ?: "chon o danh sach duoi"}")
                        OutlinedTextField(oSl, { oSl = it.filter { c -> c.isDigit() || c == '.' } }, label = { Text("So luong nhap") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(oGia, { oGia = it.filter(Char::isDigit) }, label = { Text("Don gia nhap (VND)") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        OutlinedTextField(oNcc, { oNcc = it }, label = { Text("Nha cung cap") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                        Button(onClick = {
                            if (vtChon != null) {
                                vm.nhapKho(vtChon!!, oSl.toDoubleOrNull() ?: 0.0, oGia.toLongOrNull() ?: 0L)
                                oSl = ""; oGia = ""; oNcc = ""; hienNhap = false
                            }
                        }, enabled = vtChon != null, modifier = Modifier.fillMaxWidth()) { Text("Xac nhan nhap kho") }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(oTen, { oTen = it }, label = { Text("Ten vat tu moi") }, modifier = Modifier.weight(1f))
                            OutlinedButton(onClick = { vm.themVatTu(oTen, "cai", 5.0); oTen = "" }) { Text("+ Them") }
                        }
                    }
                }
            }
            if (dsLoc.isEmpty()) {
                OTrong("Khong tim thay vat tu", "Thu tu khoa khac hoac them vat tu moi.", Icons.Filled.Inventory2)
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(dsLoc, key = { it.id }) { vt ->
                        val het = vt.tonKho < vt.tonToiThieu
                        Card(
                            onClick = { vtChon = vt.id; hienNhap = true },
                            colors = if (het) CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                            else CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerHighest),
                        ) {
                            Column(Modifier.padding(12.dp).fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text((if (vtChon == vt.id) "✓ " else "") + vt.ten, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                                    if (het) Text("SAP HET", color = Color(0xFFC62828), fontWeight = FontWeight.Bold)
                                }
                                Text("${vt.maVt} - Ton: ${vt.tonKho.toInt()} ${vt.donVi} - Toi thieu: ${vt.tonToiThieu.toInt()}", style = MaterialTheme.typography.bodySmall)
                                Text("Von TB: ${dinhDangTien(vt.giaNhapTB)} - Tri gia: ${dinhDangTien((vt.tonKho * vt.giaNhapTB).toLong())}", color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}
