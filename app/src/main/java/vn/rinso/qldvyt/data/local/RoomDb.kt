package vn.rinso.qldvyt.data.local

import android.content.Context
import androidx.room.Room

/** Factory tạo Room database local duy nhất (offline-first). */
object RoomDb {
    fun tao(ctx: Context): QldvytDatabase =
        Room.databaseBuilder(ctx.applicationContext, QldvytDatabase::class.java, "qldvyt.db")
            .addMigrations(MIGRATION_1_2)
            .fallbackToDestructiveMigrationOnDowngrade()
            .build()
}
