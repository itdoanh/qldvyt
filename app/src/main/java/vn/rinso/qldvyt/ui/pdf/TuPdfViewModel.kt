package vn.rinso.qldvyt.ui.pdf

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import java.io.File
import javax.inject.Inject
import vn.rinso.qldvyt.data.pdf.PdfLuuTru

/** ViewModel tủ PDF: đọc 2 cấp thư mục local (tổng / NV → theo tháng). */
@HiltViewModel
class TuPdfViewModel @Inject constructor(
    private val kho: PdfLuuTru,
) : ViewModel() {
    var tick by mutableIntStateOf(0)
        private set

    fun lamMoi() { tick++ }

    fun nhomTheoLoai(loai: Int): List<PdfLuuTru.NhomThang> =
        if (loai == 0) kho.dsThangTong() else kho.dsThangNv()

    fun fileTheoThang(loai: Int, thangNam: Pair<Int, Int>?): List<File> {
        if (thangNam == null) return emptyList()
        val (t, n) = thangNam
        return if (loai == 0) kho.dsFileTong(t, n) else kho.dsFileNv(t, n)
    }
}
