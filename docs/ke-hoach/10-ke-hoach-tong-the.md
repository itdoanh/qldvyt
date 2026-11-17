# Kế hoạch tổng thể (Roadmap) — QLDVYT

> Nhà phát triển: Rinso (Thuộc Rin) | Lập trình viên: Đoàn Văn Doanh (itdoanh) | Repo: https://github.com/itdoanh/qldvyt.git

## Giai đoạn (Phase)
| Phase | Nội dung | Docs | Tiêu chí xong (Definition of Done) |
|-------|----------|------|-------------------------------------|
| P0 | Setup: repo, docs, scaffold Android, CI local, emulator QA | `12-p0-setup.md` | App cài được lên emulator, docs đủ |
| P1 | Nền tảng: DB Room + Backup file + Cài đặt + Điều hướng | `13-p1-nen-tang.md` | CRUD chạy, backup/restore .zip OK |
| P2 | Kinh doanh: Khách + Dịch vụ/Gói + Đơn hàng + Thanh toán + Công nợ | `14-p2-kinh-doanh.md` | Tạo đơn 3 bước, công nợ đúng |
| P3 | Vận hành: NV + Chấm công + Nhiệm vụ 30% + Vật tư/Kho | `15-p3-van-hanh.md` | Nhiệm vụ 30% + tồn kho đúng |
| P4 | Tiền: Lương + Doanh thu + Báo cáo CSV/PDF | `16-p4-tien-bao-cao.md` | Bảng lương khớp doc 03 |
| P5 | QA: test kỹ UI/UX trên emulator + polish + release APK | `17-p5-qa-release.md` | Pass checklist `tools/qa/checklist-ui.md` |

## Thứ tự làm
P0 → P1 → P2 → P3 → P4 → P5. Mỗi phase commit riêng, message tiếng Việt. Cuối mỗi phase chụp screenshot emulator đính kèm.
