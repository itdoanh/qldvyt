package vn.rinso.qldvyt.data.local

import java.util.Calendar
import java.util.UUID
import kotlinx.coroutines.flow.first
import vn.rinso.qldvyt.data.prefs.CaiDat
import vn.rinso.qldvyt.domain.pricing.TinhTien

private fun uuid(): String = UUID.randomUUID().toString()

/**
 * Seed dữ liệu mẫu đầy đủ kiểu MISA (GD4 trong doc 06):
 * - 4 cấp bậc, 12 NV chi tiết (SĐT, địa chỉ, STK, trạng thái, override lương)
 * - 15 dịch vụ đủ 6 nhóm sổ, 5 gói, 15 vật tư + 12 phiếu nhập
 * - 20 khách hàng chi tiết, 15 đơn đủ 4 trạng thái + trả góp nhiều lần
 * - 12 nhiệm vụ đủ 4 trạng thái (30%), chấm công 12 NV × 6 ngày, thưởng/phạt/tạm ứng ≥10
 * Chạy幂等: bảng nào thiếu mới bù bảng đó → máy cũ vẫn được bổ sung.
 */
suspend fun seedNeuChuaCo(db: QldvytDatabase, caiDat: CaiDat) {
    val gio = System.currentTimeMillis()

    // 1. Cấp bậc
    val capDao = db.capBacDao()
    val capHienCo = try { capDao.tatCa().first().map { it.id }.toSet() } catch (_: Exception) { emptySet() }
    val capTT = "cap-thuc-tap"; val capNV = "cap-nhan-vien"
    val capKT = "cap-ky-thuat"; val capQL = "cap-quan-ly"
    if (capTT !in capHienCo) capDao.them(CapBac(capTT, "Thực tập", "NV mới học việc, phụ việc", 4_000_000L, 0L, gio, gio))
    if (capNV !in capHienCo) capDao.them(CapBac(capNV, "Nhân viên", "NV chính thức", 7_000_000L, 300_000L, gio, gio))
    if (capKT !in capHienCo) capDao.them(CapBac(capKT, "Kỹ thuật viên chính", "NV tay nghề cao, đi ca khó", 9_000_000L, 500_000L, gio, gio))
    if (capQL !in capHienCo) capDao.them(CapBac(capQL, "Điều dưỡng trưởng", "Quản lý ca + chất lượng", 12_000_000L, 800_000L, gio, gio))

    // 2. Dịch vụ 15 món đủ 6 nhóm sổ
    val dvDao = db.dichVuDao()
    val dvHienCo = try { dvDao.tatCa().first().map { it.id }.toSet() } catch (_: Exception) { emptySet() }
    val dsDv = listOf(
        DichVu("dv-tiem-truyen", "DV01", "Tiêm truyền tại nhà", "Truyền nước, thuốc theo chỉ định BS", 500_000L, "lần", "DANGBAN", "TIEM_TRUYEN"),
        DichVu("dv-truyen-dam", "DV02", "Truyền đạm / vitamin", "Liệu trình 3-5 buổi", 650_000L, "lần", "DANGBAN", "TIEM_TRUYEN"),
        DichVu("dv-cat-chi", "DV03", "Cắt chỉ vết mổ", "Sát khuẩn + cắt chỉ tại nhà", 250_000L, "lần", "DANGBAN", "TIEM_TRUYEN"),
        DichVu("dv-thay-bang", "DV04", "Thay băng vết thương", "Sát khuẩn + thay băng", 200_000L, "lần", "DANGBAN", "TIEM_TRUYEN"),
        DichVu("dv-di-xe", "DV05", "Đưa đón bệnh nhân đi xe", "Xe + NV hỗ trợ lên xuống", 350_000L, "chuyến", "DANGBAN", "DI_XE_TRONG"),
        DichVu("dv-trong-benh", "DV06", "Trông bệnh nhân tại viện", "Ca 8 tiếng ngày", 450_000L, "ca", "DANGBAN", "DI_XE_TRONG"),
        DichVu("dv-trong-dem", "DV07", "Trông đêm tại viện", "Ca 22h–6h", 550_000L, "ca", "DANGBAN", "DI_XE_TRONG"),
        DichVu("dv-thu-tuc", "DV08", "Làm thủ tục hồ sơ", "Nhập/xuất viện, bảo hiểm", 300_000L, "lần", "DANGBAN", "THU_TUC"),
        DichVu("dv-giay-to", "DV09", "Hỗ trợ giấy tờ y tế", "Sao y, công chứng, giám định", 200_000L, "lần", "DANGBAN", "THU_TUC"),
        DichVu("dv-cham-soc-ngay", "DV10", "Chăm sóc người bệnh ban ngày", "Ca ngày tại nhà", 400_000L, "ca", "DANGBAN", "CHAM_SOC"),
        DichVu("dv-cham-soc-dem", "DV11", "Chăm sóc người bệnh ban đêm", "Ca đêm tại nhà", 500_000L, "ca", "DANGBAN", "CHAM_SOC"),
        DichVu("dv-sau-mo", "DV12", "Chăm sóc sau mổ tại nhà", "Thay băng + theo dõi 7 ngày", 600_000L, "ngày", "DANGBAN", "CHAM_SOC"),
        DichVu("dv-vat-ly", "DV13", "Vật lý trị liệu", "Phục hồi chức năng tại nhà", 450_000L, "buổi", "DANGBAN", "VAT_LY"),
        DichVu("dv-huyet-ap", "DV14", "Đo huyết áp / đường huyết", "Kiểm tra + tư vấn", 100_000L, "lần", "DANGBAN", "KHAC"),
        DichVu("dv-tu-van", "DV15", "Tư vấn sức khỏe tại nhà", "Điều dưỡng tư vấn 60 phút", 150_000L, "lần", "DANGBAN", "KHAC"),
    )
    dsDv.forEach { if (it.id !in dvHienCo) dvDao.them(it) }

    // 3. Gói
    val goiDao = db.goiDichVuDao()
    val goiHienCo = try { goiDao.dangBan().first().map { it.id }.toSet() } catch (_: Exception) { emptySet() }
    listOf(
        GoiDichVu("goi-sau-mo", "GOI01", "Gói chăm sóc sau mổ 7 ngày", "Thay băng + theo dõi mỗi ngày", 2_500_000L),
        GoiDichVu("goi-truyen", "GOI02", "Gói tiêm truyền 5 buổi", "Truyền theo liệu trình", 2_200_000L),
        GoiDichVu("goi-trong-tuan", "GOI03", "Gói trông viện 7 ngày", "Trông ca ngày", 2_900_000L),
        GoiDichVu("goi-cham-soc-thang", "GOI04", "Gói chăm sóc tháng (20 ca)", "Tại nhà cả tháng", 7_500_000L),
        GoiDichVu("goi-vat-ly-10", "GOI05", "Gói vật lý trị liệu 10 buổi", "Phục hồi chức năng", 4_000_000L),
    ).forEach { if (it.id !in goiHienCo) goiDao.them(it) }

    // 4. Vật tư 15 món
    val vtDao = db.vatTuDao()
    val vtHienCo = try { vtDao.tatCa().first().map { it.id }.toSet() } catch (_: Exception) { emptySet() }
    listOf(
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
        VatTu("vt-sua-tam", "VT15", "Sữa tắm khô y tế", "chai", 85_000L, 4.0, 5.0),
    ).forEach { if (it.id !in vtHienCo) vtDao.them(it) }

    // 5. Nhân viên 12 người chi tiết
    val nvDao = db.nhanVienDao()
    var dsNv = try { nvDao.tatCa().first() } catch (_: Exception) { emptyList() }
    if (dsNv.size < 12) {
        val mau = listOf(
            // ma, ten, cap, sdt, diaChi, trangThai, stk, ghiChu, override
            arrayOf("NV001", "Nguyễn Thị Lan", capQL, "0903111001", "12 Láng Hạ, Hà Nội", "DANGLAM", "190123456789", "Điều dưỡng trưởng", ""),
            arrayOf("NV002", "Trần Văn Hùng", capKT, "0903111002", "25 Giải Phóng, Hà Nội", "DANGLAM", "190123456790", "Tay nghề cao", ""),
            arrayOf("NV003", "Phạm Thị Hoa", capKT, "0903111003", "8 Bạch Mai, Hà Nội", "DANGLAM", "190123456791", "Chuyên ca đêm", ""),
            arrayOf("NV004", "Lê Thị Mai", capNV, "0903111004", "3 Kim Ngưu, Hà Nội", "DANGLAM", "190123456792", "", ""),
            arrayOf("NV005", "Hoàng Văn Nam", capNV, "0903111005", "45 Xã Đàn, Hà Nội", "DANGLAM", "190123456793", "Lái xe + hỗ trợ", ""),
            arrayOf("NV006", "Đỗ Thị Nga", capNV, "0903111006", "67 Trường Chinh, Hà Nội", "DANGLAM", "190123456794", "", "7500000"),
            arrayOf("NV007", "Vũ Thị Linh", capNV, "0903111007", "90 Định Công, Hà Nội", "TAMNGHI", "190123456795", "Nghỉ thai sản", ""),
            arrayOf("NV008", "Bùi Văn Tuấn", capTT, "0903111008", "11 Tam Trinh, Hà Nội", "DANGLAM", "190123456796", "Thực tập", ""),
            arrayOf("NV009", "Ngô Thị Thu", capTT, "0903111009", "22 Kim Đồng, Hà Nội", "DANGLAM", "190123456797", "Thực tập", ""),
            arrayOf("NV010", "Đặng Văn Phúc", capNV, "0903111010", "5 Lê Thanh Nghị, Hà Nội", "DANGLAM", "190123456798", "", ""),
            arrayOf("NV011", "Trịnh Thị Yến", capNV, "0903111011", "99 Minh Khai, Hà Nội", "DANGLAM", "190123456799", "Chuyên thủ tục", ""),
            arrayOf("NV012", "Phan Văn Đức", capNV, "0903111012", "7 Đại La, Hà Nội", "NGHIVIEC", "190123456800", "Đã nghỉ việc T9", ""),
        )
        val coMa = dsNv.map { it.maNv }.toSet()
        mau.forEach { m ->
            if (m[0] as String !in coMa) {
                val ov = (m[8] as String).toLongOrNull()
                nvDao.them(
                    NhanVien(
                        id = uuid(), maNv = m[0] as String, hoTen = m[1] as String,
                        sdt = m[3] as String, diaChi = m[4] as String, capBacId = m[2] as String,
                        trangThai = m[5] as String, stk = m[6] as String, ghiChu = m[7] as String,
                        luongCoBanOverride = ov,
                    )
                )
            }
        }
        dsNv = try { nvDao.tatCa().first() } catch (_: Exception) { emptyList() }
    }

    // 6. Khách hàng 20 người chi tiết
    val khDao = db.khachHangDao()
    var dsKh = try { khDao.tatCa().first() } catch (_: Exception) { emptyList() }
    if (dsKh.size < 20) {
        val coMa = dsKh.map { it.maKh }.toSet()
        val tenKh = listOf(
            Triple("Ông Nguyễn Văn An (70t, tiểu đường)", "0912111001", "Số 1 Hàng Bông, Hà Nội"),
            Triple("Bà Trần Thị Bình (65t, huyết áp)", "0912111002", "Số 5 Quán Sứ, Hà Nội"),
            Triple("Ông Lê Văn Cường (sau mổ ruột thừa)", "0912111003", "Chung cư Linh Đàm, Hà Nội"),
            Triple("Bà Phạm Thị Dung (chăm sóc sau sinh)", "0912111004", "KĐT Văn Quán, Hà Đông"),
            Triple("Ông Hoàng Văn Đức (tai biến nhẹ)", "0912111005", "Số 9 Cầu Giấy, Hà Nội"),
            Triple("Bà Vũ Thị Hà (truyền đạm định kỳ)", "0912111006", "Số 3 Thái Hà, Hà Nội"),
            Triple("Ông Đỗ Văn Hải (thay băng chân)", "0912111007", "Số 8 Lò Đúc, Hà Nội"),
            Triple("Bà Bùi Thị Kim (khách VIP)", "0912111008", "Biệt thự Ciputra, Hà Nội"),
            Triple("Ông Ngô Văn Long (đưa đón viện K)", "0912111009", "Số 2 Tố Hữu, Hà Nam"),
            Triple("Bà Đặng Thị Minh (trông đêm)", "0912111010", "Số 15 Nguyễn Trãi, Hà Nội"),
            Triple("Ông Phan Văn Nghĩa (vật lý trị liệu)", "0912111011", "Số 6 Hoàng Mai, Hà Nội"),
            Triple("Bà Trần Thị Oanh (làm thủ tục BH)", "0912111012", "Số 11 Thanh Nhàn, Hà Nội"),
            Triple("Ông Vũ Văn Phú (sau mổ xương)", "0912111013", "Số 4 Kim Liên, Hà Nội"),
            Triple("Bà Lê Thị Quyên (tư vấn SK)", "0912111014", "Số 7 Văn Miếu, Hà Nội"),
            Triple("Ông Phạm Văn Sơn (đo ĐH định kỳ)", "0912111015", "Số 10 Cát Linh, Hà Nội"),
            Triple("Bà Hoàng Thị Tâm (thân thiết)", "0912111016", "Số 21 Xã Đàn, Hà Nội"),
            Triple("Ông Trần Văn Thắng (trông viện)", "0912111017", "Số 33 Vọng, Hà Nội"),
            Triple("Bà Nguyễn Thị Uyên (chăm sóc mẹ)", "0912111018", "Số 44 Định Công, Hà Nội"),
            Triple("Ông Lê Văn Việt (truyền vitamin)", "0912111019", "Số 55 Tam Trinh, Hà Nội"),
            Triple("Bà Đỗ Thị Xuân (khách mới)", "0912111020", "Số 66 Minh Khai, Hà Nội"),
        )
        tenKh.forEachIndexed { i, (ten, sdt, dc) ->
            val ma = "KH%03d".format(i + 1)
            if (ma !in coMa) khDao.them(
                KhachHang(id = uuid(), maKh = ma, hoTen = ten, sdt = sdt, diaChi = dc, ghiChu = if (i % 4 == 0) "Khách thân thiết" else "")
            )
        }
        dsKh = try { khDao.tatCa().first() } catch (_: Exception) { emptyList() }
    }

    val cal = Calendar.getInstance()
    val nam = cal.get(Calendar.YEAR); val thang = cal.get(Calendar.MONTH)
    fun millis(ngay: Int, h: Int = 9): Long {
        val c = Calendar.getInstance().apply { set(nam, thang, ngay.coerceIn(1, 28), h, 0, 0); set(Calendar.MILLISECOND, 0) }
        return c.timeInMillis
    }
    fun dauNgay(ngay: Int): Long {
        val c = Calendar.getInstance().apply { set(nam, thang, ngay.coerceIn(1, 28), 0, 0, 0); set(Calendar.MILLISECOND, 0) }
        return c.timeInMillis
    }

    // 7. Nhập kho 12 phiếu (có NCC, giá vốn TB)
    val nkDao = db.nhapKhoDao()
    try {
        val vtAll = vtDao.tatCa().first()
        // Vì NhapKho không có đếm, kiểm tra gián tiếp: nếu tồn == mặc định thì nhập 1 lần
        if (dsNv.isNotEmpty() && vtAll.isNotEmpty()) {
            val daNhap = try { vtAll.sumOf { it.tonKho } > 1200 } catch (_: Exception) { true }
            if (!daNhap) {
                val ncc = listOf("Công ty Dược Hà Nội", "Nhà thuốc Long Châu sỉ", "Thiết bị y tế Việt Nhật")
                vtAll.take(12).forEachIndexed { i, vt ->
                    val sl = (20 + i * 5).toDouble()
                    val gia = (vt.giaNhapTB * (100 + i) / 100).coerceAtLeast(1_000L)
                    nkDao.them(NhapKho(uuid(), vt.id, millis((i % 20) + 1), sl, gia, ncc[i % 3], "Nhập định kỳ"))
                    val giaMoi = TinhTien.giaNhapBinhQuan(vt.tonKho, vt.giaNhapTB, sl, gia)
                    vtDao.sua(vt.copy(tonKho = vt.tonKho + sl, giaNhapTB = giaMoi))
                }
            }
        }
    } catch (_: Exception) { }

    // 8. Đơn 15 đơn đủ 4 trạng thái + chi tiết + vật tư + thanh toán nhiều lần
    val donDao = db.donHangDao()
    val donHienCo = try { donDao.moiNhat().first().size } catch (_: Exception) { 99 }
    val ctDao = db.donHangChiTietDao()
    val dvtDao = db.donHangVatTuDao()
    val ttDao = db.thanhToanDao()
    if (donHienCo == 0 && dsKh.size >= 15 && dsNv.isNotEmpty()) {
        val dvMap = try { dvDao.tatCa().first().associateBy { it.id } } catch (_: Exception) { emptyMap() }
        fun dv(id: String): Pair<String, Long> {
            val d = dvMap[id]
            return (d?.ten ?: "Dịch vụ") to (d?.giaNiemYet ?: 300_000L)
        }
        // 8 đơn hoàn thành đủ tiền
        val mauHoanThanh = listOf(
            Triple(0, 2, "dv-tiem-truyen"), Triple(1, 4, "dv-thay-bang"),
            Triple(2, 6, "dv-trong-benh"), Triple(3, 8, "dv-cham-soc-ngay"),
            Triple(4, 10, "dv-vat-ly"), Triple(5, 12, "dv-di-xe"),
            Triple(6, 14, "dv-thu-tuc"), Triple(7, 16, "dv-truyen-dam"),
        )
        mauHoanThanh.forEachIndexed { i, (kIdx, ngay, dvId) ->
            val (ten, gia) = dv(dvId)
            val giam = if (i % 3 == 0) 50_000L else 0L
            val tt = TinhTien.tongThanhToan(gia, giam)
            val donId = uuid()
            donDao.them(DonHang(id = donId, maDh = "DH%04d%02d-%03d".format(nam, thang + 1, i + 1), khachHangId = dsKh[kIdx].id, ngayTao = millis(ngay), ngayHen = null, trangThai = "HOAN_THANH", tongTienHang = gia, giamGia = giam, tienVat = 0L, tyLeVat = 0.0, tongThanhToan = tt, daThu = tt, conNo = 0L, ghiChu = "Thu đủ"))
            ctDao.them(DonHangChiTiet(uuid(), donId, dichVuId = dvId, tenHienThi = ten, soLuong = 1.0, donGia = gia, thanhTien = gia))
            ttDao.them(ThanhToan(uuid(), donId, dsKh[kIdx].id, millis(ngay, 15), tt, if (i % 2 == 0) "TIENMAT" else "CK", null, "Thu đủ"))
        }
        // 3 đơn nợ (DANG_LAM) — trả góp 2 lần
        val mauNo = listOf(
            Triple(8, 18, "dv-trong-dem" to 550_000L),
            Triple(9, 19, "dv-sau-mo" to 600_000L),
            Triple(10, 20, "dv-cham-soc-dem" to 500_000L),
        )
        mauNo.forEachIndexed { i, (kIdx, ngay, p) ->
            val (dvId, gia) = p
            val (ten, _) = dv(dvId)
            val tong = gia * 2 // 2 ca/buổi
            val donId = uuid()
            val daThu = if (i == 0) 400_000L else if (i == 1) 500_000L else 200_000L
            donDao.them(DonHang(id = donId, maDh = "DH-NO-%03d".format(i + 1), khachHangId = dsKh[kIdx].id, ngayTao = millis(ngay), ngayHen = null, trangThai = "DANG_LAM", tongTienHang = tong, giamGia = 0L, tienVat = 0L, tyLeVat = 0.0, tongThanhToan = tong, daThu = daThu, conNo = tong - daThu, ghiChu = "Khách trả góp"))
            ctDao.them(DonHangChiTiet(uuid(), donId, dichVuId = dvId, tenHienThi = ten, soLuong = 2.0, donGia = gia, thanhTien = tong))
            ttDao.them(ThanhToan(uuid(), donId, dsKh[kIdx].id, millis(ngay, 15), daThu / 2, "TIENMAT", null, "Đặt cọc lần 1"))
            ttDao.them(ThanhToan(uuid(), donId, dsKh[kIdx].id, millis(ngay, 18), daThu - daThu / 2, "CK", null, "Thu lần 2"))
        }
        // 2 đơn chờ
        listOf(Triple(11, 21, "dv-di-xe"), Triple(12, 22, "dv-tu-van")).forEachIndexed { i, (kIdx, ngay, dvId) ->
            val (ten, gia) = dv(dvId)
            val donId = uuid()
            donDao.them(DonHang(id = donId, maDh = "DH-CHO-%03d".format(i + 1), khachHangId = dsKh[kIdx].id, ngayTao = millis(ngay), ngayHen = null, trangThai = "CHO_XU_LY", tongTienHang = gia, giamGia = 0L, tienVat = 0L, tyLeVat = 0.0, tongThanhToan = gia, daThu = 0L, conNo = gia, ghiChu = "Chờ xác nhận"))
            ctDao.them(DonHangChiTiet(uuid(), donId, dichVuId = dvId, tenHienThi = ten, soLuong = 1.0, donGia = gia, thanhTien = gia))
        }
        // 2 đơn hủy
        listOf(Triple(13, 5, "dv-giay-to"), Triple(14, 7, "dv-huyet-ap")).forEachIndexed { i, (kIdx, ngay, dvId) ->
            val (ten, gia) = dv(dvId)
            val donId = uuid()
            donDao.them(DonHang(id = donId, maDh = "DH-HUY-%03d".format(i + 1), khachHangId = dsKh[kIdx].id, ngayTao = millis(ngay), ngayHen = null, trangThai = "HUY", tongTienHang = gia, giamGia = 0L, tienVat = 0L, tyLeVat = 0.0, tongThanhToan = gia, daThu = 0L, conNo = 0L, ghiChu = "Khách hủy"))
            ctDao.them(DonHangChiTiet(uuid(), donId, dichVuId = dvId, tenHienThi = ten, soLuong = 1.0, donGia = gia, thanhTien = gia))
        }
        // Vật tư cho 5 đơn đầu
        try {
            val vtAll = vtDao.tatCa().first()
            val dons = donDao.moiNhat().first().take(5)
            dons.forEachIndexed { i, d ->
                val vt = vtAll.getOrNull(i) ?: return@forEachIndexed
                dvtDao.them(vn.rinso.qldvyt.data.local.DonHangVatTu(uuid(), d.id, vt.id, 2.0, vt.giaNhapTB, vt.giaNhapTB * 2))
            }
        } catch (_: Exception) { }
    }

    // 9. Nhiệm vụ 12 việc đủ 4 trạng thái
    try {
        val nvAll = db.nhiemVuDao().tatCa().first()
        if (nvAll.isEmpty() && dsNv.size >= 6) {
            val nvRepo = vn.rinso.qldvyt.data.repo.NhiemVuRepository(db)
            val jobs = listOf(
                Triple("Tiêm truyền cho ông An", "TIEM_TRUYEN", 500_000L),
                Triple("Thay băng cho ông Hải", "THAY_BANG", 200_000L),
                Triple("Trông đêm bà Minh", "KHAC", 550_000L),
                Triple("Làm thủ tục cho bà Oanh", "THU_TUC", 300_000L),
                Triple("Chăm sóc ngày bà Hà", "KHAC", 400_000L),
                Triple("Vật lý trị liệu ông Nghĩa", "KHAC", 450_000L),
                Triple("Truyền đạm bà Hà", "TIEM_TRUYEN", 650_000L),
                Triple("Đưa đón ông Long đi viện K", "KHAC", 350_000L),
                Triple("Chăm sóc đêm bà Kim", "KHAC", 500_000L),
                Triple("Tư vấn SK bà Quyên", "KHAC", 150_000L),
                Triple("Đo ĐH ông Sơn", "KHAC", 100_000L),
                Triple("Cắt chỉ ông Cường", "TIEM_TRUYEN", 250_000L),
            )
            jobs.forEachIndexed { i, (ten, loai, gia) ->
                val nv = dsNv[i % dsNv.size]
                val id = nvRepo.giaoViec(nv.id, ten, loai, gia, 0.30)
                when {
                    i < 5 -> { nvRepo.nhanViec(id); nvRepo.hoanThanh(id) }
                    i < 8 -> { nvRepo.nhanViec(id) }
                    i == 8 || i == 9 -> { /* DAGIAO */ }
                    else -> { nvRepo.huy(id) }
                }
            }
        }
    } catch (_: Exception) { }

    // 10. Chấm công 12 NV × 6 ngày + thưởng/phạt/tạm ứng
    try {
        val ccDao = db.chamCongDao()
        val testNv = dsNv.firstOrNull() ?: throw IllegalStateException("no nv")
        val probe = try { ccDao.tongCong(testNv.id, dauNgay(1), dauNgay(6)) } catch (_: Exception) { 99.0 }
        if (probe == 0.0) {
            dsNv.take(12).forEachIndexed { i, nv ->
                (1..6).forEach { ng ->
                    val muon = (i + ng) % 9 == 0
                    val cong = if ((i + ng) % 7 == 0) 0.5 else 1.0
                    ccDao.cham(vn.rinso.qldvyt.data.local.ChamCong(uuid(), nv.id, dauNgay(ng), cong, muon, ""))
                }
            }
            val tpDao = db.thuongPhatDao()
            val mauTP = listOf(
                Triple(0, 500_000L, "Thưởng ca khó"), Triple(1, 300_000L, "Thưởng chuyên cần"),
                Triple(2, -200_000L, "Phạt đi muộn"), Triple(3, 200_000L, "Thưởng khách khen"),
                Triple(4, 400_000L, "Thưởng lễ"), Triple(5, -100_000L, "Phạt quên báo cáo"),
                Triple(6, 300_000L, "Thưởng tăng ca"), Triple(7, 150_000L, "Thưởng giới thiệu khách"),
                Triple(8, -50_000L, "Phạt đồng phục"), Triple(9, 250_000L, "Thưởng tháng"),
                Triple(10, 200_000L, "Thưởng trực đêm"), Triple(11, -150_000L, "Phạt thiếu vật tư"),
            )
            mauTP.forEach { (idx, tien, lydo) ->
                val nv = dsNv.getOrNull(idx) ?: return@forEach
                tpDao.them(vn.rinso.qldvyt.data.local.ThuongPhat(uuid(), nv.id, millis(15), if (tien > 0) "THUONG" else "PHAT", tien, lydo))
            }
            val tuDao = db.tamUngDao()
            dsNv.take(10).forEachIndexed { i, nv ->
                tuDao.them(vn.rinso.qldvyt.data.local.TamUng(uuid(), nv.id, millis(10), (500_000L + i * 200_000L), "Tạm ứng sinh hoạt T${thang + 1}"))
            }
        }
    } catch (_: Exception) { }

    try { caiDat.danhDauDaSeed() } catch (_: Exception) { }
}
