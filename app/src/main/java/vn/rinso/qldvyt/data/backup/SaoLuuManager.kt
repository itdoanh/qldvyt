package vn.rinso.qldvyt.data.backup

import android.content.Context
import android.net.Uri
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import java.util.zip.ZipOutputStream
import javax.inject.Inject
import vn.rinso.qldvyt.core.Sha256

/**
 * Sao lưu / khôi phục bằng FILE (.zip qua Storage Access Framework).
 * Không cần server, không cần quyền storage legacy.
 * Xem docs/02-thiet-ke-database-room.md §4
 */
class SaoLuuManager @Inject constructor(@ApplicationContext private val ctx: Context) {

    data class KetQuaBackup(val tenFile: String, val sha256: String, val dungLuong: Long)
    data class ThongTinBackup(val ngay: String, val sha256: String, val hopLe: Boolean)

    /** Tên file gợi ý: qldvyt-backup-yyyyMMdd-HHmmss.zip */
    fun tenFileGoiY(): String {
        val stamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale("vi", "VN")).format(Date())
        return "qldvyt-backup-$stamp.zip"
    }

    /** Tạo file backup .zip tại [uri] do người dùng chọn. Trả về checksum + dung lượng. */
    fun taoBackup(uri: Uri): KetQuaBackup {
        val dbFile = ctx.getDatabasePath("qldvyt.db")
        // Checkpoint WAL: đảm bảo db chính đầy đủ trước khi copy (Room tự checkpoint khi close;
        // ở đây copy cả -wal/-shm kèm theo để khôi phục chắc chắn).
        ctx.contentResolver.openOutputStream(uri, "wt")?.use { outRaw ->
            BufferedOutputStream(outRaw).use { out ->
                ZipOutputStream(out).use { zip ->
                    var soFile = 0
                    listOf(dbFile, ctx.getDatabasePath("qldvyt.db-wal"), ctx.getDatabasePath("qldvyt.db-shm")).forEach { f ->
                        if (f.exists()) {
                            zip.putNextEntry(ZipEntry(f.name))
                            f.inputStream().use { it.copyTo(zip) }
                            zip.closeEntry()
                            soFile++
                        }
                    }
                    // DataStore preferences
                    val prefsDir = ctx.filesDir?.parentFile?.resolve("shared_prefs")
                    prefsDir?.listFiles { f -> f.name.startsWith("cai_dat") }?.forEach { f ->
                        zip.putNextEntry(ZipEntry("shared_prefs/${f.name}"))
                        f.inputStream().use { it.copyTo(zip) }
                        zip.closeEntry()
                    }
                    val meta = """{"app":"vn.rinso.qldvyt","ngay":"${
                        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale("vi", "VN")).format(Date())
                    }","dbVersion":1,"nhaPhatTrien":"Rinso (Thuoc Rin)","lapTrinhVien":"Doan Van Doanh (itdoanh)","soFileDb":$soFile}"""
                    zip.putNextEntry(ZipEntry("meta.json"))
                    zip.write(meta.toByteArray(Charsets.UTF_8))
                    zip.closeEntry()
                }
            }
        }
        var dungLuong = 0L
        var sha = ""
        ctx.contentResolver.openInputStream(uri)?.use { ins ->
            val bytes = ins.readBytes()
            dungLuong = bytes.size.toLong()
            sha = Sha256.cuaBytes(bytes)
        }
        return KetQuaBackup(tenFileGoiY(), sha, dungLuong)
    }

    /** Đọc thông tin file backup (không ghi đè gì cả). */
    fun docThongTin(uri: Uri): ThongTinBackup {
        var meta = ""
        var coDb = false
        try {
            ctx.contentResolver.openInputStream(uri)?.use { ins ->
                ZipInputStream(BufferedInputStream(ins)).use { zip ->
                    var e = zip.nextEntry
                    while (e != null) {
                        if (e.name == "qldvyt.db") coDb = true
                        if (e.name == "meta.json") meta = zip.readBytes().toString(Charsets.UTF_8)
                        zip.closeEntry()
                        e = zip.nextEntry
                    }
                }
            }
        } catch (_: Exception) {
            return ThongTinBackup("", "", false)
        }
        // Trích ngày từ meta.json (đơn giản, không cần lib JSON)
        val ngay = Regex(""""ngay":"([^"]+)"""").find(meta)?.groupValues?.getOrNull(1) ?: ""
        var sha = ""
        try {
            ctx.contentResolver.openInputStream(uri)?.use { sha = Sha256.cuaStream(it) }
        } catch (_: Exception) { }
        return ThongTinBackup(ngay, sha, coDb)
    }

    /**
     * Khôi phục từ file backup. Quy trình an toàn:
     * 1. Đọc + kiểm tra file có qldvyt.db không.
     * 2. Tự backup DB hiện tại ra cache (qldvyt-truoc-khoi-phuc.db) để chống mất.
     * 3. Chép đè db + prefs.
     * Lưu ý: caller phải restart app/process sau khi restore để Room mở lại DB.
     */
    fun khoiPhuc(uri: Uri) {
        val dbFile = ctx.getDatabasePath("qldvyt.db")
        // 1. Backup hiện tại ra cache
        if (dbFile.exists()) {
            val cache = ctx.cacheDir.resolve("qldvyt-truoc-khoi-phuc.db")
            dbFile.copyTo(cache, overwrite = true)
        }
        // 2. Đóng journal cũ
        listOf("qldvyt.db-wal", "qldvyt.db-shm").forEach { ctx.getDatabasePath(it).delete() }
        // 3. Giải nén đè vào vị trí DB + prefs
        ctx.contentResolver.openInputStream(uri)?.use { ins ->
            ZipInputStream(BufferedInputStream(ins)).use { zip ->
                var e = zip.nextEntry
                var thayDb = false
                while (e != null) {
                    when {
                        e.name == "qldvyt.db" -> {
                            dbFile.parentFile?.mkdirs()
                            dbFile.outputStream().use { zip.copyTo(it) }
                            thayDb = true
                        }
                        e.name == "qldvyt.db-wal" || e.name == "qldvyt.db-shm" -> {
                            val f = ctx.getDatabasePath(e.name)
                            f.outputStream().use { zip.copyTo(it) }
                        }
                        e.name.startsWith("shared_prefs/") -> {
                            val dir = ctx.filesDir?.parentFile?.resolve("shared_prefs")
                            dir?.mkdirs()
                            val f = dir?.resolve(e.name.removePrefix("shared_prefs/")) ?: error("thieu dir prefs")
                            f.outputStream().use { zip.copyTo(it) }
                        }
                    }
                    zip.closeEntry()
                    e = zip.nextEntry
                }
                if (!thayDb) error("File backup thiếu qldvyt.db")
            }
        }
    }

    /** Xuất 1 bảng Room ra CSV (mở bằng Excel). Hiện hỗ trợ: don_hang, nhan_vien, vat_tu. */
    fun xuatCsv(tenBang: String, uri: Uri, hang: List<Map<String, Any?>>) {
        val tieuDe = hang.firstOrNull()?.keys?.joinToString(",") ?: ""
        val sb = StringBuilder("\uFEFF") // BOM để Excel mở tiếng Việt đúng
        sb.appendLine(tieuDe)
        hang.forEach { dong ->
            sb.appendLine(dong.values.joinToString(",") { o ->
                val s = (o?.toString() ?: "").replace("\"", "\"\"")
                if (s.contains(",") || s.contains("\n") || s.contains("\"")) "\"$s\"" else s
            })
        }
        ctx.contentResolver.openOutputStream(uri, "wt")?.use { it.write(sb.toString().toByteArray(Charsets.UTF_8)) }
    }
}
