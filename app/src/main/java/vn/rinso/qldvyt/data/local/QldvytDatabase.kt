package vn.rinso.qldvyt.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

/**
 * Database local duy nhất của app (offline-first, không server).
 * Version 3: VAT/thuế (dich_vu.chiuThue, don_hang.tienVat, bang_luong.thueTncn...).
 * Mọi thay đổi entity phải tăng version + viết Migration.
 * Xem docs/02-thiet-ke-database-room.md
 */
@Database(
    entities = [
        CapBac::class, NhanVien::class, ChamCong::class, ThuongPhat::class, TamUng::class,
        DichVu::class, GoiDichVu::class, GoiChiTiet::class,
        VatTu::class, NhapKho::class,
        KhachHang::class, DonHang::class, DonHangChiTiet::class, DonHangVatTu::class,
        NhiemVu::class, ThanhToan::class, BangLuong::class,
    ],
    version = 3,
    exportSchema = true,
)
abstract class QldvytDatabase : RoomDatabase() {
    abstract fun capBacDao(): CapBacDao
    abstract fun nhanVienDao(): NhanVienDao
    abstract fun chamCongDao(): ChamCongDao
    abstract fun thuongPhatDao(): ThuongPhatDao
    abstract fun tamUngDao(): TamUngDao
    abstract fun dichVuDao(): DichVuDao
    abstract fun goiDichVuDao(): GoiDichVuDao
    abstract fun vatTuDao(): VatTuDao
    abstract fun nhapKhoDao(): NhapKhoDao
    abstract fun khachHangDao(): KhachHangDao
    abstract fun donHangDao(): DonHangDao
    abstract fun donHangChiTietDao(): DonHangChiTietDao
    abstract fun donHangVatTuDao(): DonHangVatTuDao
    abstract fun nhiemVuDao(): NhiemVuDao
    abstract fun thanhToanDao(): ThanhToanDao
    abstract fun bangLuongDao(): BangLuongDao
}
