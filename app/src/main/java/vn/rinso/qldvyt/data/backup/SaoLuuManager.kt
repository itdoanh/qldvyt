package vn.rinso.qldvyt.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedOutputStream
import java.security.MessageDigest
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import javax.inject.Inject

/**
 * Quản lý sao lưu / khôi phục bằng FILE (.zip qua Storage Access Framework).
 * Không cần server, không cần quyền storage legacy.
 */
class SaoLuuManager @Inject constructor(@ApplicationContext private val ctx: Context) {

    /** Tạo file backup .zip tại [uri] do người dùng chọn. Trả về SHA-256 checksum. */
    fun taoBackup(uri: Uri): String {
        val dbFile = ctx.getDatabasePath("qldvyt.db")
        val digest = MessageDigest.getInstance("SHA-256")
        ctx.contentResolver.openOutputStream(uri)?.use { outRaw ->
            BufferedOutputStream(outRaw).use { out ->
                ZipOutputStream(out).use { zip ->
                    if (dbFile.exists()) {
                        zip.putNextEntry(ZipEntry("qldvyt.db"))
                        dbFile.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                    val meta = """{"app":"vn.rinso.qldvyt","ngay":"${
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("vi", "VN")).format(Date())
                    }","nhaPhatTrien":"Rinso (Thuoc Rin)","lapTrinhVien":"Doan Van Doanh (itdoanh)"}"""
                    zip.putNextEntry(ZipEntry("meta.json"))
                    zip.write(meta.toByteArray())
                    zip.closeEntry()
                }
            }
        }
        // checksum file vừa ghi
        ctx.contentResolver.openInputStream(uri)?.use { ins ->
            val buf = ByteArray(8192)
            var n: Int
            while (ins.read(buf).also { n = it } > 0) digest.update(buf, 0, n)
        }
        return digest.digest().joinToString("") { "%02x".format(it) }
    }
}
