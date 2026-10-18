# Chức năng, module và luồng làm việc của hệ thống

## 1. Mục đích

Hệ thống hỗ trợ quản lý quy trình đăng ký và thực hiện đề tài sinh viên: lập đợt, giảng viên đề xuất đề tài, sinh viên lập nhóm và đăng ký, giảng viên xử lý đăng ký, nhóm nộp báo cáo, giảng viên đánh giá và ADMIN công bố kết quả.

Tài liệu này mô tả hành vi đang được thể hiện trong mã nguồn. Một số nội dung trong `SYSTEM.md` có thể là định hướng nghiệp vụ rộng hơn và chưa có màn hình hoặc luồng tương ứng ở phiên bản hiện tại.

## 2. Vai trò người dùng

| Vai trò | Chức năng chính hiện có |
| --- | --- |
| ADMIN | Quản lý đợt đăng ký; xem, duyệt, từ chối hoặc xóa đề tài; lập hội đồng; quản lý thông báo; xem/tổng hợp điểm và công bố kết quả. |
| TEACHER (giảng viên) | Tạo và cập nhật đề tài của mình; xem đăng ký nhóm cho đề tài mình tạo và duyệt/từ chối; xem hội đồng được phân công; gửi điểm cho đề tài không do mình hướng dẫn. |
| STUDENT (sinh viên) | Xem đợt và đề tài được duyệt; tạo nhóm, thêm thành viên, đăng ký đề tài; xem đăng ký, tải báo cáo lên và xem kết quả đã công bố. |

Quyền được kiểm tra ở controller bằng Spring Security; một số thao tác còn kiểm tra chủ sở hữu hoặc điều kiện nghiệp vụ tại service.

## 3. Các module

### 3.1. Đăng nhập và bảng điều khiển

- Trang đăng nhập: `/login`.
- Người chưa đăng nhập truy cập trang chủ sẽ được chuyển về đăng nhập.
- Sau đăng nhập, bảng điều khiển hiển thị theo vai trò:
  - ADMIN: số đợt, đề tài chờ duyệt, đăng ký chờ xử lý, hội đồng và kết quả đã công bố.
  - TEACHER: đề tài liên quan, hội đồng, đăng ký đang chờ xử lý và đợt có thể đăng ký đề tài.
  - STUDENT: đợt đang mở, nhóm/đề tài, báo cáo gần nhất và kết quả nếu có.

### 3.2. Quản lý đợt đăng ký

ADMIN tạo, xem, chỉnh sửa và đổi trạng thái đợt tại `/admin/periods`. Đợt lưu loại đề tài cùng các khung đăng ký của giảng viên, giai đoạn duyệt, đăng ký của sinh viên, hạn nộp điểm và ngày báo cáo hội đồng.

Khi tạo hoặc cập nhật, hệ thống kiểm tra riêng từng khung thời gian (thời điểm bắt đầu phải trước thời điểm kết thúc); các giai đoạn có thể chồng lấn để SV đăng ký sớm khi đã có đề tài được duyệt. Với loại TLCN/KLTN phải có hạn nộp điểm; riêng KLTN phải có ngày báo cáo hội đồng. Đợt mới bắt đầu ở trạng thái `DRAFT`; ADMIN chuyển trạng thái theo quy trình. Không thể chuyển sang duyệt nếu chưa có đề tài GV gửi, hoặc công bố/mở đăng ký SV nếu chưa có đề tài được duyệt. Khi hết hạn mà chưa có đề tài, ADMIN có thể gia hạn mốc kết thúc GV rồi mở lại giai đoạn; hoặc hủy/hoãn đợt.

Giảng viên tạo đề tài trong trạng thái `PHASE1_TEACHER_REG` hoặc `PHASE2_STUDENT_REG` khi còn trong khung giờ GV; nhờ vậy khung GV và SV có thể chồng lấn. Sinh viên chỉ tạo nhóm/đăng ký trong trạng thái `PHASE2_STUDENT_REG`, trong khung giờ SV và khi có đề tài được Khoa phê duyệt. Hạn SV đã qua thì phải cập nhật lịch trước khi mở/gia hạn lại.

### 3.3. Quản lý đề tài

Giảng viên tạo đề tài tại `/topics/create`, chọn đợt, bộ môn, nội dung và một hoặc hai giảng viên hướng dẫn. Đề tài mới có trạng thái `PENDING_APPROVAL`. Người tạo có thể cập nhật hoặc xóa đề tài của mình khi đề tài chưa ở trạng thái `APPROVED` hoặc `ASSIGNED`.

ADMIN xem danh sách đề tài, duyệt hoặc từ chối tại `/topics`. Khi duyệt, đề tài chuyển sang `APPROVED` và sinh viên có thể đăng ký. Khi từ chối, lý do (nếu có) được đưa vào thông báo riêng cho giảng viên tạo đề tài.

Giảng viên không thể chỉnh sửa/xóa đề tài đã được duyệt hoặc phân công. ADMIN có thể xóa đề tài; thao tác này cũng xử lý các dữ liệu đăng ký, hội đồng, điểm/kết quả liên quan và gửi thông báo cho những người bị ảnh hưởng.

### 3.4. Quản lý nhóm sinh viên

Trong đợt đang mở đăng ký và còn đề tài được duyệt để chọn, sinh viên tạo nhóm tại `/student/group`. Người tạo là nhóm trưởng và cũng là thành viên đầu tiên. Nhóm trưởng có thể thêm thành viên bằng mã sinh viên; nhóm tối đa ba người. Một sinh viên không thể tham gia nhiều nhóm trong cùng một đợt.

Chỉ nhóm trưởng được đăng ký đề tài cho nhóm. Danh sách lựa chọn chỉ gồm đề tài đã được duyệt của đợt; mỗi nhóm chỉ đăng ký một đề tài và một đề tài không thể được đăng ký bởi nhiều nhóm.

### 3.5. Xử lý đăng ký đề tài của nhóm

Giảng viên xem đăng ký tại `/teacher/registrations`. Danh sách được giới hạn theo đề tài do giảng viên đó tạo. Giảng viên tạo đề tài có thể duyệt hoặc từ chối đăng ký; từ chối có thể kèm lý do.

- Duyệt: đăng ký chuyển `APPROVED`, nhóm chuyển `APPROVED`, đề tài chuyển `ASSIGNED`; thành viên nhóm nhận thông báo.
- Từ chối: đăng ký chuyển `REJECTED`, lý do được lưu, nhóm trở lại trạng thái có thể đăng ký đề tài khác; thành viên nhóm nhận thông báo.
- Sinh viên xem các đăng ký và kết quả xử lý tại `/student/registrations`.

### 3.6. Nộp và tải báo cáo

Nhóm trưởng tải báo cáo lên qua chức năng nộp bài tại `/submissions/upload`. Hệ thống lưu tệp trên máy chủ, ghi nhận tên, kích thước, ghi chú, người nộp và số phiên bản tăng dần; nhóm chuyển sang trạng thái `SUBMITTED`. Thành viên khác không được nộp thay nhóm trưởng.

Tệp được lưu theo cấu hình `app.upload.dir`; nếu không cấu hình, thư mục mặc định là `${user.home}/uploads/fit-topics/`. Hệ thống có chức năng tải tệp báo cáo đã lưu.

### 3.7. Hội đồng phản biện

ADMIN lập và xem hội đồng tại `/admin/councils`, gắn hội đồng với một đợt, ngày/địa điểm bảo vệ, giảng viên và đề tài. Mỗi hội đồng có từ ba đến năm giảng viên, gồm một chủ tịch, một thư ký và các thành viên; không được trùng giảng viên giữa các vai trò.

Đề tài gán cho hội đồng phải ở trạng thái `ASSIGNED`, thuộc cùng đợt với hội đồng và chưa được gán vào hội đồng khác. Giảng viên xem các hội đồng có mình tham gia qua danh sách chấm điểm.

### 3.8. Chấm điểm và kết quả

Giảng viên nhập điểm và nhận xét cho đề tài tại `/scoring/topic/{topicId}`. Điểm hợp lệ nằm trong khoảng 0–10. Giảng viên hướng dẫn đề tài không được chấm chính đề tài đó. Mỗi giảng viên có một bản ghi điểm cho mỗi đề tài; gửi lại sẽ cập nhật điểm/nhận xét của chính người đó.

Sau khi có điểm, hệ thống tính trung bình các điểm hiện có, làm tròn hai chữ số thập phân. Điểm trung bình từ 5.0 trở lên được đánh dấu `PASSED`, thấp hơn 5.0 là `FAILED`. ADMIN có thể công bố kết quả theo đợt tại `/scoring/admin/publish`. Sinh viên xem danh sách kết quả đã công bố; ADMIN và TEACHER có thể xem danh sách kết quả chưa công bố.

### 3.9. Thông báo

ADMIN tạo thông báo chung theo vai trò hoặc thông báo cá nhân tại `/notifications/create`; thông báo có thể gắn với đợt. Người dùng xem thông báo chung dành cho vai trò của mình hoặc thông báo cá nhân, mở chi tiết để đánh dấu đã đọc và xem số thông báo chưa đọc.

Các luồng duyệt đề tài, duyệt/từ chối đăng ký nhóm và xóa đề tài cũng tạo thông báo tự động cho người liên quan.

## 4. Luồng nghiệp vụ tổng thể

```text
ADMIN tạo đợt và mở giai đoạn đăng ký giảng viên
        ↓
Giảng viên tạo đề tài và khai báo GV hướng dẫn
        ↓
ADMIN duyệt hoặc từ chối đề tài
        ↓
ADMIN mở giai đoạn đăng ký sinh viên
        ↓
Sinh viên tạo nhóm, thêm thành viên và nhóm trưởng chọn đề tài đã duyệt
        ↓
Giảng viên tạo đề tài duyệt hoặc từ chối đăng ký của nhóm
        ↓
Khi duyệt: nhóm được chấp nhận, đề tài chuyển sang ASSIGNED
        ↓
Nhóm trưởng nộp báo cáo theo phiên bản
        ↓
ADMIN lập hội đồng, gán giảng viên và đề tài
        ↓
Giảng viên nhập điểm/nhận xét; hệ thống tính điểm trung bình và kết quả
        ↓
ADMIN công bố kết quả; sinh viên tra cứu kết quả đã công bố
```

## 5. Trạng thái chính

- Đợt đăng ký: `DRAFT`, `PHASE1_TEACHER_REG`, `PHASE2_STUDENT_REG` và các trạng thái đóng/mở được chọn bởi ADMIN.
- Đề tài: thường đi từ `PENDING_APPROVAL` sang `APPROVED`, sau khi đăng ký nhóm được duyệt thì sang `ASSIGNED`; có thể bị `REJECTED`.
- Đăng ký đề tài: `PENDING`, sau đó thành `APPROVED` hoặc `REJECTED`.
- Nhóm: có các trạng thái như `FORMING`, `READY`, `REGISTERED`, `APPROVED`, `SUBMITTED`.
- Kết quả: `PASSED` hoặc `FAILED`; độc lập với cờ đã công bố hay chưa.

## 6. Quy tắc và giới hạn cần lưu ý

- Chỉ nhóm trưởng được thêm thành viên, đăng ký đề tài và nộp báo cáo.
- Nhóm tối đa ba sinh viên; một sinh viên thuộc tối đa một nhóm trong một đợt.
- Mỗi nhóm đăng ký tối đa một đề tài; một đề tài chỉ được một nhóm đăng ký.
- Đề tài có một hoặc hai GV hướng dẫn; không thể chọn cùng một giảng viên cho cả hai vị trí.
- Giảng viên hướng dẫn không được chấm điểm đề tài mình hướng dẫn.
- Mặc dù có giai đoạn duyệt trong mốc thời gian đợt, luồng duyệt đăng ký nhóm hiện tại được thực hiện bởi giảng viên tạo đề tài, không phải ADMIN.
- Controller hiện cho phép vai trò TEACHER truy cập thao tác gửi điểm cho đề tài bất kỳ, với kiểm tra rõ ràng là không được tự chấm đề tài mình hướng dẫn; chưa thấy kiểm tra người chấm phải là thành viên hội đồng được gán cho đề tài.
- Endpoint xác nhận/tổng hợp kết quả hiện cho phép TEACHER hoặc ADMIN; chưa thấy controller kiểm tra người thao tác phải là chủ tịch hội đồng.
- Chức năng quản lý tài khoản, phân quyền, nhật ký hoạt động, hội đồng nhập biên bản và quy trình riêng cho các loại đề tài khác nhau chưa thấy được cung cấp trong các luồng controller hiện tại; không nên xem các nội dung này là chức năng đã hoàn tất chỉ dựa trên mô tả nghiệp vụ.

## 7. Điểm bắt đầu trong mã nguồn

- Controller điều phối trang và thao tác nằm trong `src/main/java/com/hcmute/topicmanagement/controller/`.
- Quy tắc nghiệp vụ chính nằm trong `src/main/java/com/hcmute/topicmanagement/service/impl/`.
- Giao diện Thymeleaf nằm trong `src/main/resources/templates/`.
- Cấu trúc dữ liệu SQL nằm trong `src/main/resources/schema-mysql.sql`.