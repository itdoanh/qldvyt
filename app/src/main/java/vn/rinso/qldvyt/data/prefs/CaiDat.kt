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

/** Cài đặt hệ thống lưu ở DataStore (key-value local, không cần server). */
@Singleton
class CaiDat @Inject constructor(@ApplicationContext private val ctx: Context) {

    private object Key {
        val CONG_CHUAN = doublePreferencesKey("congChuanThang")
        val TY_LE_NV = doublePreferencesKey("tyLeNhiemVuMacDinh")
        val NGAY_CANH_BAO_NO = intPreferencesKey("soNgayCanhBaoNo")
        val TEN_CONG_TY = stringPreferencesKey("tenCongTy")
        val PIN_KHOA = stringPreferencesKey("pinKhoaApp")
        val DA_SEED = booleanPreferencesKey("daSeedDemo")
        val CHE_DO_GIAO_DIEN = stringPreferencesKey("cheDoGiaoDien")
    }

    /** Số công chuẩn 1 tháng (mặc định 26). */
    val congChuanThang: Flow<Double> =
        ctx.store.data.map { it[Key.CONG_CHUAN] ?: CONG_CHUAN_MAC_DINH }

    /** Tỷ lệ NV hưởng trên nhiệm vụ (mặc định 0.30 = 30%). */
    val tyLeNhiemVu: Flow<Double> =
        ctx.store.data.map { it[Key.TY_LE_NV] ?: TY_LE_NHIEM_VU_MAC_DINH }

    /** Số ngày quá hạn thì cảnh báo công nợ (mặc định 7). */
    val soNgayCanhBaoNo: Flow<Int> =
        ctx.store.data.map { it[Key.NGAY_CANH_BAO_NO] ?: SO_NGAY_CANH_BAO_NO_MAC_DINH }

    val tenCongTy: Flow<String> =
        ctx.store.data.map { it[Key.TEN_CONG_TY] ?: "Công ty Dịch vụ Chăm sóc Sức khỏe" }

    val daSeedDemo: Flow<Boolean> =
        ctx.store.data.map { it[Key.DA_SEED] ?: false }

    /** Chế độ giao diện: SANG / TOI / HE_THONG (mặc định theo hệ thống). GD12. */
    val cheDoGiaoDien: Flow<String> =
        ctx.store.data.map { it[Key.CHE_DO_GIAO_DIEN] ?: "HE_THONG" }

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

    suspend fun luuTenCongTy(ten: String) {
        ctx.store.edit { it[Key.TEN_CONG_TY] = ten }
    }

    suspend fun luuPin(pin: String) {
        ctx.store.edit { it[Key.PIN_KHOA] = pin }
    }

    suspend fun danhDauDaSeed() {
        ctx.store.edit { it[Key.DA_SEED] = true }
    }
}
