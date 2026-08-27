# HỆ THỐNG QUẢN LÝ ĐỀ TÀI SINH VIÊN

## 1. Tổng quan hệ thống

Hệ thống quản lý đề tài sinh viên được xây dựng nhằm tự động hóa toàn bộ quy trình quản lý đề tài trong Khoa Công nghệ Thông tin.

Hệ thống hỗ trợ quản lý các loại đề tài:

* Môn học
* Nghiên cứu khoa học (NCKH)
* Tiểu luận chuyên ngành (TLCN)
* Khóa luận tốt nghiệp (KLTN)

Quy trình tổng quát:

```text
Tạo đợt đăng ký
        ↓
GV đăng ký đề tài
        ↓
Khoa phê duyệt đề tài
        ↓
Công bố danh sách đề tài
        ↓
SV tạo nhóm
        ↓
Nhóm SV đăng ký đề tài
        ↓
Khoa phê duyệt/tiếp nhận đăng ký
        ↓
Phân công GVHD
        ↓
SV thực hiện đề tài
        ↓
Nhóm trưởng nộp báo cáo
        ↓
Phân công GVPB / Hội đồng
        ↓
Phản biện và chấm điểm
        ↓
Tổng hợp điểm
        ↓
Công bố kết quả
        ↓
SV xem điểm
```

---

# 2. Các loại tài khoản

Hệ thống có 4 ROLE tài khoản chính:

```text
ADMIN
TRUONG_KHOA
GIANG_VIEN
SINH_VIEN
```

Không nên tạo tài khoản riêng cho GVHD, GVPB, Chủ tịch hội đồng hoặc Thư ký hội đồng.

Các vai trò này là **vai trò nghiệp vụ** được gán cho tài khoản GIANG_VIEN tùy theo từng đề tài/hội đồng.

---

# 3. ROLE ADMIN

## 3.1. Mục đích

ADMIN là tài khoản quản trị kỹ thuật của hệ thống.

ADMIN chịu trách nhiệm quản lý tài khoản, phân quyền và các cấu hình hệ thống.

## 3.2. Quyền

ADMIN có quyền:

* Quản lý tài khoản người dùng.
* Tạo tài khoản.
* Chỉnh sửa tài khoản.
* Khóa/mở khóa tài khoản.
* Xóa tài khoản nếu hệ thống cho phép.
* Phân quyền tài khoản.
* Quản lý thông tin Khoa.
* Quản lý thông tin Bộ môn.
* Quản lý thông tin Giảng viên.
* Quản lý thông tin Sinh viên.
* Quản lý cấu hình hệ thống.
* Quản lý thông báo hệ thống.
* Xem nhật ký hoạt động.
* Quản lý bảo mật.

## 3.3. ADMIN không xử lý nghiệp vụ chuyên môn

ADMIN không nên có quyền:

* Phê duyệt đề tài.
* Chấm điểm.
* Phản biện đề tài.
* Phân công GVHD theo nghiệp vụ.
* Quyết định kết quả học tập.

Các nghiệp vụ trên thuộc về Trưởng khoa và Giảng viên.

---

# 4. ROLE TRƯỞNG KHOA

Tên role:

```text
TRUONG_KHOA
```

Trưởng khoa là người quản lý nghiệp vụ chính của hệ thống.

---

## 4.1. Quản lý đợt đăng ký

Trưởng khoa có quyền:

* Tạo đợt đăng ký.
* Chỉnh sửa đợt đăng ký.
* Xem đợt đăng ký.
* Đóng/mở đợt đăng ký.
* Thiết lập thời gian đăng ký.
* Công bố đợt đăng ký.

Mỗi đợt đăng ký gồm:

* Tên đợt đăng ký.
* Loại đợt.
* Thời gian bắt đầu GV đăng ký.
* Thời gian kết thúc GV đăng ký.
* Thời gian bắt đầu SV đăng ký.
* Thời gian kết thúc SV đăng ký.
* Hạn GVPB nộp điểm.
* Ngày báo cáo hội đồng.

---

## 4.2. Loại đợt đăng ký

Các loại:

```text
MON_HOC
NCKH
TLCN
KLTN
```

Quy tắc:

* Hạn GVPB nộp điểm chỉ thiết lập khi loại là TLCN hoặc KLTN.
* Ngày báo cáo hội đồng chỉ thiết lập khi loại là KLTN.

---

# 5. Quản lý đề tài

Trưởng khoa có quyền:

* Xem danh sách đề tài.
* Xem đề tài theo bộ môn.
* Xem chi tiết đề tài.
* Phê duyệt đề tài.
* Từ chối đề tài.
* Yêu cầu GV chỉnh sửa đề tài.
* Công bố danh sách đề tài.

Mỗi đề tài:

* Thuộc đúng một bộ môn.
* Có nội dung/mô tả rõ ràng.
* Có tối thiểu 1 GVHD.
* Có tối đa 2 GVHD.

Quy tắc:

```text
Số GVHD >= 1
Số GVHD <= 2
```

Chỉ những đề tài đã được phê duyệt và công bố mới được SV đăng ký.

---

# 6. Phân công GVHD

Trưởng khoa có quyền:

* Phân công GVHD.
* Thay đổi GVHD nếu cần.
* Xem danh sách GVHD của đề tài.

Một đề tài có:

```text
1 GVHD
hoặc
2 GVHD
```

Một giảng viên có thể hướng dẫn nhiều đề tài.

---

# 7. Quản lý GVPB

Trưởng khoa có quyền:

* Phân công GVPB.
* Xem danh sách GVPB.
* Theo dõi tình trạng nộp điểm.
* Theo dõi hạn nộp điểm.

Business rule:

```text
GV không được chấm/phản biện đề tài mà chính GV đó đang hướng dẫn.
```

---

# 8. Quản lý hội đồng

Trưởng khoa có quyền:

* Tạo hội đồng.
* Xem hội đồng.
* Chỉnh sửa hội đồng.
* Thêm thành viên.
* Xóa thành viên.
* Chỉ định Chủ tịch.
* Chỉ định Thư ký.
* Chỉ định thành viên hội đồng.
* Phân công đề tài cho hội đồng.

Mỗi hội đồng:

```text
Tối thiểu: 3 GV
Tối đa: 5 GV
```

Cấu trúc:

```text
HỘI ĐỒNG
│
├── Chủ tịch
├── Thư ký
└── Thành viên
```

Mỗi hội đồng phải có:

```text
1 Chủ tịch
1 Thư ký
```

---

# 9. Công bố kết quả

Sau khi quá trình phản biện/chấm điểm hoàn tất:

Trưởng khoa có quyền:

* Kiểm tra kết quả.
* Kiểm tra điểm.
* Kiểm tra nhận xét.
* Công bố kết quả.
* Cho phép sinh viên xem kết quả.

Sau khi công bố:

```text
Sinh viên → xem điểm
Sinh viên → xem kết quả đánh giá
```

---

# 10. ROLE GIẢNG VIÊN

Tên role:

```text
GIANG_VIEN
```

Giảng viên là tài khoản có nhiều vai trò nghiệp vụ.

Một tài khoản giảng viên có thể đồng thời:

```text
GVHD
GVPB
CHU_TICH_HOI_DONG
THU_KY_HOI_DONG
THANH_VIEN_HOI_DONG
```

Không tạo tài khoản riêng cho từng vai trò trên.

---

# 11. Vai trò GVHD

GVHD = Giảng viên hướng dẫn.

## Quyền GVHD

* Đăng ký đề tài.
* Xem đề tài của mình.
* Chỉnh sửa đề tài khi chưa được duyệt.
* Xem nhóm sinh viên được phân công.
* Xem danh sách thành viên nhóm.
* Theo dõi quá trình thực hiện.
* Xem báo cáo.
* Hướng dẫn sinh viên.
* Trao đổi với sinh viên.
* Theo dõi tiến độ đề tài.

Một GV có thể hướng dẫn nhiều đề tài.

---

# 12. Đăng ký đề tài của giảng viên

Trong thời gian GV được đăng ký:

Giảng viên có thể:

1. Tạo đề tài.
2. Nhập tên đề tài.
3. Nhập nội dung đề tài.
4. Chọn bộ môn.
5. Đăng ký làm GVHD.
6. Có thể thêm GVHD thứ hai.
7. Gửi đề tài cho Khoa phê duyệt.

Trạng thái đề tài có thể:

```text
DRAFT
PENDING_APPROVAL
APPROVED
REJECTED
PUBLISHED
REGISTERED
IN_PROGRESS
COMPLETED
```

---

# 13. ROLE GVPB

GVPB = Giảng viên phản biện.

GVPB không phải tài khoản riêng.

Một tài khoản GIANG_VIEN có thể được phân công làm GVPB cho một hoặc nhiều đề tài.

## Quyền GVPB

* Xem đề tài được phân công.
* Xem thông tin nhóm.
* Xem báo cáo.
* Xem tài liệu liên quan.
* Nhập nhận xét.
* Nhập điểm.
* Nộp điểm.
* Xem trạng thái nộp điểm.

GVPB không được:

* Chấm đề tài mà mình đang hướng dẫn.
* Sửa điểm của GVPB khác.
* Thay đổi đề tài.
* Phê duyệt đề tài.

---

# 14. Chủ tịch hội đồng

Tên vai trò nghiệp vụ:

```text
CHU_TICH_HOI_DONG
```

Đây là vai trò được gán cho một GIANG_VIEN.

## Quyền

Ngoài quyền của thành viên hội đồng, Chủ tịch có quyền:

* Xem điểm của các thành viên.
* Xem nhận xét của các thành viên.
* Kiểm tra kết quả chấm.
* Tổng hợp điểm.
* Tổng hợp nhận xét.
* Đưa ra kết quả phản biện cuối cùng.
* Xác nhận kết quả hội đồng.

Ví dụ:

```text
GV A: 8.0
GV B: 8.5
GV C: 7.5

Điểm cuối:
(8.0 + 8.5 + 7.5) / 3 = 8.0
```

---

# 15. Thư ký hội đồng

Tên vai trò:

```text
THU_KY_HOI_DONG
```

Thư ký cũng là một GIANG_VIEN.

## Quyền

* Xem hội đồng.
* Xem danh sách thành viên.
* Xem danh sách đề tài.
* Ghi nhận thông tin buổi phản biện.
* Nhập biên bản.
* Cập nhật biên bản.
* Theo dõi tình trạng chấm điểm.
* Hỗ trợ tổng hợp hồ sơ hội đồng.

---

# 16. Thành viên hội đồng

Tên vai trò:

```text
THANH_VIEN_HOI_DONG
```

## Quyền

* Xem đề tài được phân công.
* Xem báo cáo.
* Xem thông tin nhóm.
* Nhập nhận xét.
* Nhập điểm.

## Không được

* Sửa điểm của GV khác.
* Tổng hợp điểm cuối nếu không phải Chủ tịch.
* Sửa thông tin đề tài.
* Phê duyệt đề tài.
* Phân công hội đồng.

---

# 17. ROLE SINH VIÊN

Tên role:

```text
SINH_VIEN
```

Sinh viên thực hiện đề tài theo hình thức nhóm.

---

# 18. Quản lý nhóm sinh viên

Mỗi nhóm:

```text
Tối đa 3 thành viên
```

Mỗi nhóm phải có:

```text
1 nhóm trưởng
```

Một sinh viên:

```text
Chỉ được thuộc 1 nhóm
```

Một nhóm:

```text
Chỉ được đăng ký 1 đề tài
```

---

# 19. Nhóm trưởng

Nhóm trưởng là vai trò nghiệp vụ của SINH_VIEN.

Tên:

```text
NHOM_TRUONG
```

## Quyền

* Tạo nhóm.
* Đặt tên nhóm.
* Thêm thành viên.
* Xóa thành viên.
* Chọn nhóm trưởng.
* Đăng ký đề tài.
* Theo dõi trạng thái đăng ký.
* Nộp báo cáo.
* Xem GVHD.
* Xem tiến độ.
* Xem kết quả.
* Xem điểm.

Đặc biệt:

```text
CHỈ NHÓM TRƯỞNG ĐƯỢC NỘP BÁO CÁO
```

Sinh viên thành viên không được nộp báo cáo.

---

# 20. Sinh viên thành viên

Tên vai trò:

```text
THANH_VIEN_NHOM
```

## Quyền

* Xem nhóm.
* Xem thành viên.
* Xem đề tài.
* Xem GVHD.
* Xem tiến độ.
* Xem báo cáo.
* Xem điểm.
* Xem kết quả đánh giá.

## Không được

* Tham gia nhóm thứ hai.
* Đăng ký thêm đề tài.
* Nộp báo cáo.
* Thay đổi nhóm nếu không có quyền/quy trình cho phép.
* Sửa thông tin đề tài.

---

# 21. Quy trình nghiệp vụ tổng thể

```text
TRƯỞNG KHOA
    │
    ├── Tạo đợt đăng ký
    │
    ↓
GIẢNG VIÊN
    │
    ├── Đăng ký đề tài
    │
    ↓
TRƯỞNG KHOA
    │
    ├── Phê duyệt
    ├── Công bố
    │
    ↓
SINH VIÊN
    │
    ├── Tạo nhóm
    ├── Thêm thành viên
    ├── Chọn đề tài
    ├── Đăng ký đề tài
    │
    ↓
TRƯỞNG KHOA
    │
    ├── Phân công GVHD
    ├── Phân công GVPB
    ├── Thành lập hội đồng
    │
    ↓
SINH VIÊN + GVHD
    │
    ├── Thực hiện đề tài
    ├── Hướng dẫn
    │
    ↓
NHÓM TRƯỞNG
    │
    └── Nộp báo cáo
    │
    ↓
GVPB / HỘI ĐỒNG
    │
    ├── Phản biện
    ├── Nhận xét
    ├── Chấm điểm
    │
    ↓
CHỦ TỊCH HỘI ĐỒNG
    │
    └── Tổng hợp kết quả
    │
    ↓
TRƯỞNG KHOA
    │
    └── Công bố kết quả
    │
    ↓
SINH VIÊN
    │
    └── Xem điểm + kết quả
```

---

# 22. Bảng phân quyền tổng quát

| Chức năng          | ADMIN | TRƯỞNG KHOA | GIẢNG VIÊN          | SINH VIÊN       |
| ------------------ | ----- | ----------- | ------------------- | --------------- |
| Quản lý tài khoản  | Có    | Không       | Không               | Không           |
| Phân quyền         | Có    | Không       | Không               | Không           |
| Tạo đợt đăng ký    | Không | Có          | Không               | Không           |
| Sửa đợt đăng ký    | Không | Có          | Không               | Không           |
| Mở/đóng đợt        | Không | Có          | Không               | Không           |
| Tạo đề tài         | Không | Không       | Có                  | Không           |
| Sửa đề tài         | Không | Có          | Có, đề tài của mình | Không           |
| Duyệt đề tài       | Không | Có          | Không               | Không           |
| Công bố đề tài     | Không | Có          | Không               | Không           |
| Tạo nhóm           | Không | Không       | Không               | Có              |
| Thêm thành viên    | Không | Không       | Không               | Nhóm trưởng     |
| Đăng ký đề tài     | Không | Không       | Không               | Nhóm trưởng     |
| Phân công GVHD     | Không | Có          | Không               | Không           |
| Phân công GVPB     | Không | Có          | Không               | Không           |
| Thành lập hội đồng | Không | Có          | Không               | Không           |
| Hướng dẫn SV       | Không | Không       | GVHD                | Không           |
| Xem báo cáo        | Có    | Có          | Theo phân công      | Thành viên nhóm |
| Nộp báo cáo        | Không | Không       | Không               | Nhóm trưởng     |
| Phản biện          | Không | Có thể xem  | GVPB/HĐ             | Không           |
| Chấm điểm          | Không | Có thể xem  | GVPB/HĐ             | Không           |
| Tổng hợp điểm      | Không | Có thể xem  | Chủ tịch HĐ         | Không           |
| Công bố kết quả    | Không | Có          | Không               | Không           |
| Xem điểm           | Có    | Có          | Theo phân công      | Có              |
| Xem thông báo      | Có    | Có          | Có                  | Có              |
| Quản lý thông báo  | Có    | Có          | Không               | Không           |

---

# 23. Các Business Rule bắt buộc

## 23.1. Đợt đăng ký

* Chỉ GV được đăng ký đề tài trong thời gian GV đăng ký.
* Chỉ SV được đăng ký đề tài trong thời gian SV đăng ký.
* GVPB phải nộp điểm trước hạn.
* Hạn GVPB chỉ áp dụng cho TLCN/KLTN.
* Ngày báo cáo hội đồng chỉ áp dụng cho KLTN.

## 23.2. Đề tài

* Mỗi đề tài thuộc đúng một bộ môn.
* Mỗi đề tài có ít nhất 1 GVHD.
* Mỗi đề tài có tối đa 2 GVHD.
* Chỉ đề tài đã được phê duyệt/công bố mới được SV đăng ký.

## 23.3. Nhóm

* Mỗi nhóm tối đa 3 SV.
* Mỗi nhóm có đúng 1 nhóm trưởng.
* Mỗi SV chỉ thuộc 1 nhóm.
* Mỗi nhóm chỉ đăng ký 1 đề tài.

## 23.4. Hội đồng

* Mỗi hội đồng có từ 3 đến 5 GV.
* Mỗi hội đồng có đúng 1 Chủ tịch.
* Mỗi hội đồng có đúng 1 Thư ký.
* Một GV có thể tham gia nhiều hội đồng/đề tài.
* GV không được chấm đề tài mà mình đang hướng dẫn.

## 23.5. Báo cáo

* Chỉ nhóm trưởng được nộp báo cáo.
* Thành viên thường không được nộp báo cáo.

## 23.6. Điểm

* Điểm cuối được tính bằng trung bình cộng các điểm thành phần.
* Chủ tịch hội đồng chịu trách nhiệm tổng hợp kết quả.
* Sau khi kết quả được công bố, sinh viên được xem điểm.

---

# 24. Kiến trúc ROLE nên sử dụng

Không nên thiết kế quá nhiều role tài khoản.

Nên sử dụng:

```text
ACCOUNT ROLES
│
├── ADMIN
├── TRUONG_KHOA
├── GIANG_VIEN
└── SINH_VIEN
```

Sau đó dùng nghiệp vụ/assignment:

```text
GIANG_VIEN
│
├── GVHD
├── GVPB
├── CHU_TICH_HOI_DONG
├── THU_KY_HOI_DONG
└── THANH_VIEN_HOI_DONG
```

và:

```text
SINH_VIEN
│
├── NHOM_TRUONG
└── THANH_VIEN_NHOM
```

Điều này giúp hệ thống linh hoạt.

Ví dụ:

```text
Nguyễn Văn A
ACCOUNT ROLE = GIANG_VIEN

Assignment:
- GVHD → Đề tài 01
- GVHD → Đề tài 02
- GVPB → Đề tài 05
- CHU_TICH → Hội đồng 03
```

Không cần tạo 4 tài khoản khác nhau cho Nguyễn Văn A.

---

# 25. Nguyên tắc bảo mật và phân quyền

Mọi API và giao diện phải kiểm tra quyền ở backend.

Không được chỉ ẩn nút trên frontend.

Ví dụ:

```text
Frontend:
Ẩn nút "Nộp báo cáo" đối với thành viên thường.

Backend:
Nếu user không phải NHOM_TRUONG
→ từ chối request.
```

Tương tự:

```text
GVPB chấm đề tài
→ Backend kiểm tra GV có phải GVHD của đề tài hay không.

Nếu có:
→ Từ chối chấm điểm.
```

---

# 26. Tóm tắt role

```text
ADMIN
→ Quản trị hệ thống

TRUONG_KHOA
→ Quản lý nghiệp vụ toàn bộ đề tài

GIANG_VIEN
→ Đăng ký đề tài
→ GVHD
→ GVPB
→ Thành viên hội đồng
→ Chủ tịch hội đồng
→ Thư ký hội đồng

SINH_VIEN
→ Tạo nhóm
→ Đăng ký đề tài
→ Thực hiện đề tài
→ Xem kết quả

NHOM_TRUONG
→ Quyền đặc biệt của sinh viên
→ Đăng ký đề tài
→ Nộp báo cáo

THANH_VIEN_NHOM
→ Tham gia thực hiện
→ Xem thông tin
→ Không được nộp báo cáo
```

# 27. Nguyên tắc thiết kế quan trọng

Khi phát triển hệ thống, AI cần hiểu rằng:

1. GVHD/GVPB/Chủ tịch/Thư ký không phải tài khoản độc lập.
2. Đây là các vai trò nghiệp vụ của tài khoản GIANG_VIEN.
3. Nhóm trưởng/thành viên cũng là vai trò nghiệp vụ của SINH_VIEN.
4. Một GV có thể có nhiều vai trò nghiệp vụ cùng lúc.
5. Một SV chỉ thuộc một nhóm.
6. Một nhóm tối đa 3 SV.
7. Một nhóm chỉ đăng ký một đề tài.
8. Một đề tài có 1–2 GVHD.
9. Hội đồng có 3–5 GV.
10. GV không được chấm đề tài mình hướng dẫn.
11. Chỉ nhóm trưởng được nộp báo cáo.
12. Điểm cuối là trung bình cộng các điểm thành phần.
13. Backend phải kiểm tra quyền và business rule.
14. Trưởng khoa là người chịu trách nhiệm chính về nghiệp vụ.
15. ADMIN chủ yếu quản trị tài khoản, hệ thống và bảo mật.
