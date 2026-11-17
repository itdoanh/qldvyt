package vn.rinso.qldvyt.data.local

import java.util.Calendar
import java.util.UUID
import vn.rinso.qldvyt.data.prefs.CaiDat
import vn.rinso.qldvyt.domain.pricing.TinhTien
import kotlinx.coroutines.flow.first

private fun uuid(): String = UUID.randomUUID().toString()

/**
 * Seed dữ liệu mẫu phong phú (MISA-style): 4 cấp bậc, 10 NV, ~15 dịch vụ
 * theo nhóm sổ (TIEM_TRUYEN / DI_XE_TRONG / THU_TUC / CHAM_SOC / VAT_LY / KHAC),
 * 5 gói, ~15 vật tư, 20 khách hàng + 12 đơn mẫu tháng hiện tại.
 * Chạy幂等 (idempotent): bảng nào trống mới seed bảng đó → máy đã cài bản cũ
 * vẫn được bổ sung dữ liệu mới.
 */
suspend fun seedNeuChuaCo(db: QldvytDatabase, caiDat: CaiDat) {
    val gio = System.currentTimeMillis()

    // 1. Cấp bậc
    val capDao = db.capBacDao()
    // CapBacDao chưa có đếm → dùng tatCa() flow first()
    val capHienCo = try { capDao.tatCa().first().map { it.id }.toSet() } catch (_: Exception) { emptySet() }
    val capTT = "cap-thuc-tap"; val capNV = "cap-nhan-vien"
    val capKT = "cap-ky-thuat"; val capQL = "cap-quan-ly"
    if (capTT !in capHienCo) capDao.them(CapBac(capTT, "Thực tập", "NV mới học việc", 4_000_000L, 0L, gio, gio))
    if (capNV !in capHienCo) capDao.them(CapBac(capNV, "Nhân viên", "NV chính thức", 7_000_000L, 300_000L, gio, gio))
    if (capKT !in capHienCo) capDao.them(CapBac(capKT, "Kỹ thuật viên chính", "NV tay nghề cao", 9_000_000L, 500_000L, gio, gio))
    if (capQL !in capHienCo) capDao.them(CapBac(capQL, "Điều dưỡng trưởng", "Quản lý ca + chất lượng", 12_000_000L, 800_000L, gio, gio))

    // 2. Dịch vụ theo nhóm sổ doanh thu (giống ảnh sổ tay của công ty)
    val dvDao = db.dichVuDao()
    val dvHienCo = try { dvDao.tatCa().first().map { it.id }.toSet() } catch (_: Exception) { emptySet() }
    val dsDv = listOf(
        DichVu("dv-tiem-truyen", "DV01", "Tiêm truyền tại nhà", "Truyền nước, truyền thuốc theo chỉ định", 500_000L, "lần", "DANGBAN", "TIEM_TRUYEN"),
        DichVu("dv-truyen-dam", "DV02", "Truyền đạm / vitamin", "Theo liệu trình bác sĩ", 650_000L, "lần", "DANGBAN", "TIEM_TRUYEN"),
        DichVu("dv-cat-chi", "DV03", "Cắt chỉ vết mổ", "Sát khuẩn + cắt chỉ tại nhà", 250_000L, "lần", "DANGBAN", "TIEM_TRUYEN"),
        DichVu("dv-thay-bang", "DV04", "Thay băng vết thương", "Sát khuẩn + thay băng tại nhà", 200_000L, "lần", "DANGBAN", "TIEM_TRUYEN"),
        DichVu("dv-di-xe", "DV05", "Đưa đón bệnh nhân đi xe", "Xe + NV hỗ trợ lên xuống", 350_000L, "chuyến", "DANGBAN", "DI_XE_TRONG"),
        DichVu("dv-trong-benh", "DV06", "Trông bệnh nhân tại viện", "Theo ca 8 tiếng", 450_000L, "ca", "DANGBAN", "DI_XE_TRONG"),
        DichVu("dv-trong-dem", "DV07", "Trông đêm tại viện", "Ca đêm 22h–6h", 550_000L, "ca", "DANGBAN", "DI_XE_TRONG"),
        DichVu("dv-thu-tuc", "DV08", "Làm thủ tục hồ sơ", "Nhập/xuất viện, bảo hiểm", 300_000L, "lần", "DANGBAN", "THU_TUC"),
        DichVu("dv-giay-to", "DV09", "Hỗ trợ giấy tờ y tế", "Sao y, công chứng, giám định", 200_000L, "lần", "DANGBAN", "THU_TUC"),
        DichVu("dv-cham-soc-ngay", "DV10", "Chăm sóc người bệnh ban ngày", "Theo ca ngày tại nhà", 400_000L, "ca", "DANGBAN", "CHAM_SOC"),
        DichVu("dv-cham-soc-dem", "DV11", "Chăm sóc người bệnh ban đêm", "Theo ca đêm tại nhà", 500_000L, "ca", "DANGBAN", "CHAM_SOC"),
        DichVu("dv-sau-mo", "DV12", "Chăm sóc sau mổ tại nhà", "Thay băng + theo dõi", 600_000L, "ngày", "DANGBAN", "CHAM_SOC"),
        DichVu("dv-vat-ly", "DV13", "Vật lý trị liệu", "Phục hồi chức năng tại nhà", 450_000L, "buổi", "DANGBAN", "VAT_LY"),
        DichVu("dv-huyet-ap", "DV14", "Đo huyết áp / đường huyết", "Kiểm tra + tư vấn", 100_000L, "lần", "DANGBAN", "KHAC"),
        DichVu("dv-tu-van", "DV15", "Tư vấn sức khỏe tại nhà", "Điều dưỡng tư vấn 60 phút", 150_000L, "lần", "DANGBAN", "KHAC"),
    )
    dsDv.forEach { if (it.id !in dvHienCo) dvDao.them(it) }

    // 3. Gói dịch vụ
    val goiDao = db.goiDichVuDao()
    val goiHienCo = try { goiDao.dangBan().first().map { it.id }.toSet() } catch (_: Exception) { emptySet() }
    val dsGoi = listOf(
        GoiDichVu("goi-sau-mo", "GOI01", "Gói chăm sóc sau mổ 7 ngày", "Thay băng + theo dõi mỗi ngày", 2_500_000L),
        GoiDichVu("goi-truyen", "GOI02", "Gói tiêm truyền 5 buổi", "Truyền theo liệu trình bác sĩ", 2_200_000L),
        GoiDichVu("goi-trong-tuan", "GOI03", "Gói trông viện 7 ngày", "Trông ca ngày tại viện", 2_900_000L),
        GoiDichVu("goi-cham-soc-thang", "GOI04", "Gói chăm sóc tháng (20 ca)", "Chăm sóc tại nhà cả tháng", 7_500_000L),
        GoiDichVu("goi-vat-ly-10", "GOI05", "Gói vật lý trị liệu 10 buổi", "Phục hồi chức năng", 4_000_000L),
    )
    dsGoi.forEach { if (it.id !in goiHienCo) goiDao.them(it) }

    // 4. Vật tư (15 mặt hàng)
    val vtDao = db.vatTuDao()
    val vtHienCo = try { vtDao.tatCa().first().map { it.id }.toSet() } catch (_: Exception) { emptySet() }
    val dsVt = listOf(
        VatTu("vt-nuoc-muoi", "VT01", "Nước muối sinh lý 500ml", "chai", 25_000L, 50.0, 10.0),
        VatTu("vt-day-truyen", "VT02", "Dây truyền dịch", "bộ", 15_000L, 60.0, 10.0),
        VatTu("vt-kim-truyen", "VT03", "Kim luồn tĩnh mạch", "cái", 12_000L, 80.0, 15.0),
        VatTu("vt-bang-ga", "VT04", "Băng gạc vô khuẩn", "miếng", 8_000L, 100.0, 20.0),
        VatTu("vt-con", "VT05", "Cồn sát khuẩn 70 độ", "chai", 30_000L, 30.0, 5.0),
        VatTu("vt-gang", "VT06", "Găng tay y tế", "đôi", 5_000L, 200.0, 30.0),
        VatTu("vt-bong", "VT07", "Bông y tế thấm hút", "gói", 18_000L, 70.0, 15.0),
        VatTu("vt-bang-cuon", "VT08", "Băng cuộn co giãn", "cuộn", 12_000L, 90.0, 20.0),
        VatTu("vt-khau-trang", "VT09", "Khẩu trang y tế 4 lớp", "hộp", 45_000L, 40.0, 10.0),
        VatTu("vt-nuoc-cat", "VT10", "Dung dịch sát khuẩn tay", "chai", 55_000L, 35.0, 8.0),
        VatTu("vt-kim-tiem", "VT11", "Kim tiêm vô khuẩn", "cái", 3_000L, 300.0, 50.0),
        VatTu("vt-bom-tiem", "VT12", "Bơm tiêm 10ml", "cái", 6_000L, 250.0, 40.0),
        VatTu("vt-may-do", "VT13", "Que thử đường huyết", "hộp", 180_000L, 20.0, 5.0),
        VatTu("vt-ta-dan", "VT14", "Tã dán người lớn", "bịch", 120_000L, 25.0, 6.0),
        VatTu("vt-sua-tam", "VT15", "Sữa tắm khô y tế", "chai", 85_000L, 22.0, 5.0),
    )
    dsVt.forEach { if (it.id !in vtHienCo) vtDao.them(it) }

    // 5. Nhân viên mẫu 10 người
    val nvDao = db.nhanVienDao()
    val nvHienCo = try { nvDao.tatCa().first().size } catch (_: Exception) { 0 }
    if (nvHienCo == 0) {
        val dsNv = listOf(
            Triple("NV001", "Nguyễn Thị Lan", capQL), Triple("NV002", "Trần Văn Hùng", capKT),
            Triple("NV003", "Phạm Thị Hoa", capKT), Triple("NV004", "Lê Thị Mai", capNV),
            Triple("NV005", "Hoàng Văn Nam", capNV), Triple("NV006", "Đỗ Thị Nga", capNV),
            Triple("NV007", "Vũ Thị Linh", capNV), Triple("NV008", "Bùi Văn Tuấn", capTT),
            Triple("NV009", "Ngô Thị Thu", capTT), Triple("NV010", "Đặng Văn Phúc", capNV),
        )
        dsNv.forEachIndexed { i, (ma, ten, cap) ->
            nvDao.them(
                NhanVien(
                    id = uuid(), maNv = ma, hoTen = ten,
                    sdt = "090${1000000 + i * 137913}", diaChi = "Hà Nội",
                    capBacId = cap, trangThai = "DANGLAM",
                )
            )
        }
    }

    // 6. Khách hàng mẫu 20 người
    val khDao = db.khachHangDao()
    val khHienCo = try { khDao.tatCa().first().size } catch (_: Exception) { 0 }
    var dsKh: List<KhachHang> = emptyList()
    if (khHienCo == 0) {
        val tenKh = listOf(
            "Ông Nguyễn Văn An", "Bà Trần Thị Bình", "Ông Lê Văn Cường", "Bà Phạm Thị Dung",
            "Ông Hoàng Văn Đức", "Bà Vũ Thị Hà", "Ông Đỗ Văn Hải", "Bà Bùi Thị Kim",
            "Ông Ngô Văn Long", "Bà Đặng Thị Minh", "Ông Phan Văn Nghĩa", "Bà Trần Thị Oanh",
            "Ông Vũ Văn Phú", "Bà Lê Thị Quyên", "Ông Phạm Văn Sơn", "Bà Hoàng Thị Tâm",
            "Ông Trần Văn Thắng", "Bà Nguyễn Thị Uyên", "Ông Lê Văn Việt", "Bà Đỗ Thị Xuân",
        )
        dsKh = tenKh.mapIndexed { i, ten ->
            KhachHang(
                id = uuid(), maKh = "KH%03d".format(i + 1), hoTen = ten,
                sdt = "091${1000000 + i * 79193}", diaChi = "Hà Nội",
                ghiChu = if (i % 4 == 0) "Khách thân thiết" else "",
            )
        }
        dsKh.forEach { khDao.them(it) }
    } else {
        dsKh = try { khDao.tatCa().first() } catch (_: Exception) { emptyList() }
    }

    // 7. Đơn mẫu tháng hiện tại (12 đơn: 8 hoàn thành, 2 đang làm, 1 chờ, 1 hủy)
    // Để báo cáo/biểu đồ có số liệu ngay sau khi cài.
    val donDao = db.donHangDao()
    val donHienCo = try { donDao.moiNhat().first().size } catch (_: Exception) { 0 }
    if (donHienCo == 0 && dsKh.size >= 8) {
        val cal = Calendar.getInstance()
        val nam = cal.get(Calendar.YEAR); val thang = cal.get(Calendar.MONTH)
        fun millis(ngay: Int, gio9: Int = 9): Long {
            val c = Calendar.getInstance().apply { set(nam, thang, ngay.coerceIn(1, 28), gio9, 0, 0); set(Calendar.MILLISECOND, 0) }
            return c.timeInMillis
        }
        val ctDao = db.donHangChiTietDao()
        val ttDao = db.thanhToanDao()
        // (khIndex, ngay, tenDV, donGia, trangThai, daThuFull?)
        val mauDon = listOf(
            Triple(0, 2, "dv-tiem-truyen" to 500_000L), Triple(1, 4, "dv-thay-bang" to 200_000L),
            Triple(2, 6, "dv-trong-benh" to 450_000L), Triple(3, 8, "dv-cham-soc-ngay" to 400_000L),
            Triple(4, 10, "dv-vat-ly" to 450_000L), Triple(5, 12, "dv-di-xe" to 350_000L),
            Triple(6, 14, "dv-thu-tuc" to 300_000L), Triple(7, 16, "dv-truyen-dam" to 650_000L),
        )
        mauDon.forEachIndexed { i, (kIdx, ngay, dv) ->
            val (dvId, gia) = dv
            val tenDv = dsDv.firstOrNull { it.id == dvId }?.ten ?: "Dịch vụ"
            val tong = gia; val giam = if (i % 3 == 0) 50_000L else 0L
            val thanhToan = TinhTien.tongThanhToan(tong, giam)
            val donId = uuid()
            val maDh = "DH%04d%02d-%03d".format(nam, thang + 1, i + 1)
            donDao.them(
                DonHang(
                    id = donId, maDh = maDh, khachHangId = dsKh[kIdx].id,
                    ngayTao = millis(ngay), trangThai = "HOAN_THANH",
                    tongTienHang = tong, giamGia = giam,
                    tongThanhToan = thanhToan, daThu = thanhToan, conNo = 0L,
                )
            )
            ctDao.them(DonHangChiTiet(uuid(), donId, dichVuId = dvId, tenHienThi = tenDv, soLuong = 1.0, donGia = gia, thanhTien = gia))
            ttDao.them(ThanhToan(uuid(), donId, dsKh[kIdx].id, millis(ngay, 15), thanhToan, "TIENMAT", null, "Thu đủ"))
        }
        // 2 đơn nợ 1 phần + 1 chờ + 1 hủy
        val donNoId = uuid()
        donDao.them(
            DonHang(uuid(), "DH-NO-001", dsKh[8].id, millis(18), trangThai = "DANG_LAM", tongTienHang = 900_000L, giamGia = 0L, tongThanhToan = 900_000L, daThu = 400_000L, conNo = 500_000L)
        )
        ctDao.them(DonHangChiTiet(uuid(), donNoId, dichVuId = "dv-trong-dem", tenHienThi = "Trông đêm tại viện", soLuong = 1.0, donGia = 550_000L, thanhTien = 550_000L))
        ttDao.them(ThanhToan(uuid(), donNoId, dsKh[8].id, millis(18, 15), 400_000L, "CK", null, "Đặt cọc"))
        val donChoId = uuid()
        donDao.them(
            DonHang(uuid(), "DH-CHO-001", dsKh[9].id, millis(20), trangThai = "CHO_XU_LY", tongTienHang = 350_000L, giamGia = 0L, tongThanhToan = 350_000L, daThu = 0L, conNo = 350_000L)
        )
        ctDao.them(DonHangChiTiet(uuid(), donChoId, dichVuId = "dv-di-xe", tenHienThi = "Đưa đón bệnh nhân đi xe", soLuong = 1.0, donGia = 350_000L, thanhTien = 350_000L))
    }

    try { caiDat.danhDauDaSeed() } catch (_: Exception) { }
}
