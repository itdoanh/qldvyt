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

    @Query("SELECT * FROM cap_bac WHERE id = :id")
    suspend fun theoId(id: String): CapBac?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(capBac: CapBac)

    @Query("DELETE FROM cap_bac WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface NhanVienDao {
    @Query("SELECT * FROM nhan_vien ORDER BY hoTen")
    fun tatCa(): Flow<List<NhanVien>>

    @Query("SELECT * FROM nhan_vien WHERE trangThai = 'DANGLAM' ORDER BY hoTen")
    fun dangLam(): Flow<List<NhanVien>>

    @Query("SELECT * FROM nhan_vien WHERE id = :id")
    suspend fun theoId(id: String): NhanVien?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(nv: NhanVien)

    @Update
    suspend fun sua(nv: NhanVien)
}

@Dao
interface ChamCongDao {
    @Query("SELECT * FROM cham_cong WHERE nhanVienId = :nvId AND ngay BETWEEN :tu AND :den ORDER BY ngay")
    fun theoKy(nvId: String, tu: Long, den: Long): Flow<List<ChamCong>>

    @Query("SELECT COALESCE(SUM(soCong),0) FROM cham_cong WHERE nhanVienId = :nvId AND ngay BETWEEN :tu AND :den")
    suspend fun tongCong(nvId: String, tu: Long, den: Long): Double

    @Query("SELECT COUNT(*) FROM cham_cong WHERE nhanVienId = :nvId AND ngay BETWEEN :tu AND :den AND diMuon = 1")
    suspend fun soNgayDiMuon(nvId: String, tu: Long, den: Long): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun cham(cc: ChamCong)
}

@Dao
interface ThuongPhatDao {
    @Query("SELECT * FROM thuong_phat WHERE nhanVienId = :nvId AND ngay BETWEEN :tu AND :den ORDER BY ngay DESC")
    fun theoKy(nvId: String, tu: Long, den: Long): Flow<List<ThuongPhat>>

    @Query("SELECT COALESCE(SUM(soTien),0) FROM thuong_phat WHERE nhanVienId = :nvId AND ngay BETWEEN :tu AND :den AND soTien > 0")
    suspend fun tongThuong(nvId: String, tu: Long, den: Long): Long

    @Query("SELECT COALESCE(SUM(-soTien),0) FROM thuong_phat WHERE nhanVienId = :nvId AND ngay BETWEEN :tu AND :den AND soTien < 0")
    suspend fun tongPhat(nvId: String, tu: Long, den: Long): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(tp: ThuongPhat)

    @Query("DELETE FROM thuong_phat WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface TamUngDao {
    @Query("SELECT COALESCE(SUM(soTien),0) FROM tam_ung WHERE nhanVienId = :nvId AND ngay BETWEEN :tu AND :den")
    suspend fun tongTamUng(nvId: String, tu: Long, den: Long): Long

    @Query("SELECT * FROM tam_ung WHERE nhanVienId = :nvId AND ngay BETWEEN :tu AND :den ORDER BY ngay DESC")
    fun theoKy(nvId: String, tu: Long, den: Long): Flow<List<TamUng>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(tu: TamUng)

    @Query("DELETE FROM tam_ung WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface DonHangDao {
    @Query("SELECT * FROM don_hang ORDER BY ngayTao DESC")
    fun moiNhat(): Flow<List<DonHang>>

    @Query("SELECT * FROM don_hang WHERE trangThai = :tt ORDER BY ngayTao DESC")
    fun theoTrangThai(tt: String): Flow<List<DonHang>>

    @Query("SELECT * FROM don_hang WHERE ngayTao BETWEEN :tu AND :den ORDER BY ngayTao DESC")
    suspend fun trongKy(tu: Long, den: Long): List<DonHang>

    @Query("SELECT COALESCE(SUM(tongThanhToan),0) FROM don_hang WHERE trangThai='HOAN_THANH' AND ngayTao BETWEEN :tu AND :den")
    fun doanhThu(tu: Long, den: Long): Flow<Long>

    @Query("SELECT COALESCE(SUM(tongThanhToan),0) FROM don_hang WHERE trangThai='HOAN_THANH' AND ngayTao BETWEEN :tu AND :den")
    suspend fun doanhThuMotLan(tu: Long, den: Long): Long

    @Query("SELECT COALESCE(SUM(conNo),0) FROM don_hang WHERE conNo > 0 AND trangThai != 'HUY'")
    fun tongCongNo(): Flow<Long>

    @Query("SELECT * FROM don_hang WHERE id = :id")
    suspend fun theoId(id: String): DonHang?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(don: DonHang)

    @Update
    suspend fun sua(don: DonHang)
}

@Dao
interface DonHangChiTietDao {
    @Query("SELECT * FROM don_hang_chi_tiet")
    fun tatCa(): Flow<List<DonHangChiTiet>>

    @Query("SELECT * FROM don_hang_chi_tiet WHERE donHangId = :donId")
    suspend fun theoDon(donId: String): List<DonHangChiTiet>

    @Query("SELECT ct.* FROM don_hang_chi_tiet ct INNER JOIN don_hang d ON d.id = ct.donHangId WHERE d.ngayTao BETWEEN :tu AND :den AND d.trangThai = 'HOAN_THANH'")
    suspend fun trongKyHoanThanh(tu: Long, den: Long): List<DonHangChiTiet>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(ct: DonHangChiTiet)
}

@Dao
interface DonHangVatTuDao {
    @Query("SELECT * FROM don_hang_vat_tu WHERE donHangId = :donId")
    suspend fun theoDon(donId: String): List<DonHangVatTu>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(vt: DonHangVatTu)
}

@Dao
interface NhiemVuDao {
    @Query("SELECT * FROM nhiem_vu ORDER BY ngayGiao DESC")
    fun tatCa(): Flow<List<NhiemVu>>

    @Query("SELECT * FROM nhiem_vu WHERE trangThai = :trangThai ORDER BY ngayGiao DESC")
    fun theoTrangThai(trangThai: String): Flow<List<NhiemVu>>

    @Query("SELECT COALESCE(SUM(tienNvHuong),0) FROM nhiem_vu WHERE nhanVienId = :nvId AND trangThai='HOANTHANH' AND ngayHoanThanh BETWEEN :tu AND :den")
    suspend fun tienNhiemVuKy(nvId: String, tu: Long, den: Long): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(nv: NhiemVu)

    @Update
    suspend fun sua(nv: NhiemVu)

    @Query("DELETE FROM nhiem_vu WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface ThanhToanDao {
    @Query("SELECT * FROM thanh_toan WHERE donHangId = :donId ORDER BY ngay DESC")
    fun theoDon(donId: String): Flow<List<ThanhToan>>

    @Query("SELECT COALESCE(SUM(soTien),0) FROM thanh_toan WHERE donHangId = :donId")
    suspend fun daThu(donId: String): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(tt: ThanhToan)

    @Query("DELETE FROM thanh_toan WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface VatTuDao {
    @Query("SELECT * FROM vat_tu ORDER BY ten")
    fun tatCa(): Flow<List<VatTu>>

    @Query("SELECT * FROM vat_tu WHERE tonKho < tonToiThieu ORDER BY tonKho")
    fun sapHet(): Flow<List<VatTu>>

    @Query("SELECT * FROM vat_tu WHERE id = :id")
    suspend fun theoId(id: String): VatTu?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(vt: VatTu)

    @Update
    suspend fun sua(vt: VatTu)

    @Query("DELETE FROM vat_tu WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface NhapKhoDao {
    @Query("SELECT * FROM nhap_kho ORDER BY ngay DESC LIMIT 100")
    fun moiNhat(): Flow<List<NhapKho>>

    @Query("SELECT * FROM nhap_kho WHERE vatTuId = :vatTuId ORDER BY ngay DESC LIMIT 20")
    fun theoVatTu(vatTuId: String): Flow<List<NhapKho>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(nk: NhapKho)
}

@Dao
interface KhachHangDao {
    @Query("SELECT * FROM khach_hang ORDER BY hoTen")
    fun tatCa(): Flow<List<KhachHang>>

    @Query("SELECT * FROM khach_hang WHERE id = :id")
    suspend fun theoId(id: String): KhachHang?

    @Query("SELECT * FROM khach_hang WHERE sdt LIKE '%' || :tuKhoa || '%' OR hoTen LIKE '%' || :tuKhoa || '%' ORDER BY hoTen")
    fun timKiem(tuKhoa: String): Flow<List<KhachHang>>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun them(kh: KhachHang)

    @Update
    suspend fun sua(kh: KhachHang)

    @Query("DELETE FROM khach_hang WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface DichVuDao {
    @Query("SELECT * FROM dich_vu WHERE trangThai='DANGBAN' ORDER BY ten")
    fun dangBan(): Flow<List<DichVu>>

    @Query("SELECT * FROM dich_vu ORDER BY ten")
    fun tatCa(): Flow<List<DichVu>>

    @Query("SELECT * FROM dich_vu WHERE id = :id")
    suspend fun theoId(id: String): DichVu?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(dv: DichVu)

    @Update
    suspend fun sua(dv: DichVu)

    @Query("DELETE FROM dich_vu WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface GoiDichVuDao {
    @Query("SELECT * FROM goi_dich_vu WHERE trangThai='DANGBAN' ORDER BY ten")
    fun dangBan(): Flow<List<GoiDichVu>>

    @Query("SELECT * FROM goi_dich_vu ORDER BY ten")
    fun tatCa(): Flow<List<GoiDichVu>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun them(goi: GoiDichVu)

    @Update
    suspend fun sua(goi: GoiDichVu)

    @Query("DELETE FROM goi_dich_vu WHERE id = :id")
    suspend fun xoa(id: String)
}

@Dao
interface BangLuongDao {
    @Query("SELECT * FROM bang_luong WHERE thang = :thang AND nam = :nam ORDER BY thucLinh DESC")
    fun theoThang(thang: Int, nam: Int): Flow<List<BangLuong>>

    @Query("SELECT * FROM bang_luong WHERE nhanVienId = :nvId AND thang = :thang AND nam = :nam")
    suspend fun theoNvKy(nvId: String, thang: Int, nam: Int): BangLuong?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun luu(bl: BangLuong)
}
