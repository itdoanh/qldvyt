package vn.rinso.qldvyt.core

import org.junit.Assert.assertEquals
import org.junit.Test

/** Kiểm tra SHA-256 + CSV BOM tiếng Việt — chạy không cần máy thật. */
class Sha256Test {
    @Test
    fun `sha256 cua chuoi rong dung chuan`() {
        assertEquals(
            "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
            Sha256.cuaChuoi("")
        )
    }

    @Test
    fun `sha256 cua abc dung chuan`() {
        assertEquals(
            "ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
            Sha256.cuaChuoi("abc")
        )
    }
}
