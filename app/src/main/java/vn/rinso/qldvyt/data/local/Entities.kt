package vn.rinso.qldvyt.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/** Cấp bậc nhân viên (VD: Thực tập, Nhân viên, Kỹ thuật viên chính, Quản lý). */
@Entity(tableName = "cap_bac")
data class CapBac(
    @PrimaryKey val id: String,
    val ten: String,
    val moTa: String = "",
    val luongCoBan: Long = 0L,      // VNĐ/tháng (Đồng - Long)
    val chuyenCanMuc: Long = 0L,    // Mức thưởng chuyên cần (Đồng)
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis(),
)

/** Nhân viên các cấp. Tiền lương dùng Long (VNĐ). */
@Entity(
    tableName = "nhan_vien",
    foreignKeys = [ForeignKey(CapBac::class, arrayOf("id"), arrayOf("capBacId"))],
    indices = [Index("maNv", unique = true), Index("sdt", unique = true), Index("capBacId")]
)
data class NhanVien(
    @PrimaryKey val id: String,
    val maNv: String,               // VD: NV001
    val hoTen: String,
    val sdt: String = "",
    val diaChi: String = "",
    val capBacId: String,
    val ngayVaoLam: Long = System.currentTimeMillis(),
    /** DANGLAM / TAMNGHI / NGHIVIEC */
    val trangThai: String = "DANGLAM",
    /** null = dùng lương chuẩn của cấp bậc */
    val luongCoBanOverride: Long? = null,
    val hoaHongMacDinh: Double = 0.0,
    val stk: String = "",
    val ghiChu: String = "",
)

/** Chấm công theo ngày: số công 0 / 0.5 / 1. */
@Entity(
    tableName = "cham_cong",
    foreignKeys = [ForeignKey(NhanVien::class, arrayOf("id"), arrayOf("nhanVienId"), ForeignKey.CASCADE)],
    indices = [Index(value = ["nhanVienId", "ngay"], unique = true)]
)
data class ChamCong(
    @PrimaryKey val id: String,
    val nhanVienId: String,
    val ngay: Long,                 // epoch millis đầu ngày
    val soCong: Double = 1.0,
    val diMuon: Boolean = false,
    val ghiChu: String = "",
)

/** Dịch vụ (VD: tiêm truyền tại nhà, thay băng, làm thủ tục). */
@Entity(tableName = "dich_vu", indices = [Index("maDv", unique = true)])
data class DichVu(
    @PrimaryKey val id: String,
    val maDv: String,
    val ten: String,
    val moTa: String = "",
    val giaNiemYet: Long = 0L,      // VNĐ, giá catalog (snapshot vào đơn khi bán)
    val donVi: String = "lần",
    val trangThai: String = "DANGBAN",
)

/** Gói dịch vụ (VD: gói chăm sóc sau mổ 7 ngày). */
@Entity(tableName = "goi_dich_vu", indices = [Index("maGoi", unique = true)])
data class GoiDichVu(
    @PrimaryKey val id: String,
    val maGoi: String,
    val ten: String,
    val moTa: String = "",
    val giaGoi: Long = 0L,
    val trangThai: String = "DANGBAN",
)

/** Vật tư kho: giá nhập trung bình (bình quân gia quyền), tồn kho. */
@Entity(tableName = "vat_tu", indices = [Index("maVt", unique = true)])
data class VatTu(
    @PrimaryKey val id: String,
    val maVt: String,
    val ten: String,
    val donVi: String = "cái",
    val giaNhapTB: Long = 0L,
    val tonKho: Double = 0.0,
    val tonToiThieu: Double = 0.0,
    val trangThai: String = "DANGBAN",
)

/** Khách hàng. */
@Entity(tableName = "khach_hang", indices = [Index("maKh", unique = true), Index("sdt", unique = true)])
data class KhachHang(
    @PrimaryKey val id: String,
    val maKh: String,
    val hoTen: String,
    val sdt: String = "",
    val diaChi: String = "",
    val ngaySinh: Long? = null,
    val ghiChu: String = "",
    val createdAt: Long = System.currentTimeMillis(),
)

/** Đơn hàng / phiếu dịch vụ. Tiền snapshot để chống trượt giá. */
@Entity(
    tableName = "don_hang",
    foreignKeys = [ForeignKey(KhachHang::class, arrayOf("id"), arrayOf("khachHangId"))],
    indices = [Index("maDh", unique = true), Index("khachHangId"), Index(value = ["ngayTao", "trangThai"])]
)
data class DonHang(
    @PrimaryKey val id: String,
    val maDh: String,
    val khachHangId: String,
    val ngayTao: Long = System.currentTimeMillis(),
    val ngayHen: Long? = null,
    /** CHO_XU_LY / DANG_LAM / HOAN_THANH / HUY */
    val trangThai: String = "CHO_XU_LY",
    val tongTienHang: Long = 0L,
    val giamGia: Long = 0L,
    val tongThanhToan: Long = 0L,
    val daThu: Long = 0L,
    val conNo: Long = 0L,
    val ghiChu: String = "",
)

/** Nhiệm vụ (công) của NV: tiêm truyền, thay băng, làm thủ tục... hưởng 30%. */
@Entity(
    tableName = "nhiem_vu",
    foreignKeys = [
        ForeignKey(NhanVien::class, arrayOf("id"), arrayOf("nhanVienId")),
        ForeignKey(DonHang::class, arrayOf("id"), arrayOf("donHangId")),
    ],
    indices = [Index("maViec", unique = true), Index("nhanVienId"), Index("donHangId")]
)
data class NhiemVu(
    @PrimaryKey val id: String,
    val maViec: String,
    val donHangId: String? = null,
    val tenCongViec: String,
    /** TIEM_TRUYEN / THAY_BANG / THU_TUC / KHAC */
    val loai: String = "KHAC",
    val nhanVienId: String,
    val ngayGiao: Long = System.currentTimeMillis(),
    val ngayHoanThanh: Long? = null,
    val tongGia: Long = 0L,
    val tyLeHuong: Double = 0.30,
    val tienNvHuong: Long = 0L,
    /** DAGIAO / DANGLAM / HOANTHANH / HUY */
    val trangThai: String = "DAGIAO",
)
