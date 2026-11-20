package vn.rinso.qldvyt.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Migration 1 -> 2: thêm cột `nhom` cho dich_vu (nhóm sổ doanh thu kiểu MISA/sổ tay). */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE dich_vu ADD COLUMN nhom TEXT NOT NULL DEFAULT 'KHAC'")
    }
}

/**
 * Migration 2 -> 3: hệ thống Thuế & VAT.
 * - dich_vu: chiuThue (1/0), tyLeVatRieng (nullable)
 * - don_hang: tienVat, tyLeVat
 * - don_hang_chi_tiet: chiuThue
 * - bang_luong: phuCap, thueTncn
 */
val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE dich_vu ADD COLUMN chiuThue INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE dich_vu ADD COLUMN tyLeVatRieng REAL")
        db.execSQL("ALTER TABLE don_hang ADD COLUMN tienVat INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE don_hang ADD COLUMN tyLeVat REAL NOT NULL DEFAULT 0.0")
        db.execSQL("ALTER TABLE don_hang_chi_tiet ADD COLUMN chiuThue INTEGER NOT NULL DEFAULT 1")
        db.execSQL("ALTER TABLE bang_luong ADD COLUMN phuCap INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE bang_luong ADD COLUMN thueTncn INTEGER NOT NULL DEFAULT 0")
    }
}
