# Skill: room-backup-qldvyt (Local DB + Backup file skill)

Dùng khi agent đụng tới Room, migration, backup/restore.

## Quy tắc Room
- DB `qldvyt.db`, version 1 khởi đầu, `exportSchema=true` vào `app/schemas/`.
- Tiền `Long`, ngày `Long epoch millis`, FK có index, ghi tiền trong `@Transaction`.
- Đổi entity → tăng version + viết `Migration`, không destructive migration trên release.

## Quy tắc Backup file (.zip qua SAF)
- Zip gồm: `qldvyt.db` (checkpoint WAL trước), `datastore/`, `meta.json`, `CHECKSUM.sha256`.
- Tên: `qldvyt-backup-yyyyMMdd-HHmmss.zip`. Restore: verify checksum → auto-backup hiện tại → chép đè → restart.
- Không xin `READ/WRITE_EXTERNAL_STORAGE` legacy; chỉ dùng SAF `ACTION_CREATE/OPEN_DOCUMENT`.
- Nguồn: `docs/02-thiet-ke-database-room.md` §4.
