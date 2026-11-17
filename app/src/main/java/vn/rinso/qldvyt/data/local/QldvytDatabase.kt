package vn.rinso.qldvyt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Database local duy nhất của app (offline-first, không server).
 * Version 1 khởi đầu. Mọi thay đổi entity phải tăng version + viết Migration.
 */
@Database(
    entities = [CapBac::class, NhanVien::class, ChamCong::class, DichVu::class,
        GoiDichVu::class, VatTu::class, KhachHang::class, DonHang::class, NhiemVu::class],
    version = 1,
    exportSchema = true,
)
abstract class QldvytDatabase : RoomDatabase() {
    abstract fun capBacDao(): CapBacDao
    abstract fun nhanVienDao(): NhanVienDao
    abstract fun donHangDao(): DonHangDao
    abstract fun nhiemVuDao(): NhiemVuDao
    abstract fun vatTuDao(): VatTuDao
    abstract fun khachHangDao(): KhachHangDao
    abstract fun dichVuDao(): DichVuDao
}
