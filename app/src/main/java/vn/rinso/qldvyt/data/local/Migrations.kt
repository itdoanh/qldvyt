package vn.rinso.qldvyt.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Migration 1 -> 2: thêm cột `nhom` cho dich_vu (nhóm sổ doanh thu kiểu MISA/sổ tay). */
val MIGRATION_1_2 = object : Migration(1, 2) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE dich_vu ADD COLUMN nhom TEXT NOT NULL DEFAULT 'KHAC'")
    }
}
