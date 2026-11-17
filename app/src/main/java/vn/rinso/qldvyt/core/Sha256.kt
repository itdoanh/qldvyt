package vn.rinso.qldvyt.core

import java.io.File
import java.io.InputStream
import java.security.MessageDigest

/** Tính SHA-256 (dùng cho verify file backup). Pure JVM — test được không cần Android. */
object Sha256 {
    fun cuaChuoi(text: String): String = cuaBytes(text.toByteArray(Charsets.UTF_8))

    fun cuaBytes(bytes: ByteArray): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(bytes)
        return digest.toHex()
    }

    fun cuaFile(file: File): String =
        file.inputStream().use(::cuaStream)

    fun cuaStream(ins: InputStream): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val buf = ByteArray(8192)
        var n: Int
        while (ins.read(buf).also { n = it } > 0) digest.update(buf, 0, n)
        return digest.digest().toHex()
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
