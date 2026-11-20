package vn.rinso.qldvyt.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import vn.rinso.qldvyt.core.CONG_CHUAN_MAC_DINH
import vn.rinso.qldvyt.core.SO_NGAY_CANH_BAO_NO_MAC_DINH
import vn.rinso.qldvyt.core.TY_LE_NHIEM_VU_MAC_DINH

private val Context.store by preferencesDataStore(name = "cai_dat")

/**
 * Cài đặt hệ thống lưu ở DataStore (key-value local, không cần server).
 * Bao gồm: công chuẩn, tỷ lệ nhiệm vụ, VAT/thuế toàn hệ thống + từng phần,
 * thông tin công ty in hóa đơn PDF, mục tiêu doanh thu, phụ cấp/phạt.
 */
@Singleton
class CaiDat @Inject constructor(@ApplicationContext private val ctx: Context) {

    private object Key {
        val CONG_CHUAN = doublePreferencesKey("congChuanThang")
        val TY_LE_NV = doublePreferencesKey("tyLeNhiemVuMacDinh")
        val NGAY_CANH_BAO_NO = intPreferencesKey("soNgayCanhBaoNo")
        val TEN_CONG_TY = stringPreferencesKey("tenCongTy")
        val DIA_CHI_CTY = stringPreferencesKey("diaChiCongTy")
        val SDT_CTY = stringPreferencesKey("sdtCongTy")
        val MA_SO_THUE = stringPreferencesKey("maSoThue")
        val PIN_KHOA = stringPreferencesKey("pinKhoaApp")
        val DA_SEED = booleanPreferencesKey("daSeedDemo")
        val CHE_DO_GIAO_DIEN = stringPreferencesKey("cheDoGiaoDien")
        // Thuế & VAT toàn hệ thống
        val VAT_BAT = booleanPreferencesKey("vatBatToanHeThong")
        val VAT_MAC_DINH = doublePreferencesKey("vatMacDinh")
        val VAT_AP_DUNG_DON = booleanPreferencesKey("vatApDungDonHang")
        val VAT_THEO_DICH_VU = booleanPreferencesKey("vatTheoTungDichVu")
        val THUE_TNCN_BAT = booleanPreferencesKey("thueTncnBat")
        val THUE_TNCN_MAC_DINH = doublePreferencesKey("thueTncnMacDinh")
        val HOA_HONG_MAC_DINH = doublePreferencesKey("hoaHongMacDinh")
        val PHU_CAP_MAC_DINH = doublePreferencesKey("phuCapMacDinh")
        val PHAT_DI_MUON = doublePreferencesKey("phatDiMuonMoiNgay")
        val MUC_TIEU_THANG = doublePreferencesKey("mucTieuThang")
    }

    val congChuanThang: Flow<Double> =
        ctx.store.data.map { it[Key.CONG_CHUAN] ?: CONG_CHUAN_MAC_DINH }
    val tyLeNhiemVu: Flow<Double> =
        ctx.store.data.map { it[Key.TY_LE_NV] ?: TY_LE_NHIEM_VU_MAC_DINH }
    val soNgayCanhBaoNo: Flow<Int> =
        ctx.store.data.map { it[Key.NGAY_CANH_BAO_NO] ?: SO_NGAY_CANH_BAO_NO_MAC_DINH }
    val tenCongTy: Flow<String> =
        ctx.store.data.map { it[Key.TEN_CONG_TY] ?: "Công ty Dịch vụ Chăm sóc Sức khỏe Rinso" }
    val diaChiCongTy: Flow<String> =
        ctx.store.data.map { it[Key.DIA_CHI_CTY] ?: "Hà Nội, Việt Nam" }
    val sdtCongTy: Flow<String> =
        ctx.store.data.map { it[Key.SDT_CTY] ?: "0903 111 000" }
    val maSoThue: Flow<String> =
        ctx.store.data.map { it[Key.MA_SO_THUE] ?: "" }
    val daSeedDemo: Flow<Boolean> =
        ctx.store.data.map { it[Key.DA_SEED] ?: false }
    /** SÁNG / TỐI / HỆ_THỐNG */
    val cheDoGiaoDien: Flow<String> =
        ctx.store.data.map { it[Key.CHE_DO_GIAO_DIEN] ?: "HE_THONG" }

    // ===== Thuế & VAT =====
    /** Bật VAT toàn hệ thống (tắt = mọi đơn/lương đều không tính VAT). */
    val vatBatToanHeThong: Flow<Boolean> =
        ctx.store.data.map { it[Key.VAT_BAT] ?: false }
    /** Thuế suất VAT mặc định (0.08 = 8%, 0.10 = 10%). */
    val vatMacDinh: Flow<Double> =
        ctx.store.data.map { it[Key.VAT_MAC_DINH] ?: 0.08 }
    /** Có áp VAT cho đơn hàng/phiếu dịch vụ không. */
    val vatApDungDonHang: Flow<Boolean> =
        ctx.store.data.map { it[Key.VAT_AP_DUNG_DON] ?: true }
    /** true = tính VAT theo từng dịch vụ (dịch vụ nào chịu thuế mới tính). false = tính gộp cả đơn. */
    val vatTheoTungDichVu: Flow<Boolean> =
        ctx.store.data.map { it[Key.VAT_THEO_DICH_VU] ?: true }
    /** Bật khấu trừ thuế TNCN trên lương. */
    val thueTncnBat: Flow<Boolean> =
        ctx.store.data.map { it[Key.THUE_TNCN_BAT] ?: false }
    val thueTncnMacDinh: Flow<Double> =
        ctx.store.data.map { it[Key.THUE_TNCN_MAC_DINH] ?: 0.0 }
    val hoaHongMacDinh: Flow<Double> =
        ctx.store.data.map { it[Key.HOA_HONG_MAC_DINH] ?: 0.0 }
    val phuCapMacDinh: Flow<Long> =
        ctx.store.data.map { (it[Key.PHU_CAP_MAC_DINH] ?: 0.0).toLong() }
    val phatDiMuon: Flow<Long> =
        ctx.store.data.map { (it[Key.PHAT_DI_MUON] ?: 0.0).toLong() }
    val mucTieuThang: Flow<Long> =
        ctx.store.data.map { (it[Key.MUC_TIEU_THANG] ?: 50_000_000.0).toLong() }

    suspend fun luuCheDoGiaoDien(cheDo: String) {
        val chuan = when (cheDo) { "SANG", "TOI", "HE_THONG" -> cheDo else -> "HE_THONG" }
        ctx.store.edit { it[Key.CHE_DO_GIAO_DIEN] = chuan }
    }
    suspend fun luuCongChuan(giaTri: Double) {
        ctx.store.edit { it[Key.CONG_CHUAN] = giaTri.coerceIn(1.0, 31.0) }
    }
    suspend fun luuTyLeNhiemVu(tyLe: Double) {
        ctx.store.edit { it[Key.TY_LE_NV] = tyLe.coerceIn(0.0, 1.0) }
    }
    suspend fun luuSoNgayCanhBaoNo(ngay: Int) {
        ctx.store.edit { it[Key.NGAY_CANH_BAO_NO] = ngay.coerceIn(1, 90) }
    }
    suspend fun luuTenCongTy(ten: String) { ctx.store.edit { it[Key.TEN_CONG_TY] = ten } }
    suspend fun luuDiaChiCongTy(d: String) { ctx.store.edit { it[Key.DIA_CHI_CTY] = d } }
    suspend fun luuSdtCongTy(s: String) { ctx.store.edit { it[Key.SDT_CTY] = s } }
    suspend fun luuMaSoThue(m: String) { ctx.store.edit { it[Key.MA_SO_THUE] = m } }
    suspend fun luuPin(pin: String) { ctx.store.edit { it[Key.PIN_KHOA] = pin } }
    suspend fun danhDauDaSeed() { ctx.store.edit { it[Key.DA_SEED] = true } }

    // VAT / thuế
    suspend fun luuVatBat(bat: Boolean) { ctx.store.edit { it[Key.VAT_BAT] = bat } }
    suspend fun luuVatMacDinh(tyLe: Double) {
        ctx.store.edit { it[Key.VAT_MAC_DINH] = tyLe.coerceIn(0.0, 0.2) }
    }
    suspend fun luuVatApDungDon(bat: Boolean) { ctx.store.edit { it[Key.VAT_AP_DUNG_DON] = bat } }
    suspend fun luuVatTheoDichVu(bat: Boolean) { ctx.store.edit { it[Key.VAT_THEO_DICH_VU] = bat } }
    suspend fun luuThueTncnBat(bat: Boolean) { ctx.store.edit { it[Key.THUE_TNCN_BAT] = bat } }
    suspend fun luuThueTncnMacDinh(tyLe: Double) {
        ctx.store.edit { it[Key.THUE_TNCN_MAC_DINH] = tyLe.coerceIn(0.0, 0.35) }
    }
    suspend fun luuHoaHongMacDinh(tyLe: Double) {
        ctx.store.edit { it[Key.HOA_HONG_MAC_DINH] = tyLe.coerceIn(0.0, 1.0) }
    }
    suspend fun luuPhuCapMacDinh(tien: Long) {
        ctx.store.edit { it[Key.PHU_CAP_MAC_DINH] = tien.coerceAtLeast(0L).toDouble() }
    }
    suspend fun luuPhatDiMuon(tien: Long) {
        ctx.store.edit { it[Key.PHAT_DI_MUON] = tien.coerceAtLeast(0L).toDouble() }
    }
    suspend fun luuMucTieuThang(tien: Long) {
        ctx.store.edit { it[Key.MUC_TIEU_THANG] = tien.coerceAtLeast(0L).toDouble() }
    }
}
