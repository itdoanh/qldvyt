package vn.rinso.qldvyt.domain.pricing

import org.junit.Assert.assertEquals
import org.junit.Test

/** Unit test cho mọi công thức tiền — chạy bằng testDebugUnitTest, không cần máy thật. */
class TinhTienTest {

    @Test
    fun `nhiem vu 500k ty le 30 phan tram ra 150k`() {
        assertEquals(150_000L, TinhTien.tienNvHuong(500_000L, 0.30, true))
    }

    @Test
    fun `nhiem vu huy thi bang 0`() {
        assertEquals(0L, TinhTien.tienNvHuong(500_000L, 0.30, false))
    }

    @Test
    fun `don hang loi nhuan gop vi du docs`() {
        val thanhToan = TinhTien.tongThanhToan(800_000L, 50_000L) // 750k
        assertEquals(750_000L, thanhToan)
        assertEquals(520_000L, TinhTien.loiNhuanGop(thanhToan, 80_000L, 150_000L))
    }

    @Test
    fun `luong vi du docs ra dung 7_384_615`() {
        val theoCong = TinhTien.luongTheoCong(8_000_000L, 24.0, 26.0)
        assertEquals(7_384_615L, theoCong)
        val thucLinh = TinhTien.thucLinh(
            luongTheoCong = theoCong, tienNhiemVu = 1_500_000L,
            hoaHong = 200_000L, thuong = 300_000L, chuyenCan = 0L,
            tamUng = 2_000_000L, khauTruKhac = 0L
        )
        assertEquals(7_384_615L, thucLinh)
    }

    @Test
    fun `gia nhap binh quan dung`() {
        assertEquals(55_000L, TinhTien.giaNhapBinhQuan(10.0, 50_000L, 10.0, 60_000L))
    }
}
