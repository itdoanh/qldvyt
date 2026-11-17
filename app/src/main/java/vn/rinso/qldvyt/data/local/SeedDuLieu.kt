package vn.rinso.qldvyt.data.local

import java.util.UUID
import vn.rinso.qldvyt.data.prefs.CaiDat
import kotlinx.coroutines.flow.first

private fun maNgauNhien(): String = UUID.randomUUID().toString()

/**
 * Seed dữ liệu demo lần đầu (cấp bậc, dịch vụ, gói, vật tư).
 * Chỉ chạy 1 lần, gắn cờ daSeedDemo trong DataStore.
 */
suspend fun seedNeuChuaCo(db: QldvytDatabase, caiDat: CaiDat) {
    if (caiDat.daSeedDemo.first()) return

    val capBacDao = db.capBacDao()
    val gio = System.currentTimeMillis()
    val capTT = CapBac(maNgauNhien().let { "cap-thuc-tap" }, "Thực tập", "NV mới học việc", 4_000_000L, 0L, gio, gio)
    val capNV = CapBac("cap-nhan-vien", "Nhân viên", "NV chính thức", 7_000_000L, 300_000L, gio, gio)
    val capKT = CapBac("cap-ky-thuat", "Kỹ thuật viên chính", "NV tay nghề cao", 9_000_000L, 500_000L, gio, gio)
    capBacDao.them(capTT)
    capBacDao.them(capNV)
    capBacDao.them(capKT)

    val dichVuDao = db.dichVuDao()
    val dsDichVu = listOf(
        DichVu("dv-tiem-truyen", "DV01", "Tiêm truyền tại nhà", "Truyền nước, truyền thuốc theo chỉ định", 500_000L),
        DichVu("dv-thay-bang", "DV02", "Thay băng vết thương", "Sát khuẩn + thay băng tại nhà", 200_000L),
        DichVu("dv-thu-tuc", "DV03", "Làm thủ tục hồ sơ", "Hỗ trợ thủ tục khám/chuyển viện/bảo hiểm", 300_000L),
        DichVu("dv-cham-soc", "DV04", "Chăm sóc người bệnh tại nhà", "Theo ca ngày/đêm", 400_000L),
        DichVu("dv-vat-ly", "DV05", "Vật lý trị liệu", "Phục hồi chức năng tại nhà", 450_000L),
    )
    dsDichVu.forEach { dichVuDao.them(it) }

    val goiDao = db.goiDichVuDao()
    goiDao.them(GoiDichVu("goi-sau-mo", "GOI01", "Gói chăm sóc sau mổ 7 ngày", "Thay băng + theo dõi mỗi ngày", 2_500_000L))
    goiDao.them(GoiDichVu("goi-truyen", "GOI02", "Gói tiêm truyền 5 buổi", "Truyền theo liệu trình bác sĩ", 2_200_000L))

    val vatTuDao = db.vatTuDao()
    val dsVatTu = listOf(
        VatTu("vt-nuoc-muoi", "VT01", "Nước muối sinh lý 500ml", "chai", 25_000L, 50.0, 10.0),
        VatTu("vt-day-truyen", "VT02", "Dây truyền dịch", "bộ", 15_000L, 60.0, 10.0),
        VatTu("vt-kim-truyen", "VT03", "Kim luồn tĩnh mạch", "cái", 12_000L, 80.0, 15.0),
        VatTu("vt-bang-ga", "VT04", "Băng gạc vô khuẩn", "miếng", 8_000L, 100.0, 20.0),
        VatTu("vt-con", "VT05", "Cồn sát khuẩn 70 độ", "chai", 30_000L, 30.0, 5.0),
        VatTu("vt-gang", "VT06", "Găng tay y tế", "đôi", 5_000L, 200.0, 30.0),
    )
    dsVatTu.forEach { vatTuDao.them(it) }

    caiDat.danhDauDaSeed()
}
