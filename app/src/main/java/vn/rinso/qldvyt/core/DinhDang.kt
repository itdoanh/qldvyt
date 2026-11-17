package vn.rinso.qldvyt.core

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val viVN = Locale("vi", "VN")

/** Định dạng tiền VNĐ: 750000 -> "750.000đ". Dùng Long, không dùng Float/Double. */
fun dinhDangTien(dong: Long): String =
    NumberFormat.getNumberInstance(viVN).format(dong) + "đ"

/** Định dạng ngày dd/MM/yyyy. */
fun dinhDangNgay(millis: Long): String =
    SimpleDateFormat("dd/MM/yyyy", viVN).format(Date(millis))

/** Tỷ lệ 30% mặc định của nhiệm vụ (đi tiêm truyền, thay băng, làm thủ tục...). */
const val TY_LE_NHIEM_VU_MAC_DINH = 0.30

/** Số công chuẩn 1 tháng, cấu hình được ở màn Cài đặt. */
const val CONG_CHUAN_MAC_DINH = 26.0

/** Số ngày quá hạn thì cảnh báo công nợ. */
const val SO_NGAY_CANH_BAO_NO_MAC_DINH = 7
