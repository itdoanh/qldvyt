package vn.rinso.qldvyt.di

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import vn.rinso.qldvyt.data.local.QldvytDatabase
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "cai_dat")

/** Hilt module: cung cấp Database local + DataStore (offline-first). */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun database(@ApplicationContext ctx: Context): QldvytDatabase =
        Room.databaseBuilder(ctx, QldvytDatabase::class.java, "qldvyt.db")
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()

    @Provides @Singleton fun capBacDao(db: QldvytDatabase) = db.capBacDao()
    @Provides @Singleton fun nhanVienDao(db: QldvytDatabase) = db.nhanVienDao()
    @Provides @Singleton fun donHangDao(db: QldvytDatabase) = db.donHangDao()
    @Provides @Singleton fun nhiemVuDao(db: QldvytDatabase) = db.nhiemVuDao()
    @Provides @Singleton fun vatTuDao(db: QldvytDatabase) = db.vatTuDao()
    @Provides @Singleton fun khachHangDao(db: QldvytDatabase) = db.khachHangDao()
    @Provides @Singleton fun dichVuDao(db: QldvytDatabase) = db.dichVuDao()
}
