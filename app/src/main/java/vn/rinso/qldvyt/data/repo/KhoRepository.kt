package vn.rinso.qldvyt.data.repo

import androidx.room.withTransaction
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import vn.rinso.qldvyt.data.local.NhapKho
import vn.rinso.qldvyt.data.local.QldvytDatabase
import vn.rinso.qldvyt.data.local.VatTu
import vn.rinso.qldvyt.domain.pricing.TinhTien

/**
 * Repository kho: nhập kho (bình quân gia quyền), thêm vật tư.
 * Xuất kho theo đơn đã làm ở DonHangRepository (trừ tồn + snapshot giá vốn).
 */
@Singleton
class KhoRepository @Inject constructor(private val db: QldvytDatabase) {

    fun tatCa(): Flow<List<VatTu>> = db.vatTuDao().tatCa()
    fun sapHet(): Flow<List<VatTu>> = db.vatTuDao().sapHet()
    fun lichSuNhap(): Flow<List<NhapKho>> = db.nhapKhoDao().moiNhat()

    suspend fun themVatTu(ten: String, maVt: String, donVi: String, tonToiThieu: Double): String {
        require(ten.isNotBlank()) { "Nhập tên vật tư" }
        val id = UUID.randomUUID().toString()
        val ma = maVt.ifBlank { "VT" + (System.currentTimeMillis() % 100000) }
        db.vatTuDao().them(
            VatTu(id = id, maVt = ma, ten = ten.trim(), donVi = donVi.ifBlank { "cái" }, tonToiThieu = tonToiThieu.coerceAtLeast(0.0))
        )
        return id
    }

    /** Nhập kho: cập nhật giaNhapTB (bình quân) + tăng tồn trong 1 transaction. */
    suspend fun nhapKho(vatTuId: String, soLuong: Double, donGiaNhap: Long, nhaCungCap: String = "") {
        require(soLuong > 0) { "Số lượng nhập phải > 0" }
        require(donGiaNhap >= 0) { "Đơn giá phải >= 0" }
        db.withTransaction {
            val vt = db.vatTuDao().theoId(vatTuId) ?: error("Không tìm thấy vật tư")
            val giaMoi = TinhTien.giaNhapBinhQuan(vt.tonKho, vt.giaNhapTB, soLuong, donGiaNhap)
            db.nhapKhoDao().them(
                NhapKho(
                    id = UUID.randomUUID().toString(), vatTuId = vatTuId,
                    ngay = System.currentTimeMillis(), soLuong = soLuong,
                    donGiaNhap = donGiaNhap, nhaCungCap = nhaCungCap,
                )
            )
            db.vatTuDao().sua(vt.copy(tonKho = vt.tonKho + soLuong, giaNhapTB = giaMoi))
        }
    }
}
