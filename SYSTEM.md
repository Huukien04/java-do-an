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

Ứng dụng hiện hỗ trợ 3 ROLE tài khoản:

```text
ROLE_ADMIN
ROLE_TEACHER
ROLE_STUDENT
```

Các quyền nghiệp vụ Trưởng khoa hiện được thực thi bằng `ROLE_ADMIN`; dự án chưa có ROLE_TRUONG_KHOA riêng. Không tạo tài khoản riêng cho GVHD, GVPB, Chủ tịch hội đồng hoặc Thư ký hội đồng.

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

## 3.3. Phân biệt mô hình và quyền đang chạy

Theo mô hình nghiệp vụ, quản trị tài khoản tách biệt với quyết định chuyên môn. Tuy nhiên ứng dụng hiện chưa có ROLE_TRUONG_KHOA; các màn hình duyệt, điều hành đợt, phân công và công bố hiện được bảo vệ bằng `ROLE_ADMIN`. Không cấp quyền này cho tài khoản không được phép quản lý nghiệp vụ.

---

# 4. Quản trị nghiệp vụ (ROLE_ADMIN)

ROLE riêng chưa được tạo trong ứng dụng:

```text
ROLE_TRUONG_KHOA
```

Các quyền mô tả trong phần này hiện do `ROLE_ADMIN` thực hiện.

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
* Thời gian bắt đầu và kết thúc duyệt đề tài.
* Thời gian bắt đầu SV đăng ký.
* Thời gian kết thúc SV đăng ký.
* Hạn nhóm nộp báo cáo.
* Hạn GVPB/thành viên hội đồng nộp điểm.
* Ngày báo cáo hội đồng (chỉ KLTN).

Các mốc phải theo thứ tự: kết thúc GV đăng ký < bắt đầu duyệt < kết thúc duyệt < bắt đầu SV đăng ký < kết thúc SV đăng ký < hạn nộp báo cáo < hạn nộp điểm < ngày hội đồng (nếu có). Mặc định trạng thái đợt tự chuyển theo các mốc thời gian. Trong menu thao tác, quản trị viên có thể chọn GV đăng ký đề tài, SV lập nhóm đăng ký đề tài, kết thúc nhận bài và tự công bố kết quả, hủy đợt hoặc quay lại tự động theo lịch. Chọn kết thúc nhận bài sẽ khóa nộp ngay; khi tất cả giảng viên liên quan đã chấm và (với KLTN) Chủ tịch đã xác nhận, hệ thống tự công bố kết quả và hoàn tất đợt. Nếu chưa đủ điều kiện chấm/công bố, đợt tiếp tục chờ; tới hạn đã cấu hình, luồng tự động vẫn xử lý chuyển sang giai đoạn chấm. Các thao tác này không yêu cầu nhập mốc thời gian hay kiểm tra trạng thái hiện tại.

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

* Hạn nộp điểm được thiết lập cho mọi loại đợt.
* Ngày báo cáo hội đồng chỉ thiết lập khi loại là KLTN.

---

# 5. Quản lý đề tài

Trưởng khoa (hiện được ánh xạ vào `ROLE_ADMIN`) có quyền:

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

MON_HOC, NCKH, TLCN dùng 1–2 GVPB được phân công. KLTN dùng hội đồng 3–5 người; GVHD không được chấm đề tài mình hướng dẫn. Chỉ đề tài có nhóm được duyệt và đã nộp báo cáo mới được phân công/chấm.

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

Chỉ KLTN sử dụng hội đồng để chấm điểm. Không đưa đề tài chưa có báo cáo đã nộp vào hội đồng.

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

ROLE_ADMIN có quyền:

* Kiểm tra kết quả.
* Kiểm tra điểm.
* Kiểm tra nhận xét.
* Công bố kết quả.
* Cho phép sinh viên xem kết quả.

Mỗi giảng viên nhập tiêu chí riêng (MON_HOC/NCKH/TLCN: nội dung, sản phẩm, báo cáo; KLTN thêm thuyết trình). Điểm của một giảng viên là trung bình các tiêu chí, làm tròn 1 chữ số; điểm cuối là trung bình điểm giảng viên, làm tròn 2 chữ số. Chưa được tổng hợp trước khi tất cả người chấm liên quan hoàn tất; sau hạn, hệ thống được tổng hợp phần điểm đã nộp và nhắc những người còn thiếu. Với KLTN, chỉ Chủ tịch được xác nhận kết quả. Chỉ công bố kết quả mới hiển thị cho sinh viên; điểm là điểm chung của nhóm/đề tài.

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

Mỗi lần nộp tạo một phiên bản mới. Báo cáo nhận PDF/DOCX; MON_HOC bắt buộc nộp thêm source dạng ZIP. Chỉ nhóm trưởng nộp được, và chỉ thành viên nhóm, giảng viên được phân công liên quan hoặc ADMIN mới được xem/tải lịch sử và tệp.

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
ROLE_ADMIN (nghiệp vụ Trưởng khoa)
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

| Chức năng | ADMIN (nghiệp vụ Trưởng khoa) | GIẢNG VIÊN | SINH VIÊN |
| --- | --- | --- | --- |
| Quản lý tài khoản/bộ môn | Có | Không | Không |
| Tạo/sửa/gia hạn đợt | Có | Không | Không |
| Duyệt/công bố đề tài | Có | Không | Không |
| Tạo đề tài | Không | Có | Không |
| Tạo nhóm | Không | Không | Có |
| Thêm thành viên/đăng ký đề tài | Không | Không | Nhóm trưởng |
| Phân công GVHD/GVPB/hội đồng | Có | Không | Không |
| Hướng dẫn SV | Không | GVHD | Không |
| Xem báo cáo | Có | Theo phân công | Thành viên nhóm |
| Nộp báo cáo | Không | Không | Nhóm trưởng |
| Chấm điểm | Không | GVPB/HĐ được phân công | Không |
| Xác nhận điểm KLTN | Không | Chủ tịch HĐ | Không |
| Công bố kết quả | Có | Không | Không |
| Xem điểm đã công bố | Có | Theo phân công | Có, nếu là thành viên nhóm |

---

# 23. Các Business Rule bắt buộc

## 23.1. Đợt đăng ký

* Chỉ GV được đăng ký đề tài trong thời gian GV đăng ký.
* Chỉ SV được đăng ký đề tài trong thời gian SV đăng ký.
* GVPB phải nộp điểm trước hạn.
* Hạn nộp điểm áp dụng cho mọi loại đợt.
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
* Mỗi lần nộp tạo phiên bản mới; báo cáo là PDF/DOCX, source ZIP bắt buộc với MON_HOC.
* Nhóm trưởng có thể nộp lại đến hạn nộp hoặc đến khi quản trị viên chọn kết thúc đợt nhận bài; sau đó hệ thống từ chối cả nộp mới lẫn nộp phiên bản tiếp theo.
* Chỉ bài nộp đúng hạn mới đủ điều kiện chấm điểm.

## 23.6. Điểm

* Mỗi giảng viên chấm đủ tiêu chí; điểm tiêu chí và điểm cuối được làm tròn lần lượt 1 và 2 chữ số.
* Không tổng hợp trước khi đủ điểm; khi quá hạn, hệ thống nhắc người chấm còn thiếu và tổng hợp phần đã nộp đối với các đợt không phải KLTN.
* Chủ tịch hội đồng xác nhận điểm KLTN; ADMIN công bố kết quả.
* Chỉ sau khi công bố, sinh viên thuộc nhóm của đề tài mới xem được điểm chung và nhận xét.

---

# 24. Kiến trúc ROLE nên sử dụng

Không nên thiết kế quá nhiều role tài khoản.

Nên sử dụng:

```text
ACCOUNT ROLES
│
├── ROLE_ADMIN
├── ROLE_TEACHER
└── ROLE_STUDENT
```

`TRUONG_KHOA` là vai trò nghiệp vụ được hệ thống hiện tại ánh xạ vào `ROLE_ADMIN`, không phải ROLE đăng nhập riêng.

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
