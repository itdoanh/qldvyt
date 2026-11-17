package vn.rinso.qldvyt.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CapBacDao {
    @Query("SELECT * FROM cap_bac ORDER BY ten")
    fun tatCa(): Flow<List<CapBac>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(capBac: CapBac)
}

@Dao
interface NhanVienDao {
    @Query("SELECT * FROM nhan_vien WHERE trangThai = 'DANGLAM' ORDER BY hoTen")
    fun dangLam(): Flow<List<NhanVien>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(nv: NhanVien)

    @Update
    suspend fun sua(nv: NhanVien)
}

@Dao
interface DonHangDao {
    @Query("SELECT * FROM don_hang ORDER BY ngayTao DESC")
    fun moiNhat(): Flow<List<DonHang>>

    @Query("SELECT COALESCE(SUM(tongThanhToan),0) FROM don_hang WHERE trangThai='HOAN_THANH' AND ngayTao BETWEEN :tu AND :den")
    fun doanhThu(tu: Long, den: Long): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(don: DonHang)
}

@Dao
interface NhiemVuDao {
    @Query("SELECT * FROM nhiem_vu WHERE trangThai = :trangThai ORDER BY ngayGiao DESC")
    fun theoTrangThai(trangThai: String): Flow<List<NhiemVu>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(nv: NhiemVu)

    @Update
    suspend fun sua(nv: NhiemVu)
}

@Dao
interface VatTuDao {
    @Query("SELECT * FROM vat_tu ORDER BY ten")
    fun tatCa(): Flow<List<VatTu>>

    @Query("SELECT * FROM vat_tu WHERE tonKho < tonToiThieu ORDER BY tonKho")
    fun sapHet(): Flow<List<VatTu>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(vt: VatTu)
}

@Dao
interface KhachHangDao {
    @Query("SELECT * FROM khach_hang ORDER BY hoTen")
    fun tatCa(): Flow<List<KhachHang>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(kh: KhachHang)
}

@Dao
interface DichVuDao {
    @Query("SELECT * FROM dich_vu WHERE trangThai='DANGBAN' ORDER BY ten")
    fun dangBan(): Flow<List<DichVu>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(dv: DichVu)
}
