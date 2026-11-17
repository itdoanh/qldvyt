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
    val luongCoBan: Long = 0L, // VNĐ/tháng (Đồng - Long)
    val chuyenCanMuc: Long = 0L, // Mức thưởng chuyên cần (Đồng)
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
    val maNv: String, // VD: NV001
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
    val ngay: Long, // epoch millis đầu ngày
    val soCong: Double = 1.0,
    val diMuon: Boolean = false,
    val ghiChu: String = "",
)

/** Thưởng / phạt / chuyên cần. soTien > 0 là thưởng, < 0 là phạt (VNĐ). */
@Entity(
    tableName = "thuong_phat",
    foreignKeys = [ForeignKey(NhanVien::class, arrayOf("id"), arrayOf("nhanVienId"), ForeignKey.CASCADE)],
    indices = [Index("nhanVienId"), Index("ngay")]
)
data class ThuongPhat(
    @PrimaryKey val id: String,
    val nhanVienId: String,
    val ngay: Long = System.currentTimeMillis(),
    /** THUONG / PHAT / CHUYEN_CAN / KHAC */
    val loai: String = "THUONG",
    val soTien: Long = 0L,
    val lyDo: String = "",
)

/** Tạm ứng lương của nhân viên. */
@Entity(
    tableName = "tam_ung",
    foreignKeys = [ForeignKey(NhanVien::class, arrayOf("id"), arrayOf("nhanVienId"), ForeignKey.CASCADE)],
    indices = [Index("nhanVienId"), Index("ngay")]
)
data class TamUng(
    @PrimaryKey val id: String,
    val nhanVienId: String,
    val ngay: Long = System.currentTimeMillis(),
    val soTien: Long = 0L,
    val lyDo: String = "",
)

/** Dịch vụ (VD: tiêm truyền tại nhà, thay băng, làm thủ tục). */
@Entity(tableName = "dich_vu", indices = [Index("maDv", unique = true)])
data class DichVu(
    @PrimaryKey val id: String,
    val maDv: String,
    val ten: String,
    val moTa: String = "",
    val giaNiemYet: Long = 0L, // VNĐ, giá catalog (snapshot vào đơn khi bán)
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

/** Chi tiết gói: gói gồm những dịch vụ nào, số lượng bao nhiêu. */
@Entity(
    tableName = "goi_chi_tiet",
    primaryKeys = ["goiId", "dichVuId"],
    foreignKeys = [
        ForeignKey(GoiDichVu::class, arrayOf("id"), arrayOf("goiId"), ForeignKey.CASCADE),
        ForeignKey(DichVu::class, arrayOf("id"), arrayOf("dichVuId"), ForeignKey.CASCADE),
    ],
    indices = [Index("dichVuId")]
)
data class GoiChiTiet(
    val goiId: String,
    val dichVuId: String,
    val soLuong: Double = 1.0,
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

/** Phiếu nhập kho → app tự cập nhật giaNhapTB + tonKho. */
@Entity(
    tableName = "nhap_kho",
    foreignKeys = [ForeignKey(VatTu::class, arrayOf("id"), arrayOf("vatTuId"))],
    indices = [Index("vatTuId"), Index("ngay")]
)
data class NhapKho(
    @PrimaryKey val id: String,
    val vatTuId: String,
    val ngay: Long = System.currentTimeMillis(),
    val soLuong: Double = 0.0,
    val donGiaNhap: Long = 0L,
    val nhaCungCap: String = "",
    val ghiChu: String = "",
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

/** Dòng chi tiết đơn: snapshot tên + đơn giá tại lúc bán (chống trượt giá). */
@Entity(
    tableName = "don_hang_chi_tiet",
    foreignKeys = [ForeignKey(DonHang::class, arrayOf("id"), arrayOf("donHangId"), ForeignKey.CASCADE)],
    indices = [Index("donHangId")]
)
data class DonHangChiTiet(
    @PrimaryKey val id: String,
    val donHangId: String,
    val dichVuId: String? = null,
    val goiId: String? = null,
    val tenHienThi: String = "",
    val soLuong: Double = 1.0,
    val donGia: Long = 0L,
    val thanhTien: Long = 0L,
)

/** Vật tư đã xuất cho đơn (snapshot giá vốn lúc xuất). */
@Entity(
    tableName = "don_hang_vat_tu",
    foreignKeys = [
        ForeignKey(DonHang::class, arrayOf("id"), arrayOf("donHangId"), ForeignKey.CASCADE),
        ForeignKey(VatTu::class, arrayOf("id"), arrayOf("vatTuId")),
    ],
    indices = [Index("donHangId"), Index("vatTuId")]
)
data class DonHangVatTu(
    @PrimaryKey val id: String,
    val donHangId: String,
    val vatTuId: String,
    val soLuong: Double = 0.0,
    val donGiaVon: Long = 0L,
    val thanhTienVon: Long = 0L,
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

/** Thanh toán: mỗi lần thu tiền ghi 1 dòng (1 đơn thu nhiều lần được). */
@Entity(
    tableName = "thanh_toan",
    foreignKeys = [ForeignKey(DonHang::class, arrayOf("id"), arrayOf("donHangId"), ForeignKey.CASCADE)],
    indices = [Index("donHangId"), Index("ngay")]
)
data class ThanhToan(
    @PrimaryKey val id: String,
    val donHangId: String,
    val khachHangId: String = "",
    val ngay: Long = System.currentTimeMillis(),
    val soTien: Long = 0L,
    /** TIENMAT / CK */
    val phuongThuc: String = "TIENMAT",
    val nguoiThuId: String? = null,
    val ghiChu: String = "",
)

/** Bảng lương tháng: chốt → khóa, chỉ admin được mở khóa. */
@Entity(
    tableName = "bang_luong",
    foreignKeys = [ForeignKey(NhanVien::class, arrayOf("id"), arrayOf("nhanVienId"), ForeignKey.CASCADE)],
    indices = [Index("nhanVienId"), Index(value = ["nhanVienId", "thang", "nam"], unique = true)]
)
data class BangLuong(
    @PrimaryKey val id: String,
    val nhanVienId: String,
    val thang: Int,
    val nam: Int,
    val luongCoBan: Long = 0L,
    val tongCong: Double = 0.0,
    val tienNhiemVu: Long = 0L,
    val hoaHong: Long = 0L,
    val thuong: Long = 0L,
    val chuyenCan: Long = 0L,
    val tamUng: Long = 0L,
    val khauTruKhac: Long = 0L,
    val thucLinh: Long = 0L,
    /** NHAP / CHOT */
    val trangThai: String = "NHAP",
    val ngayChot: Long? = null,
)
