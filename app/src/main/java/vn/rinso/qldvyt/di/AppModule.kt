package vn.rinso.qldvyt.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.rinso.qldvyt.data.backup.SaoLuuManager
import vn.rinso.qldvyt.data.local.QldvytDatabase
import vn.rinso.qldvyt.data.local.RoomDb
import vn.rinso.qldvyt.data.prefs.CaiDat
import javax.inject.Singleton

/** Hilt module: cung cấp Database local + DataStore + Backup (offline-first). */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext ctx: Context): QldvytDatabase =
        RoomDb.tao(ctx)

    @Provides @Singleton fun caiDat(@ApplicationContext ctx: Context): CaiDat = CaiDat(ctx)

    @Provides @Singleton fun saoLuu(@ApplicationContext ctx: Context): SaoLuuManager = SaoLuuManager(ctx)

    @Provides @Singleton fun capBacDao(db: QldvytDatabase) = db.capBacDao()
    @Provides @Singleton fun nhanVienDao(db: QldvytDatabase) = db.nhanVienDao()
    @Provides @Singleton fun chamCongDao(db: QldvytDatabase) = db.chamCongDao()
    @Provides @Singleton fun thuongPhatDao(db: QldvytDatabase) = db.thuongPhatDao()
    @Provides @Singleton fun tamUngDao(db: QldvytDatabase) = db.tamUngDao()
    @Provides @Singleton fun dichVuDao(db: QldvytDatabase) = db.dichVuDao()
    @Provides @Singleton fun goiDichVuDao(db: QldvytDatabase) = db.goiDichVuDao()
    @Provides @Singleton fun vatTuDao(db: QldvytDatabase) = db.vatTuDao()
    @Provides @Singleton fun nhapKhoDao(db: QldvytDatabase) = db.nhapKhoDao()
    @Provides @Singleton fun khachHangDao(db: QldvytDatabase) = db.khachHangDao()
    @Provides @Singleton fun donHangDao(db: QldvytDatabase) = db.donHangDao()
    @Provides @Singleton fun donHangChiTietDao(db: QldvytDatabase) = db.donHangChiTietDao()
    @Provides @Singleton fun donHangVatTuDao(db: QldvytDatabase) = db.donHangVatTuDao()
    @Provides @Singleton fun nhiemVuDao(db: QldvytDatabase) = db.nhiemVuDao()
    @Provides @Singleton fun thanhToanDao(db: QldvytDatabase) = db.thanhToanDao()
    @Provides @Singleton fun bangLuongDao(db: QldvytDatabase) = db.bangLuongDao()

    @Provides @Singleton
    fun donHangRepository(db: QldvytDatabase): vn.rinso.qldvyt.data.repo.DonHangRepository =
        vn.rinso.qldvyt.data.repo.DonHangRepository(db)

    @Provides @Singleton
    fun nhiemVuRepository(db: QldvytDatabase): vn.rinso.qldvyt.data.repo.NhiemVuRepository =
        vn.rinso.qldvyt.data.repo.NhiemVuRepository(db)

    @Provides @Singleton
    fun khoRepository(db: QldvytDatabase): vn.rinso.qldvyt.data.repo.KhoRepository =
        vn.rinso.qldvyt.data.repo.KhoRepository(db)

    @Provides @Singleton
    fun nhanVienRepository(db: QldvytDatabase): vn.rinso.qldvyt.data.repo.NhanVienRepository =
        vn.rinso.qldvyt.data.repo.NhanVienRepository(db)

    @Provides @Singleton
    fun luongRepository(
        db: QldvytDatabase,
        caiDat: CaiDat,
    ): vn.rinso.qldvyt.data.repo.LuongRepository =
        vn.rinso.qldvyt.data.repo.LuongRepository(db, caiDat)
}
