package vn.rinso.qldvyt.data.pdf

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Kho PDF local (offline-first).
 * Goc: filesDir - pdf-luong -
 *  - bang_luong_tong - thang MM yyyy - file pdf
 *  - bang_luong_nv - thang MM yyyy - file pdf
 */
@Singleton
class PdfLuuTru @Inject constructor(@ApplicationContext private val ctx: Context) {

    data class NhomThang(val thang: Int, val nam: Int, val soFile: Int, val dungLuong: Long)

    fun goc(): File = File(ctx.filesDir, "pdf-luong").apply { mkdirs() }

    fun thuMucTong(thang: Int, nam: Int): File =
        File(goc(), "bang_luong_tong/thang_%02d_%d".format(thang, nam)).apply { mkdirs() }

    fun thuMucNv(thang: Int, nam: Int): File =
        File(goc(), "bang_luong_nv/thang_%02d_%d".format(thang, nam)).apply { mkdirs() }

    fun fileBangTong(thang: Int, nam: Int): File =
        File(thuMucTong(thang, nam), "bang-luong-thang-$thang-$nam.pdf")

    /** Tên file phiếu an toàn (bỏ dấu/cách để không lỗi FileProvider). */
    fun filePhieu(maNv: String, thang: Int, nam: Int): File {
        val safe = maNv.trim().ifBlank { "NV" }
            .replace(Regex("[^A-Za-z0-9_-]+"), "-").take(24)
        return File(thuMucNv(thang, nam), "phieu-luong-thang-$thang-$nam-$safe.pdf")
    }

    private fun quetThuMuc(cha: File): List<NhomThang> {
        if (!cha.exists()) return emptyList()
        return cha.listFiles { f -> f.isDirectory }?.mapNotNull { d ->
            val m = Regex("thang_(\\d{1,2})_(\\d{4})").find(d.name) ?: return@mapNotNull null
            val t = m.groupValues[1].toIntOrNull() ?: return@mapNotNull null
            val n = m.groupValues[2].toIntOrNull() ?: return@mapNotNull null
            val files = d.listFiles { f -> f.isFile && f.name.endsWith(".pdf", true) } ?: emptyArray()
            NhomThang(t, n, files.size, files.sumOf { it.length() })
        }?.sortedWith(compareByDescending<NhomThang> { it.nam }.thenByDescending { it.thang })
            ?: emptyList()
    }

    fun dsThangTong(): List<NhomThang> = quetThuMuc(File(goc(), "bang_luong_tong"))
    fun dsThangNv(): List<NhomThang> = quetThuMuc(File(goc(), "bang_luong_nv"))

    fun dsFileTong(thang: Int, nam: Int): List<File> =
        thuMucTong(thang, nam).listFiles { f -> f.isFile && f.name.endsWith(".pdf", true) }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()

    fun dsFileNv(thang: Int, nam: Int): List<File> =
        thuMucNv(thang, nam).listFiles { f -> f.isFile && f.name.endsWith(".pdf", true) }
            ?.sortedByDescending { it.lastModified() } ?: emptyList()
}
